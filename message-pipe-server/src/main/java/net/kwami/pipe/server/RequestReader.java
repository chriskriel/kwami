package net.kwami.pipe.server;

import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.util.concurrent.Future;

import net.kwami.pipe.FifoPipe;
import net.kwami.pipe.Message;
import net.kwami.pipe.MessagePipe;
import net.kwami.utils.Configurator;
import net.kwami.utils.MyLogger;

public class RequestReader extends ManagedThread {
	private static final MyLogger logger = new MyLogger(RequestReader.class);
	private final PipeServer server;
	private final MessagePipe messagePipe;
	private final ByteBuffer workBuffer;
	private final Class<StringCallable> callableClass;
	private int[] fifoIndexes;

	@SuppressWarnings("unchecked")
	public RequestReader(PipeServer server, MessagePipe messagePipe) throws Exception {
		this.server = server;
		this.messagePipe = messagePipe;
		ServerConfig config = Configurator.get(ServerConfig.class);
		callableClass = (Class<StringCallable>) Class.forName(config.getCallableImplementation());
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
					Message request = messagePipe.read(workBuffer);
					if (request.getData().equals(MessagePipe.END_OF_STREAM))
						break;
					StringCallable container = callableClass.newInstance();
					container.setParameter(request.getData());
					Future<String> future = server.getThreadPoolExecutor().submit(container);
					MessageOrigin msgKey = new MessageOrigin(request.getId(), messagePipe);
					server.getExecutingRequests().put(msgKey, future);
				} catch (Exception e) {
					if (e instanceof ClosedChannelException) {
						logger.info("%s was closed, terminating", messagePipe.getRemoteEndpoint().toString());
						break;
					}
					if (e.toString().contains(MessagePipe.END_OF_STREAM)) {
						logger.info("%s was closed by the client, terminating",
								messagePipe.getRemoteEndpoint().toString());
						break;
					}
					logger.error(e);
					break;
				}
			}
		} finally {
			logger.info("Stopping");
			try {
				if (messagePipe != null) {
					if (messagePipe instanceof FifoPipe) {
						Message msg = new Message(0, MessagePipe.END_OF_STREAM);
						messagePipe.write(workBuffer, msg);
						Thread.sleep(10000);
						if (fifoIndexes != null)
							for (int i = 0; i < fifoIndexes.length; i++)
								PipeServer.freeFifo(fifoIndexes[i]);
					}
					messagePipe.close();
				}
			} catch (Exception e) {
			}
		}
	}

	public void setFifoIndexes(int[] fifoIndexes) {
		this.fifoIndexes = fifoIndexes;
	}

}
