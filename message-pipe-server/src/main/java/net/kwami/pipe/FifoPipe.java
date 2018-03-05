package net.kwami.pipe;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

public class FifoPipe extends Pipe {
	public static final String SERVER_READ_PATH_KEY = "readPath";
	public static final String SERVER_WRITE_PATH_KEY = "writePath";
	private FileChannel readChannel;
	private FileChannel writeChannel;

	/**
	 * Opens a FIFO (named-pipes) connector for reading and/or writing
	 * 
	 * @param remoteEndpoint
	 *            The IP address and port of the remote end-point.
	 * @param readPath
	 *            The path to a defined FIFO on the Linux file-system. If null then
	 *            reading from a FIFO will be disabled in this instance of the
	 *            FifoConnector.
	 * @param writePath
	 *            The path to a defined FIFO on the Linux file-system. If null then
	 *            writing to a FIFO will be disabled in this instance of the
	 *            FifoConnector.
	 * @throws IOException
	 *             Re-thrown from Java NIO. Also on END-OF-STREAM and when disabled
	 *             reading or writing.
	 */
	public FifoPipe(final RemoteEndpoint remoteEndpoint, final String readPath, final String writePath)
			throws IOException {
		super();
		setRemoteEndpoint(remoteEndpoint);
		if (readPath != null) {
			Path fifo = Paths.get(readPath);
			readChannel = FileChannel.open(fifo, StandardOpenOption.READ, StandardOpenOption.WRITE);
		}
		if (writePath != null) {
			Path fifo = Paths.get(writePath);
			writeChannel = FileChannel.open(fifo, StandardOpenOption.READ, StandardOpenOption.WRITE);
		}
	}

	@Override
	public void close() throws Exception {
		if (readChannel != null)
			readChannel.close();
		if (writeChannel != null)
			writeChannel.close();
	}

	/**
	 * Retrieves a Message from the FIFO stream.
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
	@Override
	public Message read(final ByteBuffer workBuffer) throws IOException {
		if (readChannel == null)
			throw new IOException(Pipe.READING_DISABLED);
		synchronized (readChannel) {
			return super.read(workBuffer);
		}
	}

	@Override
	protected void readFully(final ByteBuffer byteBuffer) throws IOException {
		while (byteBuffer.position() != byteBuffer.limit()) {
			if (readChannel.read(byteBuffer) < 0)
				throw new IOException(Pipe.END_OF_STREAM);
		}
	}

	/**
	 * Sends a Message to the remote end-point
	 * 
	 * @param workBuffer
	 *            An empty buffer that the caller must provide for temporary use
	 *            during the execution of this method. It must be large enough for
	 *            the biggest message that may be sent.
	 * @param msg
	 *            The zacobcx.ppfe.container.Message that must be sent.
	 * @throws IOException
	 *             NIO exceptions are simply percolated up the stack.
	 */
	@Override
	public void write(final ByteBuffer workBuffer, final Message msg) throws IOException {
		if (writeChannel == null)
			throw new IOException(Pipe.WRITING_DISABLED);
		synchronized (writeChannel) {
			prepareWriteBuffer(workBuffer, msg);
			writeChannel.write(workBuffer);
		}
	}

}
