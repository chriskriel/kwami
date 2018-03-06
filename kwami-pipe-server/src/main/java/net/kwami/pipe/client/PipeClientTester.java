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
		String testName;
		PipeClient client;

		public MessageSenderThread(PipeClient client, int clientThreadNum, int threadNum, String testName) {
			super();
			this.threadNum = threadNum;
			this.client = client;
			this.clientThreadNum = clientThreadNum;
			this.testName = testName;
		}

		public void run() {
			String threadName = String.format("%s-Thread-%s-%s", testName, clientThreadNum, threadNum);
			Thread.currentThread().setName(threadName);
			try {
				sendRequest(threadName, 0);
//				Thread.sleep(3000);
				for (int i = 1; i < 5000; i++) {
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
	private String testName;

	public PipeClientTester(int threadNum, String testName) {
		this.threadNum = threadNum;
		this.testName = testName;
	}

	public static void main(String[] args) throws Exception {
		String testName = "test";
		if (args.length == 1)
			testName = args[0];
		
		long start = System.currentTimeMillis();
		Thread[] threads = new Thread[1];
		for (int i = 0; i < threads.length; i++) {
			threads[i] = new PipeClientTester(i, testName);
		}
		for (int i = 0; i < threads.length; i++) {
			threads[i].run();
		}
//		for (int i = 0; i < threads.length; i++) {
//			threads[i].join();
//		}
		System.out.printf("timing %dms", System.currentTimeMillis() - start);
		logger.debug("D O N E !");
	}

	@Override
	public void run() {
		try {
			String remoteHost = InetAddress.getByName(RemoteEndpoint.MACHINE_ADDRESS).getHostAddress();
			InetSocketAddress remoteSocketAddress = new InetSocketAddress(remoteHost, 58080);
			RemoteEndpoint endpoint = new RemoteEndpoint(48080 + threadNum, remoteSocketAddress);
			PipeClient client = new PipeClient(endpoint, 50);
			try {
				Thread[] threads = new Thread[5];
				for (int i = 0; i < threads.length; i++) {
					threads[i] = new MessageSenderThread(client, threadNum, i, testName);
					threads[i].run();
				}
//				for (int i = 0; i < threads.length; i++) {
//					threads[i].join();
//				}
			} finally {
				client.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
