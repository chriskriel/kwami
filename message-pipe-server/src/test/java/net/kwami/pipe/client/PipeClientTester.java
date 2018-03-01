package net.kwami.pipe.client;

import java.net.InetAddress;
import java.net.InetSocketAddress;

import net.kwami.pipe.RemoteEndpoint;

public class PipeClientTester extends Thread {
	static class MessageSenderThread extends Thread {
		int threadNum;
		int clientThreadNum;
		PipeClient client;

		public MessageSenderThread(PipeClient client, int clientThreadNum, int threadNum) {
			super();
			this.threadNum = threadNum;
			this.client = client;
			this.clientThreadNum = clientThreadNum;
		}

		public void run() {
			String threadName = String.format("Thread-%s-%s", clientThreadNum, threadNum);
			Thread.currentThread().setName(threadName);
			try {
				for (int i = 1; i < 500; i++) {
					String s = client.sendRequest(threadName + " request-" + i, 10000000);
					System.out.println(threadName + " received: '" + s + "' for request-" + i);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private int threadNum;

	public PipeClientTester(int threadNum) {
		this.threadNum = threadNum;
	}

	public static void main(String[] args) throws Exception {
		Thread[] threads = new Thread[5];
		for (int i = 0; i < threads.length; i++) {
			threads[i] = new PipeClientTester(i);
			threads[i].run();
		}
		for (int i = 0; i < threads.length; i++) {
			threads[i].join();
		}
	}

	@Override
	public void run() {
		try {
			Thread.currentThread().setName("PipeClientTesterThread-" + threadNum);
			String remoteHost = InetAddress.getByName(RemoteEndpoint.MACHINE_ADDRESS).getHostAddress();
			InetSocketAddress remoteSocketAddress = new InetSocketAddress(remoteHost, 58080);
			RemoteEndpoint endpoint = new RemoteEndpoint(48080 + threadNum, remoteSocketAddress);
			try (PipeClient client = new PipeClient(endpoint, 10)) {
				Thread[] threads = new Thread[5];
				for (int i = 0; i < threads.length; i++) {
					threads[i] = new MessageSenderThread(client, threadNum, i);
					threads[i].run();
				}
				for (int i = 0; i < threads.length; i++) {
					threads[i].join();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
