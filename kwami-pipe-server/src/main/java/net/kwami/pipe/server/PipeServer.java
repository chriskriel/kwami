package net.kwami.pipe.server;

import java.io.File;
import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.kwami.pipe.FifoPipe;
import net.kwami.pipe.Pipe;
import net.kwami.pipe.RemoteEndpoint;
import net.kwami.pipe.TcpPipe;
import net.kwami.utils.Configurator;
import net.kwami.utils.MyProperties;

/**
 * @author Chris Kriel
 *
 */
public final class PipeServer {

	public static final String RESPONSE_TRANSMITTER_NAME = "ResponseTransmitterThread";
	private static final Logger LOGGER = LogManager.getLogger(PipeServer.class);
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
	private final MyThreadPoolExecutor threadPoolExecutor;
	private final ByteBuffer commandBuffer;
	private final int serverPort;
	private final Class<MyCallable> callableClass;
	private int fifoByteMapPosition;

	public PipeServer() throws Exception {
		super();
		LOGGER.traceEntry();
		ServerConfig config = Configurator.get(ServerConfig.class);
		commandBuffer = ByteBuffer.allocate(config.getCommandBufferSize());
		this.serverPort = config.getPort();
		System.setProperty("pipe.server.port", String.valueOf(this.serverPort));
		System.setProperty("pipe.server.host", RemoteEndpoint.getMachineAddress());
		callableClass = loadCallableClass(config);
		threadPoolExecutor = new MyThreadPoolExecutor(config.getCorePoolSize(), config.getMaxPoolSize(),
				config.getKeepAliveTime(), TimeUnit.DAYS,
				new ArrayBlockingQueue<Runnable>(config.getSubmitQueueSize()));
		int i = 0;
		File fifoDir = new File(System.getProperty("user.dir"));
		fifoDir = new File(fifoDir, "fifo");
		if (fifoDir.exists()) {
			String[] fifos = fifoDir.list();
			List<String> fifoList = Arrays.asList(fifos);
			for (; i < fifos.length; i++) {
				String fifoName = String.valueOf(i);
				if (fifoList.contains(fifoName)) {
					continue;
				} else {
					if (i > 0 && (i % 2) != 1)
						i--;
					break;
				}
			}
		}
		LOGGER.info("FIFOs 0 to {} can be used as {} bi-directional connections to this server", i - 1, i / 2);
		fifoByteMap = new byte[i];
		callableClass.newInstance(); // configure BasicCapsule class object
	}

	public Class<MyCallable> loadCallableClass(ServerConfig config) throws Exception {
		@SuppressWarnings("unchecked")
		Class<MyCallable> callableClass = (Class<MyCallable>) Class.forName(config.getCallableImplementation());
		return callableClass;
	}

	public final void call() throws IOException {
		LOGGER.traceEntry();
		try (ServerSocketChannel serverChannel = ServerSocketChannel.open()) {
			serverChannel.socket().bind(
					new InetSocketAddress(Inet4Address.getByName(RemoteEndpoint.getMachineAddress()), serverPort));
			Thread.currentThread().setName("PipeServer" + serverChannel.socket().getLocalSocketAddress().toString());
			while (true) {
				SocketChannel socketChannel = serverChannel.accept();
				Command request = Command.read(socketChannel, commandBuffer);
				if (request.getCommand() == Command.Cmd.CONNECT) {
					InetSocketAddress remoteSocketAddress = (InetSocketAddress) socketChannel.getRemoteAddress();
					RemoteEndpoint remoteEndpoint = new RemoteEndpoint(remoteSocketAddress.getHostString(),
							remoteSocketAddress.getPort());
					MyProperties parms = request.getParameters();
					if (!remoteEndpoint.isOnThisMachine())
						startTcpRequestReader(socketChannel, remoteEndpoint);
					else {
						if (parms != null && parms.getBooleanProperty(Command.Parm.FORCE_TCP, false)) {
							startTcpRequestReader(socketChannel, remoteEndpoint);
						} else {
							startFifoRequestReader(socketChannel, remoteEndpoint);
						}
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
			LOGGER.error("SERIOUS PROBLEM: System is not functional", e);
		}
		LOGGER.traceExit();
	}

	private final void startTcpRequestReader(final SocketChannel socketChannel, final RemoteEndpoint remoteEndpoint)
			throws Exception {
		Command response = new Command(Command.Cmd.RESPONSE);
		response.addParameter("protocol", "TCP");
		response.write(socketChannel, commandBuffer);
		Pipe msgPipe = new TcpPipe(remoteEndpoint, socketChannel);
		RequestReader requestSubmitter = new RequestReader(this, msgPipe, callableClass);
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
					LOGGER.debug("reusing FIFO {}", i);
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
			LOGGER.error("Server is out of FIFO pipes, falling back to TCP");
			startTcpRequestReader(socketChannel, remoteEndpoint);
			return;
		}
		LOGGER.info(remoteEndpoint + " Communication will be via FIFOs");
		Pipe msgPipe = new FifoPipe(remoteEndpoint, fifoNameRequests, fifoNameResponses);
		RequestReader requestReader = new RequestReader(this, msgPipe, callableClass);
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

	public final MyThreadPoolExecutor getThreadPoolExecutor() {
		return threadPoolExecutor;
	}

	public List<ManagedThread> getManagedThreads() {
		return managedThreads;
	}

}
