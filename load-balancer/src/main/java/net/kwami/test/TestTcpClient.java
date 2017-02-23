package net.kwami.test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.SocketChannel;

public class TestTcpClient {

	public static void main(String[] args) {
		try {
			File outputFile = new File("/home/chris/git/kwami/general/load-balancer/src/test/resources/new.pdf");
			outputFile.createNewFile();
			try (SocketChannel socketChannel = SocketChannel.open(new InetSocketAddress(8000));
					FileInputStream fileInput = new FileInputStream(
							"/home/chris/git/kwami/general/load-balancer/src/test/resources/JavaPlatform-javanut5-chp-5.pdf");
					FileOutputStream fileOutput = new FileOutputStream(outputFile);) {
				System.out.println("TestTcpClient connected to TestTcpLoadBalancer @ port 8000");
				ByteBuffer sendBuffer = ByteBuffer.allocateDirect(8192);
				ByteBuffer recvBuffer = ByteBuffer.allocateDirect(8192);
				FileChannel fileReadChannel = fileInput.getChannel();
				FileChannel fileWriteChannel = fileOutput.getChannel();
				boolean mustReadFile = true, mustReadSocket = true;
				int fileBytesRead, fileBytesWritten, socketBytesRead, socketBytesWritten;
				while (mustReadFile || mustReadSocket) {
					fileBytesRead = 0;
					fileBytesWritten = 0;
					socketBytesRead = 0;
					socketBytesWritten = 0;
					if (mustReadFile) {
						fileBytesRead = fileReadChannel.read(sendBuffer);
						if (fileBytesRead == -1)
							mustReadFile = false;
						else {
							sendBuffer.flip();
							socketBytesWritten = socketChannel.write(sendBuffer);
							sendBuffer.compact();
						}
					}
					if (mustReadSocket) {
						socketBytesRead = socketChannel.read(recvBuffer);
						if (socketBytesRead == -1)
							mustReadSocket = false;
						else {
							recvBuffer.flip();
							fileBytesWritten = fileWriteChannel.write(recvBuffer);
							recvBuffer.compact();
						}
					}
					System.out.println(String.format(
							"fileBytesRead=%d,socketBytesWritten=%d,socketBytesRead=%d,fileBytesWritten=%d,mustReadFile=%b,mustReadSocket=%b",
							fileBytesRead, socketBytesWritten, socketBytesRead, fileBytesWritten, mustReadFile,
							mustReadSocket));
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
