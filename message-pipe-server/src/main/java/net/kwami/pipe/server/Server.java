package net.kwami.pipe.server;

import java.io.IOException;
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

import net.kwami.utils.MyLogger;

public class Server extends ManagedThread {

	private static final MyLogger logger = new MyLogger(Server.class);
	private final ConcurrentMap<PipeKey, MessagePipe> pipesToClients = new ConcurrentHashMap<>();
	private final ConcurrentMap<MessageKey, Future<String>> futuresTable = new ConcurrentHashMap<>();
	private final MyThreadPoolExecutor threadPoolExecutor;
	private final Gson gson = new GsonBuilder().create();
	private final ByteBuffer byteBuffer = ByteBuffer.allocate(256);
	private final int serverPort;
	private Object responseTransmitterLock;

	public Server(int serverPort) {
		super();
		this.serverPort = serverPort;
		threadPoolExecutor = new MyThreadPoolExecutor(this, 10, 10, 10L, TimeUnit.SECONDS, new ArrayBlockingQueue<Runnable>(10));
	}

	public static void main(String[] args) {
		System.setProperty("config.default.file.type", "json");
		new Server(58080).run();
	}

	@Override
	public void run() {
		try (ServerSocketChannel serverChannel = ServerSocketChannel.open()) {
			new ResponseTransmitter(this).run();
			serverChannel.socket()
					.bind(new InetSocketAddress(InetAddress.getByName(PipeKey.MACHINE_ADDRESS), serverPort));
			while (mustRun) {
				if (mustBlock)
					this.wait();
				SocketChannel socketChannel = serverChannel.accept();
				socketChannel.read(byteBuffer);
				String requestStr = new String(byteBuffer.array(), 0, byteBuffer.position());
				Command request = gson.fromJson(requestStr, Command.class);
				if (request.getCommand() == Command.Cmd.CONNECT) {
					InetSocketAddress remoteSocketAddress = (InetSocketAddress) socketChannel.getRemoteAddress();
					PipeKey key = new PipeKey(serverPort, remoteSocketAddress);
					if (key.isForThisMachine()) {
						startFifoRequestReader(socketChannel, key);
					} else {
						startTcpRequestReader(socketChannel, key);
					}
				}
			}
		} catch (Exception e) {
			logger.error(e, "SERIOUS PROBLEM: System is not functional");
		}
	}

	private ManagedThread startTcpRequestReader(SocketChannel socketChannel, PipeKey key) throws IOException {
		Command response = new Command(Command.Cmd.RESPONSE);
		response.addParameter("protocol", "TCP");
		byteBuffer.put(response.toString().getBytes());
		byteBuffer.flip();
		socketChannel.write(byteBuffer);
		MessagePipe msgPipe = new TcpPipe(key, socketChannel);
		pipesToClients.put(key, msgPipe);
		RequestSubmitter requestSubmitter = new RequestSubmitter(this, msgPipe);
		requestSubmitter.setDaemon(true);
		requestSubmitter.setName(String.format("TcpRequestReader: %s", key.toString()));
		requestSubmitter.start();
		return requestSubmitter;
	}

	private ManagedThread startFifoRequestReader(SocketChannel socketChannel, PipeKey key) throws IOException {
		Command response = new Command(Command.Cmd.RESPONSE);
		response.addParameter("protocol", "FIFO");
		byteBuffer.put(response.toString().getBytes());
		byteBuffer.flip();
		socketChannel.write(byteBuffer);
		String fifoNameRequests = String.format("fifo/requests.%d", String.valueOf(key.getPort()));
		String fifoNameResponses = String.format("fifo/responses.%d", String.valueOf(key.getPort()));
		Runtime.getRuntime().exec("mkfifo " + fifoNameRequests);
		Runtime.getRuntime().exec("mkfifo " + fifoNameResponses);
		MessagePipe msgPipe = new FifoPipe(key, fifoNameRequests, fifoNameResponses);
		pipesToClients.put(key, msgPipe);
		RequestSubmitter requestSubmitter = new RequestSubmitter(this, msgPipe);
		requestSubmitter.setDaemon(true);
		requestSubmitter.setName(String.format("FifoRequestReader: %s", key.toString()));
		requestSubmitter.start();
		return requestSubmitter;
	}

	public Object getResponseTransmitterLock() {
		return responseTransmitterLock;
	}

	public void setResponseTransmitterLock(Object reponseTransmitterLock) {
		this.responseTransmitterLock = reponseTransmitterLock;
	}

	public ConcurrentMap<PipeKey, MessagePipe> getPipesToClients() {
		return pipesToClients;
	}

	public ConcurrentMap<MessageKey, Future<String>> getFuturesTable() {
		return futuresTable;
	}

	public MyThreadPoolExecutor getThreadPoolExecutor() {
		return threadPoolExecutor;
	}

}
