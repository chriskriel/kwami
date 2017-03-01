package zacobcx.threadpool;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class ThreadPoolExecutorTester {

	private static final int IDLE_KEEP_ALIVE = 10;
	private static final int MAX_THREADS = 4;
	private static final int MIN_THREADS = 2;

	public static void main(String args[]) throws InterruptedException {
		MyRejectedExecutionHandler rejectionHandler = new MyRejectedExecutionHandler();
		ThreadFactory threadFactory = Executors.defaultThreadFactory();
		ThreadPoolExecutor executorPool = new ThreadPoolExecutor(MIN_THREADS, MAX_THREADS, IDLE_KEEP_ALIVE, TimeUnit.SECONDS,
				new ArrayBlockingQueue<Runnable>(2), threadFactory, rejectionHandler);
		ThreadPoolMonitor monitor = new ThreadPoolMonitor(executorPool, 3);
		Thread monitorThread = new Thread(monitor);
		monitorThread.start();
		for (int i = 0; i < 10; i++) {
			executorPool.execute(new MyRunnable("cmd" + i));
		}
		monitor.shutdown();
		monitorThread.interrupt();
		monitorThread.join();
		executorPool.shutdown();
		executorPool.awaitTermination(10L, TimeUnit.SECONDS);
	}
}