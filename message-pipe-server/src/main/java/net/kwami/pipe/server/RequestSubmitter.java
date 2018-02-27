package net.kwami.pipe.server;

import java.nio.ByteBuffer;
import java.util.concurrent.Future;

import net.kwami.pipe.Message;
import net.kwami.pipe.MessagePipe;
import net.kwami.utils.Configurator;

public class RequestSubmitter extends ManagedThread {
	private final Server server;
	private final MessagePipe messagePipe;
	private final ByteBuffer workBuffer;
	private final Class<StringCallable> callableClass;

	@SuppressWarnings("unchecked")
	public RequestSubmitter(Server server, MessagePipe messagePipe) throws Exception {
		this.server = server;
		this.messagePipe = messagePipe;
		ServerConfig config = Configurator.get(ServerConfig.class);
		callableClass = (Class<StringCallable>)Class.forName(config.getCallableImplementation());
		workBuffer = ByteBuffer.allocate(Short.MAX_VALUE);
	}

	@Override
	public void run() {
		while (mustRun) {
			if (mustBlock)
				try {
					this.wait();
				} catch (InterruptedException e) {
				}
			try {
				Message request = messagePipe.read(workBuffer);
				StringCallable container = callableClass.newInstance();
				container.setParameter(request.getData());
				Future<String> future = server.getThreadPoolExecutor().submit(container);
				MessageOrigin msgKey = new MessageOrigin(request.getId(), messagePipe);
				server.getExecutingRequests().put(msgKey, future);
			} catch (Exception e) {
				break;
			}
		}
	}

}
