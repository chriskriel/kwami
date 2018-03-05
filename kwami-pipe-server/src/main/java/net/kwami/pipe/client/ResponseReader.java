package net.kwami.pipe.client;

import java.io.IOException;
import java.nio.channels.ClosedChannelException;

import net.kwami.pipe.Message;
import net.kwami.pipe.Pipe;
import net.kwami.pipe.server.ManagedThread;
import net.kwami.utils.MyLogger;

public class ResponseReader extends ManagedThread {
	private static final MyLogger logger = new MyLogger(ResponseReader.class);
	private PipeClient context;

	public ResponseReader(PipeClient context) {
		super();
		this.context = context;
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
					Message response = context.getPipe().read();
					if (response.getData().equals(Pipe.END_OF_STREAM)) {
						try {
							context.setResponseReader(null);
							context.close();
						} catch (Exception e) {
						}
						break;
					}
					Message originalRequest = context.getOutstandingRequests().get(response.getId());
					// client may have timed-out and removed the original request
					if (originalRequest == null)
						continue;
					synchronized (originalRequest) {
						originalRequest.setData(response.getData());
						originalRequest.setStatus(Message.Status.DONE);
						originalRequest.notify();
					}
				} catch (IOException e) {
					if (e instanceof ClosedChannelException) {
						logger.info("%s was closed by another thread, terminating",
								context.getPipe().getRemoteEndpoint().toString());
						break;
					}
					if (e.toString().contains(Pipe.END_OF_STREAM)) {
						logger.info("%s was closed by the server, terminating",
								context.getPipe().getRemoteEndpoint().toString());
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
