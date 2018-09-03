package net.kwami.pipe;

import java.io.IOException;
import java.nio.channels.SocketChannel;

public class TcpPipe extends Pipe {
	private final SocketChannel socketChannel;

	/**
	 * This constructor will keep a local reference to the SocketChannel.
	 * 
	 * @param remoteEndpoint
	 *            The IP address and port of the remote end-point.
	 * @param socketChannel
	 *            The SocketChannel that must be used for communications.
	 */
	public TcpPipe(final RemoteEndpoint remoteEndpoint, final SocketChannel socketChannel) {
		super();
		this.socketChannel = socketChannel;
		setRemoteEndpoint(remoteEndpoint);
	}

	@Override
	public void close() throws Exception {
		socketChannel.close();
	}

	/**
	 * Sends a Message to the remote end-point
	 * 
	 * @param message
	 *            The zacobcx.ppfe.container.Message that must be sent.
	 * @throws IOException
	 *             NIO exceptions are simply percolated up the stack.
	 */
	@Override
	public void write(final Message message) throws IOException {
		synchronized (writeBuffer) {
			prepareWriteBuffer(message);
			socketChannel.write(writeBuffer);
		}
	}

	@Override
	protected void readFully() throws IOException {
		while (readBuffer.position() != readBuffer.limit()) {
			if (socketChannel.read(readBuffer) < 0)
				throw new IOException(Pipe.END_OF_STREAM);
		}
	}

}
