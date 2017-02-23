package net.kwami.test;

import java.net.InetSocketAddress;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

import net.kwami.tcp.Transceiver;

public class TestTcpBalancer {

	public static void main(String[] args) {
		try (ServerSocketChannel server = ServerSocketChannel.open();) {
			server.socket().bind(new InetSocketAddress(8000));
			System.out.println("TcpLoadBalancer listening on port 8000");
			try (SocketChannel clientChannel = server.accept();
					SocketChannel serverChannel = SocketChannel.open(new InetSocketAddress(8001));) {
				System.out.println("TcpLoadBalancer connected to TestTcpServer on port 8001");
				Transceiver xceiver = new Transceiver(clientChannel, serverChannel);
				xceiver.setDaemon(true);
				xceiver.start();
				xceiver.join();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
