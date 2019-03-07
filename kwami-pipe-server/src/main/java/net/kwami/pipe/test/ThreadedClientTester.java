package net.kwami.pipe.test;

import java.net.InetAddress;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.kwami.pipe.RemoteEndpoint;
import net.kwami.pipe.client.PipeClient;
import net.kwami.pipe.client.TimeoutException;

public class ThreadedClientTester extends Thread {
	private static final Logger LOGGER = LogManager.getLogger(ThreadedClientTester.class);

	static class MessageSenderThread extends Thread {
		int pipeNo;
		int threadNum;
		int msgsPerThread;
		private final PipeClient client;

		public MessageSenderThread(PipeClient client, int pipeNo, int threadNum, int msgsPerThread)
				throws Exception {
			super();
			this.client = client;
			this.threadNum = threadNum;
			this.pipeNo = pipeNo;
			this.msgsPerThread = msgsPerThread;
		}

		public void run() {
			LOGGER.info("starting");
			String threadName = String.format("%s-%s", pipeNo, threadNum);
			Thread.currentThread().setName(threadName);
			for (int i = 0; i < 5000; i++) {
				try {
					sendRequest(client, threadName, i);
				} catch (TimeoutException e) {
					LOGGER.info("On {}: {}", threadName, e.toString());
					continue;
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}

		private void sendRequest(PipeClient client, String threadName, int i) throws Exception {
			long start = System.currentTimeMillis();
			String request = threadName + " request-" + i;
			String s = client.sendRequest(request, 100);
			long latency = System.currentTimeMillis() - start;
			LOGGER.info("{} received after {}ms: '{}' for {}", threadName, latency, s, request);

		}
	}

	private final int pipeNo;
	private final int threadsPerPipe;
	private final int msgsPerThread;
	private final PipeClient client;

	public ThreadedClientTester(int pipeNo, RemoteEndpoint endpoint, int threadsPerPipe, int msgsPerThread) throws Exception {
		this.pipeNo = pipeNo;
		this.threadsPerPipe = threadsPerPipe;
		this.msgsPerThread = msgsPerThread;
		client = new PipeClient(endpoint);
	}

	public static void main(String[] args) throws Exception {
		System.setProperty("config.default.file.type", "json");
		int pipes = Integer.parseInt(args[0]);
		int threadsPerPipe = Integer.parseInt(args[1]);
		int msgsPerThread = Integer.parseInt(args[2]);
		long start = System.currentTimeMillis();
		String remoteHost = InetAddress.getByName(RemoteEndpoint.getMachineAddress()).getHostAddress();
		RemoteEndpoint endpoint = new RemoteEndpoint(remoteHost, 58080);
		Thread[] threads = new Thread[pipes];
		for (int i = 0; i < threads.length; i++) {
			threads[i] = new ThreadedClientTester(i, endpoint, threadsPerPipe, msgsPerThread);
		}
		for (int i = 0; i < threads.length; i++) {
			threads[i].start();
		}
		for (int i = 0; i < threads.length; i++) {
			threads[i].join();
		}
		LOGGER.info("D O N E ! (timing {}ms)", System.currentTimeMillis() - start);
	}

	@Override
	public void run() {
		try {
			Thread[] threads = new Thread[threadsPerPipe];
			for (int i = 0; i < threads.length; i++) {
				threads[i] = new MessageSenderThread(client, pipeNo, i, msgsPerThread);
				threads[i].start();
			}
			for (int i = 0; i < threads.length; i++) {
				threads[i].join();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
