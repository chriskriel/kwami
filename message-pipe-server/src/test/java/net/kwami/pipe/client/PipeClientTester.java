package net.kwami.pipe.client;

import java.net.InetAddress;
import java.net.InetSocketAddress;

import net.kwami.pipe.RemoteEndpoint;

public class PipeClientTester {

	public static void main(String[] args) {
		Thread.currentThread().setName("PipeClientTesterThread");
		try {
			String remoteHost = InetAddress.getByName(RemoteEndpoint.MACHINE_ADDRESS).getHostAddress();
			InetSocketAddress remoteSocketAddress = new InetSocketAddress(remoteHost, 58080);
			RemoteEndpoint endpoint = new RemoteEndpoint(48080, remoteSocketAddress);
			try (PipeClient client = new PipeClient(endpoint, 10)) {
				for (int i = 1; i < 20; i++) {
					String s = client.sendRequest("request:" + i, 10000000);
					System.out.println("received: " + s + ", expecting nr: " + String.valueOf(i));
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
