package net.kwami.pipe.client;

import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.kwami.pipe.FifoPipe;
import net.kwami.pipe.Message;
import net.kwami.pipe.Message.Status;
import net.kwami.pipe.Pipe;
import net.kwami.pipe.RemoteEndpoint;
import net.kwami.pipe.TcpPipe;
import net.kwami.pipe.server.Command;
import net.kwami.pipe.server.Command.Cmd;
import net.kwami.utils.Configurator;
import net.kwami.utils.MyProperties;

public class PipeClient implements AutoCloseable {
	private static final Logger LOGGER = LogManager.getLogger();
	public final AtomicLong nextMsgId = new AtomicLong();
	private final BlockingQueue<Long> transmitQueue;
	private final ConcurrentMap<Long, Message> outstandingRequests = new ConcurrentHashMap<>();
	private final int maxOutstanding;
	private Pipe pipe;
	private ResponseReader responseReader;
	private RequestTransmitter requestTransmitter;
	private Boolean isClosed = false;

	public PipeClient(RemoteEndpoint remoteEndpoint) throws Exception {
		super();
		ClientConfig config = Configurator.get(ClientConfig.class);
		this.maxOutstanding = config.getMaxOutstanding();
		transmitQueue = new ArrayBlockingQueue<>(config.getMaxTransmitQueue());
		createMessagePipe(remoteEndpoint);
		createClientThreads();
	}

	@Override
	public void close() throws Exception {
		LOGGER.info("{}", pipe.getRemoteEndpoint().toString());
		isClosed = true;
		// When auto-closing the responseReader is active and we have a MessagePipe,
		// so notify the server to reclaim the pipe. The other scenario is when the
		// server sent a command to close the pipe, then the ResponseReader will
		// no longer be running.
		if (pipe != null && responseReader != null) {
			Message msg = new Message(0, Pipe.END_OF_STREAM);
			pipe.write(msg);
			pipe.close();
		}
		if (requestTransmitter != null)
			requestTransmitter.terminate();
		if (responseReader != null)
			responseReader.terminate();
	}

	public String sendRequest(String data, long timeoutMs) throws Exception {
		if (isClosed)
			throw new Exception("PipeClient has been closed");
		if (outstandingRequests.size() > maxOutstanding)
			throw new Exception(String.format("outstanding requests reached maximum of %d", maxOutstanding));
		long msgId = nextMsgId.incrementAndGet();
		Message msg = new Message(msgId, data);
		try {
			long queueWaitMs = timeoutMs / 2;
			outstandingRequests.put(msgId, msg);
			boolean successFul = transmitQueue.offer(msgId, queueWaitMs, TimeUnit.MILLISECONDS);
			if (!successFul) {
				outstandingRequests.remove(msgId);
				throw new TimeoutException(String.format("%s: failed to add message to transmit queue after %dms",
						pipe.getRemoteEndpoint().toString(), queueWaitMs));
			}
			msg.setStatus(Status.WAIT);
			synchronized (msg) {
				if (msg.getStatus() != Message.Status.DONE)
					msg.wait(timeoutMs);
			}
			if (msg.getStatus() != Status.DONE) {
				outstandingRequests.remove(msgId);
				throw new TimeoutException(
						String.format("%s, msgId %d waiting for response from server on request '%s'",
								pipe.getRemoteEndpoint().toString(), msgId, data));
			}
			return msg.getData();
		} finally {
			outstandingRequests.remove(msgId);
		}
	}

	private void createClientThreads() throws Exception {
		requestTransmitter = new RequestTransmitter(this);
		responseReader = new ResponseReader(this);
		requestTransmitter.setDaemon(true);
		requestTransmitter.setName(String.format("ReqXmtr%s", pipe.getRemoteEndpoint().toString()));
		requestTransmitter.start();
		responseReader.setDaemon(true);
		responseReader.setName(String.format("RespRdr%s", pipe.getRemoteEndpoint().toString()));
		responseReader.start();
	}

	private void createMessagePipe(RemoteEndpoint remoteEndpoint) throws Exception {
		SocketChannel socketChannel = SocketChannel.open();
		socketChannel.connect(remoteEndpoint.getSocketAddress());
		Command cmd = new Command(Cmd.CONNECT);
		ByteBuffer commandBuffer = ByteBuffer.allocate(1024);
		commandBuffer.put(cmd.getBytes());
		commandBuffer.flip();
		socketChannel.write(commandBuffer);
		commandBuffer.clear();
		socketChannel.read(commandBuffer);
		cmd = Command.fromBytes(commandBuffer.array(), commandBuffer.position());
		String protocol = null;
		MyProperties parameters = cmd.getParameters();
		if (parameters != null) {
			protocol = parameters.getProperty("protocol");
		}
		if (protocol == null)
			throw new Exception(String.format("Could not determine protocol for Pipe %s", remoteEndpoint.toString()));
		if (protocol.equals("TCP"))
			pipe = new TcpPipe(remoteEndpoint, socketChannel);
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
			pipe = new FifoPipe(remoteEndpoint, readPath, writePath);
		} else
			throw new Exception(
					String.format("Unknown protocol of '%s' returned from %s", protocol, remoteEndpoint.toString()));
		return;
	}

	public ResponseReader getResponseReader() {
		return responseReader;
	}

	void setResponseReader(ResponseReader responseReader) {
		this.responseReader = responseReader;
	}

	public RequestTransmitter getRequestTransmitter() {
		return requestTransmitter;
	}

	void setRequestTransmitter(RequestTransmitter requestTransmitter) {
		this.requestTransmitter = requestTransmitter;
	}

	public BlockingQueue<Long> getTransmitQueue() {
		return transmitQueue;
	}

	public Pipe getPipe() {
		return pipe;
	}

	public ConcurrentMap<Long, Message> getOutstandingRequests() {
		return outstandingRequests;
	}

}
