package net.kwami.pipe.server;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import net.kwami.pipe.FifoPipe;
import net.kwami.pipe.Pipe;
import net.kwami.pipe.RemoteEndpoint;
import net.kwami.pipe.TcpPipe;
import net.kwami.utils.Configurator;
import net.kwami.utils.MyLogger;

/**
 * @author Chris Kriel
 *
 */
public final class PipeServer {

	public static final String RESPONSE_TRANSMITTER_NAME = "ResponseTransmitterThread";
	private static final MyLogger logger = new MyLogger(PipeServer.class);
	private static byte[] fifoByteMap = { 1 };

	public static void main(String[] args) {
		System.setProperty("config.default.file.type", "json");
		try {
			new PipeServer().call();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void freeFifo(int index) {
		synchronized (fifoByteMap) {
			fifoByteMap[index] = 0;
		}
	}

	private final List<ManagedThread> managedThreads = new ArrayList<>();
	private final ConcurrentMap<CallableMessage, Future<String>> executingRequests = new ConcurrentHashMap<>();
	private final MyThreadPoolExecutor threadPoolExecutor;
	private final ByteBuffer commandBuffer;
	private final int pipeCount;
	private final int serverPort;
	private final Object responseTransmitterLock = new Object();
	private int fifoByteMapPosition;

	public PipeServer() throws Exception {
		super();
		ServerConfig config = Configurator.get(ServerConfig.class);
		commandBuffer = ByteBuffer.allocate(config.getCommandBufferSize());
		this.serverPort = config.getPort();
		threadPoolExecutor = new MyThreadPoolExecutor(this, config.getCorePoolSize(), config.getMaxPoolSize(),
				config.getKeepAliveTime(), TimeUnit.DAYS,
				new ArrayBlockingQueue<Runnable>(config.getSubmitQueueSize()));
		pipeCount = config.getMaxFifoMessagePipes() * 2;
		Runtime.getRuntime().exec("rm -rf fifo");
		if (pipeCount == 0)
			return;
		fifoByteMap = new byte[pipeCount];
		Runtime.getRuntime().exec("mkdir fifo");
		for (int i = 0; i < pipeCount; i++) {
			String cmd = "mkfifo fifo/" + i;
			Runtime.getRuntime().exec(cmd);
		}
		// wait for File System to do the above
		Thread.sleep(3000);
	}

	public final void call() throws IOException {
		try (ServerSocketChannel serverChannel = ServerSocketChannel.open()) {
			serverChannel.socket()
					.bind(new InetSocketAddress(InetAddress.getByName(RemoteEndpoint.MACHINE_ADDRESS), serverPort));
			Thread.currentThread()
					.setName("PipeServer" + serverChannel.socket().getLocalSocketAddress().toString());
			while (true) {
				SocketChannel socketChannel = serverChannel.accept();
				Command request = Command.read(socketChannel, commandBuffer);
				if (request.getCommand() == Command.Cmd.CONNECT) {
					InetSocketAddress remoteSocketAddress = (InetSocketAddress) socketChannel.getRemoteAddress();
					RemoteEndpoint remoteEndpoint = new RemoteEndpoint(remoteSocketAddress.getHostString(),
							remoteSocketAddress.getPort());
					if (remoteEndpoint.isForThisMachine()) {
						startFifoRequestReader(socketChannel, remoteEndpoint);
					} else {
						startTcpRequestReader(socketChannel, remoteEndpoint);
					}
				} else if (request.getCommand() == Command.Cmd.SHUTDOWN) {
					for (ManagedThread pipe : managedThreads) {
						pipe.terminate();
					}
					managedThreads.clear();
					break;
				}
			}
		} catch (Exception e) {
			logger.error(e, "SERIOUS PROBLEM: System is not functional");
		}
	}

	private final void startTcpRequestReader(final SocketChannel socketChannel, final RemoteEndpoint remoteEndpoint)
			throws Exception {
		Command response = new Command(Command.Cmd.RESPONSE);
		response.addParameter("protocol", "TCP");
		response.write(socketChannel, commandBuffer);
		Pipe msgPipe = new TcpPipe(remoteEndpoint, socketChannel);
		RequestReader requestSubmitter = new RequestReader(this, msgPipe);
		requestSubmitter.setDaemon(true);
		requestSubmitter.setName("ReqRdr" + remoteEndpoint.toString());
		requestSubmitter.start();
		managedThreads.add(requestSubmitter);
		return;
	}

	private final int getFreeFifo() {
		synchronized (fifoByteMap) {
			for (int i = fifoByteMapPosition; i < fifoByteMap.length; i++)
				if (fifoByteMap[i] == 0) {
					fifoByteMap[i] = 1;
					fifoByteMapPosition = i;
					return i;
				}
			// try to reuse if available
			for (int i = 0; i < fifoByteMap.length; i++)
				if (fifoByteMap[i] == 0) {
					logger.debug("reusing FIFO %d", i);
					fifoByteMap[i] = 1;
					fifoByteMapPosition = i;
					return i;
				}
		}
		return -1;
	}

	private final void startFifoRequestReader(final SocketChannel socketChannel, final RemoteEndpoint remoteEndpoint)
			throws Exception {
		String fifoNameRequests = null;
		String fifoNameResponses = null;
		int[] usedFifos = new int[2];
		int i = getFreeFifo();
		if (i >= 0) {
			fifoNameRequests = "fifo/" + i;
			usedFifos[0] = i;
		}
		if (i >= 0) {
			i = getFreeFifo();
			fifoNameResponses = "fifo/" + i;
			usedFifos[1] = i;
		}
		if (i < 0) {
			logger.error("Server is out of FIFO pipes, falling back to TCP");
			startTcpRequestReader(socketChannel, remoteEndpoint);
			return;
		}
		logger.info(remoteEndpoint + " Communication will be via FIFOs");
		Pipe msgPipe = new FifoPipe(remoteEndpoint, fifoNameRequests, fifoNameResponses);
		RequestReader requestReader = new RequestReader(this, msgPipe);
		requestReader.setFifoIndexes(usedFifos);
		requestReader.setDaemon(true);
		requestReader.setName("ReqRdr/" + fifoNameRequests);
		requestReader.start();
		managedThreads.add(requestReader);
		Command response = new Command(Command.Cmd.RESPONSE);
		response.addParameter("protocol", "FIFO");
		String s = Paths.get(fifoNameRequests).toAbsolutePath().toString();
		response.addParameter(FifoPipe.SERVER_READ_PATH_KEY, s);
		s = Paths.get(fifoNameResponses).toAbsolutePath().toString();
		response.addParameter(FifoPipe.SERVER_WRITE_PATH_KEY, s);
		response.write(socketChannel, commandBuffer);
	}

	public final Object getResponseTransmitterLock() {
		return responseTransmitterLock;
	}

	public final ConcurrentMap<CallableMessage, Future<String>> getExecutingRequests() {
		return executingRequests;
	}

	public final MyThreadPoolExecutor getThreadPoolExecutor() {
		return threadPoolExecutor;
	}

	public List<ManagedThread> getManagedThreads() {
		return managedThreads;
	}

}
