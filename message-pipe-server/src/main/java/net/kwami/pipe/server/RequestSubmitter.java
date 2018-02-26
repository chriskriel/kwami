package net.kwami.pipe.server;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.Future;

public class RequestSubmitter extends ManagedThread {
	private final Server server;
	private final MessagePipe messagePipe;
	private final ByteBuffer workBuffer;

	public RequestSubmitter(Server server, MessagePipe messagePipe) throws IOException {
		super();
		this.server = server;
		this.messagePipe = messagePipe;
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
				DummyCallable container = new DummyCallable(request.getData());
				Future<String> future = server.getThreadPoolExecutor().submit(container);
				MessageKey msgKey = new MessageKey(request.getId(), messagePipe.getPipeKey());
				server.getFuturesTable().put(msgKey, future);
			} catch (IOException e) {
				break;
			}
		}
	}

}
