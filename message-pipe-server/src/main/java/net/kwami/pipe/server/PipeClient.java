package net.kwami.pipe.server;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

import net.kwami.pipe.server.Command.Cmd;
import net.kwami.pipe.server.Message.Status;
import net.kwami.utils.MyProperties;

public class PipeClient {
	public static final ConcurrentMap<PipeKey, PipeClient> register = new ConcurrentHashMap<>();
	private final PipeKey pipeKey;
	private final BlockingQueue<MessageKey> transmitQueue;
	private final ConcurrentMap<Long, Message> activeMessageList = new ConcurrentHashMap<>();
	private MessagePipe messagePipe;
	private ResponseReader responseReader;
	private RequestTransmitter requestTransmitter;

	public PipeClient(PipeKey pipeKey, int maxTransmitQueueSize) throws Exception {
		super();
		this.pipeKey = pipeKey;
		transmitQueue = new ArrayBlockingQueue<>(maxTransmitQueueSize);
		createMessagePipe(pipeKey);
		createClientThreads();
		register.put(pipeKey, this);
	}

	public String sendRequest(String data, long timeoutMs) throws Exception {
		MessageKey msgKey = new MessageKey(pipeKey);
		Message msg = new Message(msgKey.getMsgId(), data);
		activeMessageList.put(msgKey.getMsgId(), msg);
		try {
			long queueWaitMs = timeoutMs / 2;
			boolean successFul = transmitQueue.offer(msgKey, queueWaitMs, TimeUnit.MILLISECONDS);
			if (!successFul)
				throw new Exception(String.format("%s: failed to add message to transmit queue after %dms",
						pipeKey.toString(), queueWaitMs));
			msg.setStatus(Status.WAIT);
			msg.wait(timeoutMs);
			if (msg.getStatus() == Status.WAIT)
				throw new Exception(String.format("%s: request '%s' has timed out", pipeKey.toString(), data));
			return msg.getData();
		} finally {
			activeMessageList.remove(msgKey.getMsgId());
		}
	}

	private synchronized void createClientThreads() throws Exception {
		requestTransmitter = new RequestTransmitter(this);
		responseReader = new ResponseReader(this);
		requestTransmitter.setDaemon(true);
		requestTransmitter.setName(String.format("RequestTransmitter: %s", pipeKey.toString()));
		requestTransmitter.start();
		responseReader.setDaemon(true);
		responseReader.setName(String.format("ResponseReader: %s", pipeKey.toString()));
		responseReader.start();
	}

	private void createMessagePipe(PipeKey pipeKey) throws Exception {
		SocketChannel socketChannel = SocketChannel.open();
		SocketAddress socketAddress = new InetSocketAddress(pipeKey.getHost(), pipeKey.getPort());
		socketChannel.connect(socketAddress);
		Command cmd = new Command(Cmd.CONNECT);
		ByteBuffer bb = ByteBuffer.allocate(128);
		bb.put(cmd.getBytes());
		bb.flip();
		socketChannel.write(bb);
		bb.clear();
		socketChannel.read(bb);
		cmd = Command.fromBytes(bb.array());
		String protocol = null;
		MyProperties parameters = cmd.getParameters();
		if (parameters != null) {
			protocol = parameters.getProperty("protocol");
		}
		if (protocol == null)
			throw new Exception(String.format("Could not determine protocol for Pipe %s", pipeKey.toString()));
		if (protocol.equals("TCP"))
			messagePipe = new TcpPipe(pipeKey, socketChannel);
		else if (protocol.equals("FIFO")) {
			socketChannel.close();
			String readPath = parameters.getProperty(FifoPipe.READ_PATH_KEY);
			if (readPath == null)
				throw new Exception(
						String.format("for pipe %s the %s cannot be null", pipeKey.toString(), FifoPipe.READ_PATH_KEY));
			String writePath = parameters.getProperty(FifoPipe.READ_PATH_KEY);
			if (writePath == null)
				throw new Exception(String.format("for pipe %s the %s cannot be null", pipeKey.toString(),
						FifoPipe.WRITE_PATH_KEY));
			messagePipe = new FifoPipe(pipeKey, readPath, writePath);
		} else
			throw new Exception(String.format("Unknown protocol of '%s' returned from %s:%d", protocol,
					pipeKey.getHost(), pipeKey.getPort()));
		return;
	}

	public ResponseReader getResponseReader() {
		return responseReader;
	}

	public void setResponseReader(ResponseReader responseReader) {
		this.responseReader = responseReader;
	}

	public RequestTransmitter getRequestTransmitter() {
		return requestTransmitter;
	}

	public void setRequestTransmitter(RequestTransmitter requestTransmitter) {
		this.requestTransmitter = requestTransmitter;
	}

	public PipeKey getPipeKey() {
		return pipeKey;
	}

	public BlockingQueue<MessageKey> getTransmitQueue() {
		return transmitQueue;
	}

	public MessagePipe getMessagePipe() {
		return messagePipe;
	}

	public ConcurrentMap<Long, Message> getActiveMessageList() {
		return activeMessageList;
	}

}
