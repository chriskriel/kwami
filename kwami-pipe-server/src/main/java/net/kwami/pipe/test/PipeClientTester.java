package net.kwami.pipe.test;

import java.net.InetAddress;
import java.net.UnknownHostException;

import net.kwami.pipe.RemoteEndpoint;
import net.kwami.pipe.client.PipeClient;
import net.kwami.pipe.client.TimeoutException;
import net.kwami.utils.MyLogger;

public class PipeClientTester extends Thread {
	private static final MyLogger logger = new MyLogger(PipeClientTester.class);

	static class MessageSenderThread extends Thread {
		int threadNum;
		int clientThreadNum;
		String testName;
		RemoteEndpoint endpoint;

		public MessageSenderThread(RemoteEndpoint endpoint, int clientThreadNum, int threadNum, String testName)
				throws Exception {
			super();
			this.endpoint = endpoint;
			this.threadNum = threadNum;
			this.clientThreadNum = clientThreadNum;
			this.testName = testName;
		}

		public void run() {
			logger.info("starting");
			String threadName = String.format("%s-Thread-%s-%s", testName, clientThreadNum, threadNum);
			Thread.currentThread().setName(threadName);
			try (PipeClient client = new PipeClient(endpoint)) {
				for (int i = 0; i < 5000; i++) {
					try {
						sendRequest(client, threadName, i);
					} catch (TimeoutException e) {
						System.out.printf("On %s: %s\n", threadName, e.toString());
						continue;
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		private void sendRequest(PipeClient client, String threadName, int i) throws Exception {
			long start = System.currentTimeMillis();
			String request = threadName + " request-" + i;
			String s = client.sendRequest(request, 200);
			long latency = System.currentTimeMillis() - start;
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
		System.setProperty("config.default.file.type", "json");
		String testName = "test";
		if (args.length == 1)
			testName = args[0];

		long start = System.currentTimeMillis();
		Thread[] threads = new Thread[1];
		for (int i = 0; i < threads.length; i++) {
			threads[i] = new PipeClientTester(i, testName);
		}
		for (int i = 0; i < threads.length; i++) {
			threads[i].start();
		}
		for (int i = 0; i < threads.length; i++) {
			threads[i].join();
		}
		System.out.printf("D O N E ! (timing %dms)\n", System.currentTimeMillis() - start);
	}

	@Override
	public void run() {
		try {
			String remoteHost = InetAddress.getByName(RemoteEndpoint.getMachineAddress()).getHostAddress();
			RemoteEndpoint endpoint = new RemoteEndpoint(remoteHost, 58080);
			try {
				Thread[] threads = new Thread[5];
				for (int i = 0; i < threads.length; i++) {
					threads[i] = new MessageSenderThread(endpoint, threadNum, i, testName);
					threads[i].start();
				}
				for (int i = 0; i < threads.length; i++) {
					threads[i].join();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
	}

}
