package net.kwami.test;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

public class TestTcpServer {

	public static void main(String[] args) {
		ByteBuffer buffer = ByteBuffer.allocate(2048);
		try (ServerSocketChannel server = ServerSocketChannel.open();) {
			server.socket().bind(new InetSocketAddress(8001));
			try (SocketChannel client = server.accept();) {
				int bytesRead = 0, bytesWritten = 0;
				boolean mustRun = true;
				while (mustRun) {
					bytesRead = client.read(buffer);
					System.out.println(String.format("TestTcpServer read %d bytes from Tranceiver", bytesRead));
					if (bytesRead == -1) {
						mustRun = false;
						continue;
					}
					buffer.flip();
					bytesWritten = client.write(buffer);
					System.out.println(String.format("TestTcpServer wrote %d bytes to Tranceiver", bytesWritten));
					buffer.compact();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
