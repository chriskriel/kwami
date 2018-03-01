package net.kwami.pipe.client;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousCloseException;

import net.kwami.pipe.Message;
import net.kwami.pipe.MessagePipe;
import net.kwami.pipe.server.ManagedThread;
import net.kwami.utils.MyLogger;

public class ResponseReader extends ManagedThread {
	private static final MyLogger logger = new MyLogger(ResponseReader.class);
	private PipeClient context;
	private final ByteBuffer workBuffer;

	public ResponseReader(PipeClient context) {
		super();
		this.context = context;
		workBuffer = ByteBuffer.allocate(Short.MAX_VALUE);
	}

	@Override
	public void run() {
		logger.info("Starting");
		try {
			while (mustRun) {
				if (mustBlock)
					try {
						synchronized (this) {
							this.wait();
						}
					} catch (InterruptedException e) {
					}
				try {
					Message response = context.getMessagePipe().read(workBuffer);
					if (response.getData().equals(MessagePipe.END_OF_STREAM)) {
						try {
							context.setResponseReader(null);
							context.close();
						} catch (Exception e) {
						}
						break;
					}
					Message originalRequest = context.getOutstandingRequests().get(response.getId());
					originalRequest.setData(response.getData());
					originalRequest.setStatus(Message.Status.DONE);
					synchronized (originalRequest) {
						originalRequest.notify();
					}
				} catch (IOException e) {
					if (e instanceof AsynchronousCloseException) {
						logger.info("%s was closed by another thread, terminating",
								context.getMessagePipe().getRemoteEndpoint().toString());
						break;
					}
					if (e.toString().contains(MessagePipe.END_OF_STREAM)) {
						logger.info("%s was closed by the server, terminating",
								context.getMessagePipe().getRemoteEndpoint().toString());
						break;
					}
					logger.error(e);
					break;
				}
			}
		} finally {
			logger.info("Stopping");
			context.setResponseReader(null);
		}
	}

}
