package net.kwami.pipe.server;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Map.Entry;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import net.kwami.utils.MyLogger;

public class ResponseTransmitter extends ManagedThread {
	private static final MyLogger logger = new MyLogger(ResponseTransmitter.class);
	private final ByteBuffer workBuffer;
	private final Server server;

	public ResponseTransmitter(Server server) {
		super();
		this.server = server;
		workBuffer = ByteBuffer.allocate(Short.MAX_VALUE);
		this.server.setResponseTransmitterLock(new Object());
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
					int completedRequests = 0;
					for (Entry<MessageKey, Future<String>> entry : server.getFuturesTable().entrySet()) {
						Future<String> future = entry.getValue();
						if (future.isDone()) {
							completedRequests++;
							MessageKey key = entry.getKey();
							Message response = new Message(key.getMsgId(), null);
							try {
								response.setData(future.get());
							} catch (ExecutionException e) {
								String error = e.getCause() != null ? e.getCause().toString() : e.toString();
								response.setData("ERROR: " + error);
							}
							server.getFuturesTable().remove(key);
							MessagePipe pipe = server.getPipesToClients().get(key.getPipeKey());
							pipe.write(workBuffer, response);
						}
					}
					if (completedRequests == 0)
						server.getResponseTransmitterLock().wait(1000);
				} catch (InterruptedException e) {
					logger.error(e);
					continue;
				} catch (IOException e) {
					logger.error(e);
					break;
				}
			}
		} finally {
		}
	}

}
