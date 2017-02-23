package net.kwami.tcp;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

/**
 * The Transceiver manages two byte streams. An request stream from a client
 * (for example a browser) to a designated back-end server and an response
 * stream from the server back to a client. The Transceiver is interposed
 * between the client and the server and thus manages two SocketChannels for
 * this purposes. One SocketChannel connecting upstream to the client and one
 * SocketChannel for downstream connection to the server.
 */
public class Transceiver extends Thread {
	private final SocketChannel upstreamChannel;
	private final ByteBuffer requestBuffer;
	private final ByteBuffer responseBuffer;
	private final SocketChannel downstreamChannel;
	private final Selector selector;
	private final SelectionKey upstreamReadKey;
	private final SelectionKey downstreamReadKey;
	private volatile boolean mustRun = true;

	public Transceiver(final SocketChannel upstream, final SocketChannel downstream) throws IOException {
		super();
		upstreamChannel = upstream;
		upstreamChannel.configureBlocking(false);
		downstreamChannel = downstream;
		downstreamChannel.configureBlocking(false);
		requestBuffer = ByteBuffer.allocateDirect(4096);
		responseBuffer = ByteBuffer.allocateDirect(4096);
		selector = Selector.open();
		upstreamReadKey = upstreamChannel.register(selector, SelectionKey.OP_READ);
		downstreamReadKey = downstreamChannel.register(selector, SelectionKey.OP_READ);
	}

	public void setMustRun(boolean mustRun) {
		this.mustRun = mustRun;
	}

	@Override
	public void run() {
		try {
			while (mustRun) {
				selector.select();
				Set<SelectionKey> keys = selector.selectedKeys();
				for (Iterator<SelectionKey> i = keys.iterator(); i.hasNext();) {
					SelectionKey key = i.next();
					i.remove();
					if (!key.isReadable())
						continue;
					int bytesRead = 0;
					if (key == upstreamReadKey) {
						bytesRead = upstreamChannel.read(requestBuffer);
						if (bytesRead == -1)
							mustRun = false;
						else {
							requestBuffer.flip();
							downstreamChannel.write(requestBuffer);
							requestBuffer.compact();
						}
					} else {
						bytesRead = downstreamChannel.read(responseBuffer);
						if (bytesRead == -1)
							mustRun = false;
						else {
							responseBuffer.flip();
							upstreamChannel.write(responseBuffer);
							responseBuffer.compact();
						}
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			upstreamReadKey.cancel();
			downstreamReadKey.cancel();
			try {
				upstreamChannel.close();
			} catch (IOException e) {
			}
			try {
				downstreamChannel.close();
			} catch (IOException e) {
			}
		}
	}

}
