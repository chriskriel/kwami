package net.kwami.pipe.server;

import java.io.IOException;
import java.nio.channels.ClosedChannelException;
import java.util.concurrent.RejectedExecutionException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.kwami.pipe.FifoPipe;
import net.kwami.pipe.Message;
import net.kwami.pipe.Pipe;

public class RequestReader extends ManagedThread {
	private static final Logger LOGGER = LogManager.getLogger(RequestReader.class);
	private final PipeServer server;
	private final Pipe pipe;
	private final Class<MyCallable> callableClass;
	private int[] fifoIndexes;

	public RequestReader(PipeServer server, Pipe pipe, Class<MyCallable> callableClass) throws Exception {
		this.server = server;
		this.pipe = pipe;
		this.callableClass = callableClass;
	}

	@Override
	public void run() {
		LOGGER.info("Starting");
		try {
			while (mustRun) {
				if (mustBlock)
					try {
						this.wait();
					} catch (InterruptedException e) {
					}
				Message msg = null;
				try {
					msg = pipe.read();
					if (msg.getData().equals(Pipe.END_OF_STREAM))
						break;
					MyCallable callable = callableClass.newInstance();
					callable.setParameter(new CallableMessage(msg, pipe));
					server.getThreadPoolExecutor().submit(callable);
				} catch (RejectedExecutionException e) {
					if (!sendExceptionToClient(msg, e))
						break;
				} catch (IOException e) {
					LOGGER.error("{}: {}", pipe.getRemoteEndpoint().toString(), e.toString());
					break;
				} catch (Exception e) {
					if (e instanceof ClosedChannelException) {
						LOGGER.info("{} was closed, terminating", pipe.getRemoteEndpoint().toString());
						break;
					}
					if (e.toString().contains(Pipe.END_OF_STREAM)) {
						LOGGER.info("{} was closed by the client, terminating", pipe.getRemoteEndpoint().toString());
						break;
					}
					if (!sendExceptionToClient(msg, e))
						break;
				}
			}
		} finally {
			LOGGER.info("Stopping");
			try {
				if (pipe != null) {
					if (pipe instanceof FifoPipe) {
						Message msg = new Message(0, Pipe.END_OF_STREAM);
						pipe.write(msg);
						Thread.sleep(10000);
						if (fifoIndexes != null)
							for (int i = 0; i < fifoIndexes.length; i++)
								PipeServer.freeFifo(fifoIndexes[i]);
					}
					pipe.close();
				}
			} catch (Exception e) {
			}
		}
	}

	private boolean sendExceptionToClient(Message msg, Exception e) {
		LOGGER.error("{}: {}", pipe.getRemoteEndpoint().toString(), e.toString());
		msg.setData(String.format("%s %s in %s", Pipe.EXCPTN_PRFX, e.getClass().getSimpleName(), this.getName()));
		try {
			pipe.write(msg);
			return true;
		} catch (IOException e1) {
			LOGGER.error("{}: {}", pipe.getRemoteEndpoint().toString(), e1.toString());
			return false;
		}
	}

	public void setFifoIndexes(int[] fifoIndexes) {
		this.fifoIndexes = fifoIndexes;
	}

}
