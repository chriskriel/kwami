package net.kwami;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

import net.kwami.utils.HexDumper;

public class TcpProxy extends Thread {
	private String proxyChannel;
	private int localPort;
	private String remoteHost;
	private int remotePort;

	public TcpProxy(String proxyChannel, int localPort, String remoteHost, int remotePort) {
		super();
		this.proxyChannel = proxyChannel;
		this.localPort = localPort;
		this.remoteHost = remoteHost;
		this.remotePort = remotePort;
	}

	@Override
	public void run() {
		HexDumper hexDumper = new HexDumper();
		try (ServerSocket ss = new ServerSocket(localPort)) {
			final byte[] request = new byte[4096];
			byte[] response = new byte[4096];
			while (true) {
				try (Socket localSocket = ss.accept();
						Socket remoteServer = new Socket(remoteHost, remotePort);
						final InputStream streamFromClient = localSocket.getInputStream();
						final OutputStream streamToServer = remoteServer.getOutputStream();
						final InputStream streamFromServer = remoteServer.getInputStream();
						final OutputStream streamToClient = localSocket.getOutputStream();
						final OutputStream streamToFile = new FileOutputStream(new File("capture-" + proxyChannel))) {
					Thread sendingThread = new Thread() {
						public void run() {
							HexDumper hexDumper = new HexDumper();
							int bytesRead;
							try {
								while ((bytesRead = streamFromClient.read(request)) != -1) {
									streamToServer.write(request, 0, bytesRead);
									synchronized (System.out) {
										System.out.println(Thread.currentThread().getName());
										System.out.println(hexDumper.buildHexDump(request, bytesRead));
										System.out.println();
									}
								}
								streamToServer.flush();
							} catch (IOException e) {
							}
						}
					};
					sendingThread.setDaemon(true);
					sendingThread.setName("sendingThread[" + proxyChannel + "]:");
					sendingThread.start();
					int bytesRead;
					try {
						while ((bytesRead = streamFromServer.read(response)) != -1) {
							streamToClient.write(response, 0, bytesRead);
							streamToFile.write(request, 0, bytesRead);
							synchronized (System.out) {
								System.out.println(Thread.currentThread().getName());
								System.out.println(hexDumper.buildHexDump(response, bytesRead));
								System.out.println();
							}
						}
						streamToClient.flush();
						streamToFile.flush();
					} catch (IOException e) {
					}
				} catch (IOException e) {
					System.err.print("Streams to and from proxy failed\n ");
					e.printStackTrace();
				}
			}
		} catch (IOException e) {
			System.err.print("ServerSocket open failed\n ");
			e.printStackTrace();
		}
	}

	public static void main(String[] args) throws IOException {
		int remotePort = 0, localPort = 0;
		String remoteHost = "localhost";
		if (args.length == 0) {
			System.err.println("Usage: net.kwami.TcpProxy <localPort:remoteHost:remotePort> ...");
			return;
		}
		for (String proxyChannel : args) {
			boolean valid = true;
			String[] addressParts = proxyChannel.split(":");
			if (addressParts.length != 3) {
				valid = false;
			}
			try {
				localPort = Integer.parseInt(addressParts[0]);
				remoteHost = addressParts[1];
				remotePort = Integer.parseInt(addressParts[2]);
			} catch (Throwable t) {
				t.printStackTrace();
				valid = false;
			}
			if (!valid) {
				System.err.printf("Expecting, for example, 80:remoteHost:80 but got %s (trying the next parameter).\n",
						proxyChannel);
				continue;
			}
			System.err.printf("Starting a proxy for %s\n", proxyChannel);
			TcpProxy proxy = new TcpProxy(proxyChannel, localPort, remoteHost, remotePort);
			proxy.setName("ReceivingThread[" + proxyChannel + "]:");
			proxy.setDaemon(true);
			proxy.start();
		}
		System.err.println("Hit enter to terminate");
		new InputStreamReader(System.in).read();
	}
}
