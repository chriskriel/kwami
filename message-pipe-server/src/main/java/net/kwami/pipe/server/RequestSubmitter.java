package net.kwami.pipe.server;

import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.util.concurrent.Future;

import net.kwami.pipe.Message;
import net.kwami.pipe.MessagePipe;
import net.kwami.utils.Configurator;
import net.kwami.utils.MyLogger;

public class RequestSubmitter extends ManagedThread {
	private static final MyLogger logger = new MyLogger(RequestSubmitter.class);
	private final PipeServer server;
	private final MessagePipe messagePipe;
	private final ByteBuffer workBuffer;
	private final Class<StringCallable> callableClass;

	@SuppressWarnings("unchecked")
	public RequestSubmitter(PipeServer server, MessagePipe messagePipe) throws Exception {
		this.server = server;
		this.messagePipe = messagePipe;
		ServerConfig config = Configurator.get(ServerConfig.class);
		callableClass = (Class<StringCallable>) Class.forName(config.getCallableImplementation());
		workBuffer = ByteBuffer.allocate(Short.MAX_VALUE);
	}

	@Override
	public void run() {
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
			try {
				messagePipe.close();
			} catch (Exception e) {
			}
		}
	}

}
