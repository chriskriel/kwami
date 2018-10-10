package net.kwami;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import net.kwami.utils.HexDumper;
import net.kwami.utils.MyLogger;

/**
 * A SendingThread reads from the local socket from the client and writes to the
 * remote socket of the parent.
 * 
 * @author chris kriel
 *
 */
public class SendingThread extends Thread {
	private static final MyLogger LOGGER = new MyLogger(SendingThread.class);
	private final InputStream streamFromClient;
	private final OutputStream streamToServer;
	private final HexDumper hexDumper = new HexDumper(52);

	public SendingThread(Socket localSocket, Socket remoteSocket) throws Exception {
		super();
		streamFromClient = localSocket.getInputStream();
		streamToServer = remoteSocket.getOutputStream();
	}

	public void run() {
		int bytesRead;
		final byte[] request = new byte[16*1024];
		try {
			while (true) {
				bytesRead = streamFromClient.read(request);
				if (bytesRead < 0)
					break;
				streamToServer.write(request, 0, bytesRead);
				LOGGER.info("\n" + hexDumper.buildHexDump(request, bytesRead).toString());
			}
			streamToServer.flush();
		} catch (IOException e) {
			LOGGER.error(e);
		}
		LOGGER.info("terminated");
	}

}
