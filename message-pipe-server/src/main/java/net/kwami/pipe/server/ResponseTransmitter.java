package net.kwami.pipe.server;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.util.Map.Entry;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import net.kwami.pipe.Message;
import net.kwami.pipe.MessagePipe;
import net.kwami.utils.MyLogger;

public class ResponseTransmitter extends ManagedThread {
	private static final MyLogger logger = new MyLogger(ResponseTransmitter.class);
	private final ByteBuffer workBuffer;
	private final PipeServer server;

	public ResponseTransmitter(PipeServer server) {
		super();
		this.server = server;
		workBuffer = ByteBuffer.allocate(Short.MAX_VALUE);
	}

	@Override
	public void run() {
		try {
			while (mustRun) {
				if (mustBlock)
					try {
						synchronized (this) {
							this.wait();
						}
					} catch (InterruptedException e) {
					}
				MessagePipe messagePipe = null;
				try {
					int completedRequests = 0;
					for (Entry<MessageOrigin, Future<String>> entry : server.getExecutingRequests().entrySet()) {
						Future<String> future = entry.getValue();
						if (future.isDone()) {
							completedRequests++;
							MessageOrigin messageOrigin = entry.getKey();
							messagePipe = messageOrigin.getMessagePipe();
							Message response = new Message(messageOrigin.getMsgId(), null);
							try {
								response.setData(future.get());
							} catch (ExecutionException e) {
								String error = e.getCause() != null ? e.getCause().toString() : e.toString();
								response.setData("ERROR: " + error);
							}
							server.getExecutingRequests().remove(messageOrigin);
							messagePipe.write(workBuffer, response);
						}
					}
					if (completedRequests == 0) {
						synchronized (server.getResponseTransmitterLock()) {
							server.getResponseTransmitterLock().wait(1000);
						}
					}
				} catch (InterruptedException e) {
					logger.error(e);
					continue;
				} catch (IOException e) {
					if (e instanceof ClosedChannelException || e.toString().contains(MessagePipe.END_OF_STREAM)) {
						logger.info("%s was closed, terminating", messagePipe.getRemoteEndpoint().toString());
						try {
							messagePipe.close();
						} catch (Exception e1) {
							logger.error(e1);
						}
						continue;
					}
					logger.error(e);
					break;
				}
			}
		} finally {
		}

	}

}
