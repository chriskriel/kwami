package net.kwami.pipe.server;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.file.Paths;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import net.kwami.pipe.FifoPipe;
import net.kwami.pipe.MessagePipe;
import net.kwami.pipe.RemoteEndpoint;
import net.kwami.pipe.TcpPipe;
import net.kwami.utils.Configurator;
import net.kwami.utils.MyLogger;

public class PipeServer {

	private static final MyLogger logger = new MyLogger(PipeServer.class);
	private final ConcurrentMap<RemoteEndpoint, MessagePipe> pipesToClients = new ConcurrentHashMap<>();
	private final ConcurrentMap<MessageOrigin, Future<String>> executingRequests = new ConcurrentHashMap<>();
	private final MyThreadPoolExecutor threadPoolExecutor;
	private final Gson gson = new GsonBuilder().create();
	private final ByteBuffer commandBuffer;
	private final int serverPort;
	private final Object responseTransmitterLock = new Object();

	public PipeServer() throws Exception {
		super();
		ServerConfig config = Configurator.get(ServerConfig.class);
		commandBuffer = ByteBuffer.allocate(config.getCommandBufferSize());
		this.serverPort = config.getPort();
		threadPoolExecutor = new MyThreadPoolExecutor(this, config.getCorePoolSize(), config.getMaxPoolSize(),
				config.getKeepAliveTime(), TimeUnit.DAYS,
				new ArrayBlockingQueue<Runnable>(config.getSubmitQueueSize()));
	}

	public static void main(String[] args) {
		System.setProperty("config.default.file.type", "json");
		try {
			new PipeServer().call();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void call() {
		try (ServerSocketChannel serverChannel = ServerSocketChannel.open()) {
			startResponseTransmitter();
			serverChannel.socket()
					.bind(new InetSocketAddress(InetAddress.getByName(RemoteEndpoint.MACHINE_ADDRESS), serverPort));
			Thread.currentThread()
					.setName("PipeServerThread" + serverChannel.socket().getLocalSocketAddress().toString());
			while (true) {
				SocketChannel socketChannel = serverChannel.accept();
				socketChannel.read(commandBuffer);
				String requestStr = new String(commandBuffer.array(), 0, commandBuffer.position());
				commandBuffer.clear();
				Command request = gson.fromJson(requestStr, Command.class);
				if (request.getCommand() == Command.Cmd.CONNECT) {
					InetSocketAddress remoteSocketAddress = (InetSocketAddress) socketChannel.getRemoteAddress();
					RemoteEndpoint remoteEndpoint = new RemoteEndpoint(serverPort, remoteSocketAddress);
					if (remoteEndpoint.isForThisMachine()) {
						startFifoRequestReader(socketChannel, remoteEndpoint);
					} else {
						startTcpRequestReader(socketChannel, remoteEndpoint);
					}
				}
			}
		} catch (Exception e) {
			logger.error(e, "SERIOUS PROBLEM: System is not functional");
		}
	}

	private void startResponseTransmitter() {
		ResponseTransmitter rt = new ResponseTransmitter(this);
		rt.setDaemon(true);
		rt.setName("ResponseTransmitterThread");
		rt.start();
	}

	private ManagedThread startTcpRequestReader(SocketChannel socketChannel, RemoteEndpoint remoteEndpoint)
			throws Exception {
		Command response = new Command(Command.Cmd.RESPONSE);
		response.addParameter("protocol", "TCP");
		commandBuffer.put(response.toString().getBytes());
		commandBuffer.flip();
		socketChannel.write(commandBuffer);
		MessagePipe msgPipe = new TcpPipe(remoteEndpoint, socketChannel);
		pipesToClients.put(remoteEndpoint, msgPipe);
		RequestSubmitter requestSubmitter = new RequestSubmitter(this, msgPipe);
		requestSubmitter.setDaemon(true);
		requestSubmitter.setName(String.format("TcpRequestReaderThread on %s", remoteEndpoint.toString()));
		requestSubmitter.start();
		return requestSubmitter;
	}

	private ManagedThread startFifoRequestReader(SocketChannel socketChannel, RemoteEndpoint remoteEndpoint)
			throws Exception {
		String fifoNameRequests = String.format("fifo/requests.%d", remoteEndpoint.getRemotePort());
		String fifoNameResponses = String.format("fifo/responses.%d", remoteEndpoint.getRemotePort());
		Runtime.getRuntime().exec("mkfifo " + fifoNameRequests);
		Runtime.getRuntime().exec("mkfifo " + fifoNameResponses);
		Thread.sleep(2000);
		MessagePipe msgPipe = new FifoPipe(remoteEndpoint, fifoNameRequests, fifoNameResponses);
		pipesToClients.put(remoteEndpoint, msgPipe);
		RequestSubmitter requestSubmitter = new RequestSubmitter(this, msgPipe);
		requestSubmitter.setDaemon(true);
		requestSubmitter.setName(String.format("FifoRequestReaderThread on: %s", remoteEndpoint.toString()));
		requestSubmitter.start();
		Command response = new Command(Command.Cmd.RESPONSE);
		response.addParameter("protocol", "FIFO");
		String s = Paths.get(fifoNameRequests).toAbsolutePath().toString();
		response.addParameter(FifoPipe.SERVER_READ_PATH_KEY, s);
		s = Paths.get(fifoNameResponses).toAbsolutePath().toString();
		response.addParameter(FifoPipe.SERVER_WRITE_PATH_KEY, s);
		commandBuffer.put(response.toString().getBytes());
		commandBuffer.flip();
		socketChannel.write(commandBuffer);
		return requestSubmitter;
	}

	public Object getResponseTransmitterLock() {
		return responseTransmitterLock;
	}

	public ConcurrentMap<RemoteEndpoint, MessagePipe> getPipesToClients() {
		return pipesToClients;
	}

	public ConcurrentMap<MessageOrigin, Future<String>> getExecutingRequests() {
		return executingRequests;
	}

	public MyThreadPoolExecutor getThreadPoolExecutor() {
		return threadPoolExecutor;
	}

}
