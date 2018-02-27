package net.kwami.pipe.server;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import net.kwami.utils.Configurator;
import net.kwami.utils.MyLogger;

public class Server {

	private static final MyLogger logger = new MyLogger(Server.class);
	private final ConcurrentMap<RemoteEndpoint, MessagePipe> pipesToClients = new ConcurrentHashMap<>();
	private final ConcurrentMap<MessageKey, Future<String>> futuresTable = new ConcurrentHashMap<>();
	private final MyThreadPoolExecutor threadPoolExecutor;
	private final Gson gson = new GsonBuilder().create();
	private final ByteBuffer byteBuffer;
	private final int serverPort;
	private final Object responseTransmitterLock = new Object();

	public Server() throws Exception {
		super();
		ServerConfig config = Configurator.get(ServerConfig.class);
		byteBuffer = ByteBuffer.allocate(config.getCommandBufferSize());
		this.serverPort = config.getPort();
		threadPoolExecutor = new MyThreadPoolExecutor(this, config.getCorePoolSize(), config.getMaxPoolSize(),
				config.getKeepAliveTime(), TimeUnit.DAYS,
				new ArrayBlockingQueue<Runnable>(config.getSubmitQueueSize()));
	}

	public static void main(String[] args) {
		System.setProperty("config.default.file.type", "json");
		try {
			new Server().run();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void run() {
		try (ServerSocketChannel serverChannel = ServerSocketChannel.open()) {
			startResponseTransmitter();
			serverChannel.socket()
					.bind(new InetSocketAddress(InetAddress.getByName(RemoteEndpoint.MACHINE_ADDRESS), serverPort));
			while (true) {
				SocketChannel socketChannel = serverChannel.accept();
				socketChannel.read(byteBuffer);
				String requestStr = new String(byteBuffer.array(), 0, byteBuffer.position());
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
		rt.setName("ResponseTransmitter");
		rt.start();
	}

	private ManagedThread startTcpRequestReader(SocketChannel socketChannel, RemoteEndpoint remoteEndpoint) throws Exception {
		Command response = new Command(Command.Cmd.RESPONSE);
		response.addParameter("protocol", "TCP");
		byteBuffer.put(response.toString().getBytes());
		byteBuffer.flip();
		socketChannel.write(byteBuffer);
		MessagePipe msgPipe = new TcpPipe(remoteEndpoint, socketChannel);
		pipesToClients.put(remoteEndpoint, msgPipe);
		RequestSubmitter requestSubmitter = new RequestSubmitter(this, msgPipe);
		requestSubmitter.setDaemon(true);
		requestSubmitter.setName(String.format("TcpRequestReader: %s", remoteEndpoint.toString()));
		requestSubmitter.start();
		return requestSubmitter;
	}

	private ManagedThread startFifoRequestReader(SocketChannel socketChannel, RemoteEndpoint remoteEndpoint) throws Exception {
		Command response = new Command(Command.Cmd.RESPONSE);
		response.addParameter("protocol", "FIFO");
		byteBuffer.put(response.toString().getBytes());
		byteBuffer.flip();
		socketChannel.write(byteBuffer);
		String fifoNameRequests = String.format("fifo/requests.%d", String.valueOf(remoteEndpoint.getRemotePort()));
		String fifoNameResponses = String.format("fifo/responses.%d", String.valueOf(remoteEndpoint.getRemotePort()));
		Runtime.getRuntime().exec("mkfifo " + fifoNameRequests);
		Runtime.getRuntime().exec("mkfifo " + fifoNameResponses);
		MessagePipe msgPipe = new FifoPipe(remoteEndpoint, fifoNameRequests, fifoNameResponses);
		pipesToClients.put(remoteEndpoint, msgPipe);
		RequestSubmitter requestSubmitter = new RequestSubmitter(this, msgPipe);
		requestSubmitter.setDaemon(true);
		requestSubmitter.setName(String.format("FifoRequestReader: %s", remoteEndpoint.toString()));
		requestSubmitter.start();
		return requestSubmitter;
	}

	public Object getResponseTransmitterLock() {
		return responseTransmitterLock;
	}

	public ConcurrentMap<RemoteEndpoint, MessagePipe> getPipesToClients() {
		return pipesToClients;
	}

	public ConcurrentMap<MessageKey, Future<String>> getFuturesTable() {
		return futuresTable;
	}

	public MyThreadPoolExecutor getThreadPoolExecutor() {
		return threadPoolExecutor;
	}

}