package net.kwami.pipe.server;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.util.Map.Entry;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import net.kwami.pipe.Message;
import net.kwami.pipe.Pipe;
import net.kwami.utils.MyLogger;

public class ResponseTransmitter extends ManagedThread {
	private static final MyLogger logger = new MyLogger(ResponseTransmitter.class);
	private final ByteBuffer workBuffer;
	private final PipeServer server;
	private final long idleSleepTime;

	public ResponseTransmitter(PipeServer server, long idleSleepTime) {
		super();
		this.server = server;
		workBuffer = ByteBuffer.allocate(Short.MAX_VALUE);
		this.idleSleepTime = idleSleepTime;
	}

	@Override
	public void run() {
		logger.info("Starting");
		int noneDoneCnt = 0;
		try {
			while (mustRun) {
				if (mustBlock)
					try {
						synchronized (this) {
							this.wait();
						}
					} catch (InterruptedException e) {
					}
				Pipe messagePipe = null;
				try {
					int transmitCnt = 0;
					for (Entry<CallableMessage, Future<String>> entry : server.getExecutingRequests().entrySet()) {
						Future<String> future = entry.getValue();
						if (future.isDone()) {
							noneDoneCnt = 0;
							transmitCnt++;
							CallableMessage messageOrigin = entry.getKey();
							messagePipe = messageOrigin.getPipe();
							Message response = new Message(messageOrigin.getMsg().getId(), null);
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
					if (transmitCnt == 0) {
						synchronized (server.getResponseTransmitterLock()) {
							if (++noneDoneCnt % 5 == 0)
								server.getResponseTransmitterLock().wait(idleSleepTime);
							else
								server.getResponseTransmitterLock().wait(idleSleepTime / 10);
						}
					}
				} catch (InterruptedException e) {
					logger.info("interrupted, continuing");
					continue;
				} catch (IOException e) {
					if (e instanceof ClosedChannelException || e.toString().contains(Pipe.END_OF_STREAM)) {
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
			logger.info("Stopping");
		}

	}

}
