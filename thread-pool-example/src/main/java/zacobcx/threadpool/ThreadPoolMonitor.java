package zacobcx.threadpool;

import java.util.concurrent.ThreadPoolExecutor;

public class ThreadPoolMonitor implements Runnable {

	private ThreadPoolExecutor executor;
	private long delayMs;
	private boolean mustRun = true;

	public ThreadPoolMonitor(ThreadPoolExecutor executor, long delaySecs) {
		this.executor = executor;
		this.delayMs = delaySecs * 1000;
	}

	public void shutdown() {
		this.mustRun = false;
	}

	@Override
	public void run() {
		while (mustRun) {
			System.out.println(
					String.format("[monitor] [%d/%d] Active: %d, Completed: %d, Task: %d, isShutdown: %s, isTerminated: %s",
							this.executor.getPoolSize(), this.executor.getCorePoolSize(), this.executor.getActiveCount(),
							this.executor.getCompletedTaskCount(), this.executor.getTaskCount(), this.executor.isShutdown(),
							this.executor.isTerminated()));
			try {
				Thread.sleep(delayMs);
			} catch (InterruptedException e) {
			}
		}
	}
}