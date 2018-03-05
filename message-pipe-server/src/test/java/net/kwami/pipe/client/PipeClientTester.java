package net.kwami.pipe.client;

import java.net.InetAddress;
import java.net.InetSocketAddress;

import net.kwami.pipe.RemoteEndpoint;
import net.kwami.utils.MyLogger;

public class PipeClientTester extends Thread {
	private static final MyLogger logger = new MyLogger(PipeClientTester.class);
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
				sendRequest(threadName, 0);
				Thread.sleep(3000);
				for (int i = 1; i < 500; i++) {
					sendRequest(threadName, i);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		private void sendRequest(String threadName, int i) throws Exception {
			long start = System.currentTimeMillis();
			String request = threadName + " request-" + i;
			String s = client.sendRequest(request, 50);
			long latency = System.currentTimeMillis() - start;
//			logger.debug("%s received after %dms: '%s' for %s", threadName, latency, s, request);
			System.out.printf("%s received after %dms: '%s' for %s\n", threadName, latency, s, request);
			
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
		}
		for (int i = 0; i < threads.length; i++) {
			threads[i].run();
		}
//		for (int i = 0; i < threads.length; i++) {
//			threads[i].join();
//		}
		logger.debug("D O N E !");
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
//				for (int i = 0; i < threads.length; i++) {
//					threads[i].join();
//				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
