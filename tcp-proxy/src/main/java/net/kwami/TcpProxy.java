package net.kwami;

import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import net.kwami.utils.MyLogger;

public class TcpProxy extends Thread {
	private static final MyLogger LOGGER = new MyLogger(TcpProxy.class);
	private final String proxyChannel;
	private final String remoteHost;
	private final int remotePort;
	private final ServerSocket listenSocket;
	private final List<Socket> sockets = new ArrayList<>();

	public TcpProxy(String proxyChannel, int localPort, String remoteHost, int remotePort) throws Exception {
		super();
		LOGGER.info("Creating...");
		this.proxyChannel = proxyChannel;
		this.remoteHost = remoteHost;
		this.remotePort = remotePort;
		listenSocket = new ServerSocket(localPort);
	}

	@Override
	public void run() {
		LOGGER.info("Starting...");
		while (true) {
			try {
				Socket localSocket = listenSocket.accept();
				LOGGER.info("Accepted a connection");
				sockets.add(localSocket);
				Socket remoteSocket = new Socket(remoteHost, remotePort);
				sockets.add(remoteSocket);
				SendingThread sendingThread = new SendingThread(localSocket, remoteSocket);
				ReceivingThread receivingThread = new ReceivingThread(proxyChannel, remoteSocket, localSocket);
				sendingThread.setDaemon(true);
				sendingThread.setName("sendingThread[" + proxyChannel + "]:");
				sendingThread.start();
				Thread.sleep(1000);
				receivingThread.setDaemon(true);
				receivingThread.setName("receivingThread[" + proxyChannel + "]:");
				receivingThread.start();
			} catch (Exception e) {
				LOGGER.error(e, "Not listening anymore");
			}
		}
	}

	public void closeSockets() {
		LOGGER.info("");
		for (Socket socket : sockets)
			try {
				socket.close();
			} catch (Exception e) {
			}
	}

	public static void main(String[] args) throws Exception {
		int remotePort = 0, localPort = 0;
		String remoteHost = "localhost";
		List<TcpProxy> proxies = new ArrayList<>();
		if (args.length == 0) {
			LOGGER.error("Usage: net.kwami.TcpProxy <localPort:remoteHost:remotePort> ...");
			return;
		}
		for (String proxyChannel : args) {
			String[] addressParts = proxyChannel.split(":");
			try {
				if (addressParts.length != 3)
					throw new Exception();
				localPort = Integer.parseInt(addressParts[0]);
				remoteHost = addressParts[1];
				remotePort = Integer.parseInt(addressParts[2]);
			} catch (Throwable t) {
				LOGGER.error("Expecting, for example, 80:remoteHost:80 but got %s.\n", proxyChannel);
				continue;
			}
			TcpProxy proxy = new TcpProxy(proxyChannel, localPort, remoteHost, remotePort);
			proxies.add(proxy);
			proxy.setName("ProxyThread[" + proxyChannel + "]:");
			proxy.setDaemon(true);
			proxy.start();
		}
		System.out.println("Hit enter to terminate");
		new InputStreamReader(System.in).read();
		for (TcpProxy proxy : proxies)
			proxy.closeSockets();
		System.out.println("terminated normally");
	}
}
