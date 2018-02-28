package net.kwami.pipe.client;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import net.kwami.pipe.FifoPipe;
import net.kwami.pipe.Message;
import net.kwami.pipe.MessagePipe;
import net.kwami.pipe.RemoteEndpoint;
import net.kwami.pipe.TcpPipe;
import net.kwami.pipe.Message.Status;
import net.kwami.pipe.server.Command;
import net.kwami.pipe.server.Command.Cmd;
import net.kwami.utils.MyProperties;

public class PipeClient implements AutoCloseable {
	public static final ConcurrentMap<RemoteEndpoint, PipeClient> register = new ConcurrentHashMap<>();
	public static final AtomicLong nextMsgId = new AtomicLong();
	private final BlockingQueue<Long> transmitQueue;
	private final ConcurrentMap<Long, Message> outstandingRequests = new ConcurrentHashMap<>();
	private MessagePipe messagePipe;
	private ResponseReader responseReader;
	private RequestTransmitter requestTransmitter;

	public PipeClient(RemoteEndpoint remoteEndpoint, int maxTransmitQueueSize) throws Exception {
		super();
		transmitQueue = new ArrayBlockingQueue<>(maxTransmitQueueSize);
		createMessagePipe(remoteEndpoint);
		createClientThreads();
		register.put(remoteEndpoint, this);
	}

	@Override
	public void close() throws Exception {
		if (messagePipe != null) {
			ByteBuffer bb = ByteBuffer.allocate(128);
			Message msg = new Message(nextMsgId.incrementAndGet(), MessagePipe.END_OF_STREAM);
			messagePipe.write(bb, msg);
			Thread.sleep(1000);
			messagePipe.close();
		}
		if (requestTransmitter != null)
			requestTransmitter.terminate();
		if (responseReader != null)
			responseReader.terminate();
	}

	public String sendRequest(String data, long timeoutMs) throws Exception {
		long msgId = nextMsgId.incrementAndGet();
		Message msg = new Message(msgId, data);
		outstandingRequests.put(msgId, msg);
		try {
			long queueWaitMs = timeoutMs / 2;
			boolean successFul = transmitQueue.offer(msgId, queueWaitMs, TimeUnit.MILLISECONDS);
			if (!successFul)
				throw new Exception(String.format("%s: failed to add message to transmit queue after %dms",
						messagePipe.getRemoteEndpoint().toString(), queueWaitMs));
			msg.setStatus(Status.WAIT);
			synchronized (msg) {
				msg.wait(timeoutMs);
			}
			if (msg.getStatus() == Status.WAIT)
				throw new Exception(String.format("%s: request '%s' has timed out",
						messagePipe.getRemoteEndpoint().toString(), data));
			return msg.getData();
		} finally {
			outstandingRequests.remove(msgId);
		}
	}

	private synchronized void createClientThreads() throws Exception {
		requestTransmitter = new RequestTransmitter(this);
		responseReader = new ResponseReader(this);
		requestTransmitter.setDaemon(true);
		requestTransmitter.setName(String.format("RequestTransmitter: %s", messagePipe.getRemoteEndpoint().toString()));
		requestTransmitter.start();
		responseReader.setDaemon(true);
		responseReader.setName(String.format("ResponseReader: %s", messagePipe.getRemoteEndpoint().toString()));
		responseReader.start();
	}

	private void createMessagePipe(RemoteEndpoint remoteEndpoint) throws Exception {
		SocketChannel socketChannel = SocketChannel.open();
		SocketAddress socketAddress = new InetSocketAddress(remoteEndpoint.getRemoteHost(),
				remoteEndpoint.getRemotePort());
		socketChannel.connect(socketAddress);
		Command cmd = new Command(Cmd.CONNECT);
		ByteBuffer bb = ByteBuffer.allocate(128);
		bb.put(cmd.getBytes());
		bb.flip();
		socketChannel.write(bb);
		bb.clear();
		socketChannel.read(bb);
		cmd = Command.fromBytes(bb.array(), bb.position());
		String protocol = null;
		MyProperties parameters = cmd.getParameters();
		if (parameters != null) {
			protocol = parameters.getProperty("protocol");
		}
		if (protocol == null)
			throw new Exception(String.format("Could not determine protocol for Pipe %s", remoteEndpoint.toString()));
		if (protocol.equals("TCP"))
			messagePipe = new TcpPipe(remoteEndpoint, socketChannel);
		else if (protocol.equals("FIFO")) {
			socketChannel.close();
			String readPath = parameters.getProperty(FifoPipe.SERVER_WRITE_PATH_KEY);
			if (readPath == null)
				throw new Exception(String.format("for pipe %s the %s cannot be null", remoteEndpoint.toString(),
						FifoPipe.SERVER_WRITE_PATH_KEY));
			String writePath = parameters.getProperty(FifoPipe.SERVER_READ_PATH_KEY);
			if (writePath == null)
				throw new Exception(String.format("for pipe %s the %s cannot be null", remoteEndpoint.toString(),
						FifoPipe.SERVER_READ_PATH_KEY));
			messagePipe = new FifoPipe(remoteEndpoint, readPath, writePath);
		} else
			throw new Exception(String.format("Unknown protocol of '%s' returned from %s:%d", protocol,
					remoteEndpoint.getRemoteHost(), remoteEndpoint.getRemotePort()));
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

	public BlockingQueue<Long> getTransmitQueue() {
		return transmitQueue;
	}

	public MessagePipe getMessagePipe() {
		return messagePipe;
	}

	public ConcurrentMap<Long, Message> getOutstandingRequests() {
		return outstandingRequests;
	}

}
