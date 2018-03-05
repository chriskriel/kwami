package net.kwami.pipe.server;

import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;

import net.kwami.pipe.FifoPipe;
import net.kwami.pipe.Message;
import net.kwami.pipe.Pipe;
import net.kwami.utils.Configurator;
import net.kwami.utils.MyLogger;

public class RequestReader extends ManagedThread {
	private static final MyLogger logger = new MyLogger(RequestReader.class);
	private final PipeServer server;
	private final Pipe pipe;
	private final ByteBuffer workBuffer;
	private final Class<MyCallable> callableClass;
	private int[] fifoIndexes;

	@SuppressWarnings("unchecked")
	public RequestReader(PipeServer server, Pipe pipe) throws Exception {
		this.server = server;
		this.pipe = pipe;
		ServerConfig config = Configurator.get(ServerConfig.class);
		callableClass = (Class<MyCallable>) Class.forName(config.getCallableImplementation());
		workBuffer = ByteBuffer.allocate(Short.MAX_VALUE);
	}

	@Override
	public void run() {
		logger.info("Starting");
		try {
			while (mustRun) {
				if (mustBlock)
					try {
						this.wait();
					} catch (InterruptedException e) {
					}
				try {
					Message request = pipe.read(workBuffer);
					if (request.getData().equals(Pipe.END_OF_STREAM))
						break;
					MyCallable callable = callableClass.newInstance();
					callable.setParameter(new CallableMessage(request, pipe));
					server.getThreadPoolExecutor().submit(callable);
				} catch (Exception e) {
					if (e instanceof ClosedChannelException) {
						logger.info("%s was closed, terminating", pipe.getRemoteEndpoint().toString());
						break;
					}
					if (e.toString().contains(Pipe.END_OF_STREAM)) {
						logger.info("%s was closed by the client, terminating",
								pipe.getRemoteEndpoint().toString());
						break;
					}
					logger.error(e);
					break;
				}
			}
		} finally {
			logger.info("Stopping");
			try {
				if (pipe != null) {
					if (pipe instanceof FifoPipe) {
						Message msg = new Message(0, Pipe.END_OF_STREAM);
						pipe.write(workBuffer, msg);
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

	public void setFifoIndexes(int[] fifoIndexes) {
		this.fifoIndexes = fifoIndexes;
	}

}
