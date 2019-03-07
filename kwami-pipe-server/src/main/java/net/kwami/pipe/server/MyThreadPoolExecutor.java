package net.kwami.pipe.server;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class MyThreadPoolExecutor extends ThreadPoolExecutor {
	private static final Logger LOGGER = LogManager.getLogger(MyThreadPoolExecutor.class);

	public MyThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime,
			TimeUnit unit, BlockingQueue<Runnable> workQueue) {
		super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue);
	}

	@Override
	protected void afterExecute(Runnable r, Throwable t) {
		super.afterExecute(r, t);
		CallableMessage callableMessage = null;
		if (t == null && r instanceof Future<?>) {
			try {
				Object result = ((Future<?>) r).get();
				callableMessage = (CallableMessage) result;
				callableMessage.getPipe().write(callableMessage.getMsg());
			} catch (CancellationException ce) {
				t = ce;
			} catch (ExecutionException ee) {
				t = ee.getCause();
			} catch (InterruptedException ie) {
				Thread.currentThread().interrupt(); // ignore/reset
			} catch (Exception e) {
				t = e;
			}
		}
		if (t != null)
			LOGGER.error("", t);
	}

}
