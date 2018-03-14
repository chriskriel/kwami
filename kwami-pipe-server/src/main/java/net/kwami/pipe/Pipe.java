package net.kwami.pipe;

import java.io.IOException;
import java.nio.ByteBuffer;

import net.kwami.utils.MyLogger;

public abstract class Pipe implements AutoCloseable {
	private static final MyLogger logger = new MyLogger(Pipe.class);
	public static final String END_OF_STREAM = "END-OF-STREAM";
	public static final String READING_DISABLED = "READING-DISABLED";
	public static final String WRITING_DISABLED = "WRITING-DISABLED";
	public static final String TCP_READY = "TCP-READY";
	private RemoteEndpoint remoteEndpoint;
	protected ByteBuffer readBuffer = ByteBuffer.allocate(Short.MAX_VALUE + 1024);
	protected ByteBuffer writeBuffer = ByteBuffer.allocate(Short.MAX_VALUE + 1024);

	public abstract void write(final Message message) throws IOException;

	protected abstract void readFully() throws IOException;

	protected void prepareWriteBuffer(final Message message) throws IOException {
		logger.trace(message.getData());
		writeBuffer.clear();
		writeBuffer.putLong(message.getId());
		byte[] dataBytes = message.getDataBytes();
		if (dataBytes.length > Short.MAX_VALUE)
			throw new IOException(String.format("data length is %d, max allowed is %d", dataBytes.length, Short.MAX_VALUE));
		writeBuffer.putInt(dataBytes.length);
		writeBuffer.put(dataBytes);
		writeBuffer.flip();
	}

	/**
	 * Retrieves a Message from the TCP stream.
	 * 
	 * @return The zacobcx.ppfe.container.Message that was sent by the remote
	 *         end-point.
	 * @throws IOException
	 *             NIO exceptions are simply percolated up the stack.
	 */
	public Message read() throws IOException {
		readBuffer.clear();
		readBuffer.limit(12);
		readFully();
		long id = readBuffer.getLong(0);
		int mlen = readBuffer.getInt(Long.BYTES);
		readBuffer.clear();
		readBuffer.limit(mlen);
		readFully();
		String data = new String(readBuffer.array(), 0, mlen);
		logger.trace(data);
		return new Message(id, data);
	}

	public RemoteEndpoint getRemoteEndpoint() {
		return remoteEndpoint;
	}

	public void setRemoteEndpoint(RemoteEndpoint pipeKey) {
		this.remoteEndpoint = pipeKey;
	}
}
