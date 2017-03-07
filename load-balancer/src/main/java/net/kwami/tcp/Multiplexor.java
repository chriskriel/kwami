package net.kwami.tcp;

import java.net.InetSocketAddress;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;

public class Multiplexor {
	public static final Map<String, Receiver> clients = new ConcurrentHashMap<>(1000);
	private String appName;
	private final Map<String, Thread> servers = new HashMap<>(10);
	private final Vector<Application> containers;
	
	public Multiplexor(String appName, Vector<Application> containers) {
		super();
		this.containers = containers;
	}

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
