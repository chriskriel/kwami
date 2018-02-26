package net.kwami.pipe.server;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public class TcpPipe extends MessagePipe {
	private final SocketChannel socketChannel;

	/**
	 * This constructor will keep a local reference to the SocketChannel.
	 * 
	 * @param socketChannel
	 *            The SocketChannel that must be used for communications.
	 */
	public TcpPipe(final PipeKey pipeKey, final SocketChannel socketChannel) {
		super();
		this.socketChannel = socketChannel;
		setPipeKey(pipeKey);
	}

	@Override
	public void close() throws Exception {
		socketChannel.close();
	}

	/**
	 * Sends a Message to the remote end-point
	 * 
	 * @param workBuffer
	 *            An empty buffer that the caller must provide for temporary use
	 *            during the execution of this method. It must be large enough for
	 *            the biggest message that may be sent.
	 * @param message
	 *            The zacobcx.ppfe.container.Message that must be sent.
	 * @throws IOException
	 *             NIO exceptions are simply percolated up the stack.
	 */
	@Override
	public void write(final ByteBuffer workBuffer, final Message message) throws IOException {
		prepareWriteBuffer(workBuffer, message);
		socketChannel.write(workBuffer);
	}

	@Override
	protected void readFully(final ByteBuffer workBuffer) throws IOException {
		while (workBuffer.position() != workBuffer.limit()) {
			if (socketChannel.read(workBuffer) < 0)
				throw new IOException(MessagePipe.END_OF_STREAM);
		}
	}

}
