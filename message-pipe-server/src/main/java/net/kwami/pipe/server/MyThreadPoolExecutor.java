package net.kwami.pipe.server;

import java.nio.ByteBuffer;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import net.kwami.utils.MyLogger;

public class MyThreadPoolExecutor extends ThreadPoolExecutor {
	private static final MyLogger logger = new MyLogger(MyThreadPoolExecutor.class);
	private final ByteBuffer workBuffer = ByteBuffer.allocate(Short.MAX_VALUE + 2048);

	public MyThreadPoolExecutor(PipeServer server, int corePoolSize, int maximumPoolSize, long keepAliveTime,
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
				callableMessage.getPipe().write(workBuffer, callableMessage.getMsg());
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
			logger.error(t);
	}

}
