package net.kwami.pipe;

import java.io.IOException;
import java.nio.ByteBuffer;

public abstract class MessagePipe implements AutoCloseable {
	public static final String END_OF_STREAM = "END-OF-STREAM";
	public static final String READING_DISABLED = "READING-DISABLED";
	public static final String WRITING_DISABLED = "WRITING-DISABLED";
	public static final String TCP_READY = "TCP-READY";
	private RemoteEndpoint remoteEndpoint;

	public abstract void write(final ByteBuffer workBuffer, final Message message) throws IOException;

	protected abstract void readFully(final ByteBuffer workBuffer) throws IOException;

	protected void prepareWriteBuffer(final ByteBuffer workBuffer, final Message message) {
		workBuffer.clear();
		workBuffer.putLong(message.getId());
		byte[] dataBytes = message.getDataBytes();
		workBuffer.putInt(dataBytes.length);
		workBuffer.put(dataBytes);
		workBuffer.flip();
	}

	/**
	 * Retrieves a Message from the TCP stream.
	 * 
	 * @param workBuffer
	 *            An empty buffer that the caller must provide for temporary use
	 *            during the execution of this method. It must be large enough for
	 *            the biggest message that may be received.
	 * @return The zacobcx.ppfe.container.Message that was sent by the remote
	 *         end-point.
	 * @throws IOException
	 *             NIO exceptions are simply percolated up the stack.
	 */
	public Message read(final ByteBuffer workBuffer) throws IOException {
		workBuffer.clear();
		workBuffer.limit(12);
		readFully(workBuffer);
		long id = workBuffer.getLong(0);
		int mlen = workBuffer.getInt(Long.BYTES);
		workBuffer.clear();
		workBuffer.limit(mlen);
		readFully(workBuffer);
		String data = new String(workBuffer.array(), 0, mlen);
		return new Message(id, data);
	}

	public RemoteEndpoint getRemoteEndpoint() {
		return remoteEndpoint;
	}

	public void setRemoteEndpoint(RemoteEndpoint pipeKey) {
		this.remoteEndpoint = pipeKey;
	}
}
