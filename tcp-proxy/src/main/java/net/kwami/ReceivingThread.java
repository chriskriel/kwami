package net.kwami;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.net.Socket;
import java.nio.file.Paths;

import net.kwami.utils.HexDumper;
import net.kwami.utils.MyLogger;

public class ReceivingThread extends Thread {
	private static final MyLogger LOGGER = new MyLogger(ReceivingThread.class);
    private final InputStream streamFromServer;
    private final OutputStream streamToClient;
    private final HexDumper hexDumper = new HexDumper(52);
    private final RandomAccessFile captureFile;

    public ReceivingThread(String proxyChannel, Socket remoteSocket, Socket localSocket) throws Exception {
        super();
        streamFromServer = remoteSocket.getInputStream();
        streamToClient = localSocket.getOutputStream();
        String fileName = "capture_";
        fileName += String.join("_", proxyChannel.split(":"));
        File file = Paths.get(fileName).toFile();
        try {
            file.delete();
        } catch (Exception e) {
        }
        file.createNewFile();
        captureFile = new RandomAccessFile(fileName, "rw");
    }

    @Override
    public void run() {
        byte[] response = new byte[16*1024];
        int bytesRead = 0;
        int filePos = 0;
        try {
            while (true) {
                bytesRead = streamFromServer.read(response);
                if (bytesRead < 0)
                	break;
                streamToClient.write(response, 0, bytesRead);
                captureFile.seek(filePos);
                filePos += bytesRead;
                captureFile.write(response, 0, bytesRead);
                LOGGER.info("\n" + hexDumper.buildHexDump(response, bytesRead).toString());
            }
        } catch (IOException e) {
            LOGGER.error(e);
        } finally {
            try {
                captureFile.close();
            } catch (Exception e) {
            }
            try {
                streamToClient.flush();
            } catch (Exception e) {
            }
        }
		LOGGER.info("terminated");
    }
}
