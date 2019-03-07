package net.kwami.pipe.client;

import java.io.IOException;
import java.nio.channels.ClosedChannelException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.kwami.pipe.Message;
import net.kwami.pipe.Pipe;
import net.kwami.pipe.server.ManagedThread;

public class ResponseReader extends ManagedThread {
	private static final Logger LOGGER = LogManager.getLogger(ResponseReader.class);
	private PipeClient context;

	public ResponseReader(PipeClient context) {
		super();
		this.context = context;
	}

	@Override
	public void run() {
		LOGGER.info("Starting");
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
					if (originalRequest == null) {
						LOGGER.error("no outstanding request for response id {}", response.getId());
						continue;
					}
					synchronized (originalRequest) {
						originalRequest.setData(response.getData());
						originalRequest.setStatus(Message.Status.DONE);
						originalRequest.notify();
					}
				} catch (IOException e) {
					if (e instanceof ClosedChannelException) {
						LOGGER.info("{} was closed by another thread, terminating",
								context.getPipe().getRemoteEndpoint().toString());
						break;
					}
					if (e.toString().contains(Pipe.END_OF_STREAM)) {
						LOGGER.info("{} was closed by the server, terminating",
								context.getPipe().getRemoteEndpoint().toString());
						break;
					}
					LOGGER.error(e);
					break;
				}
			}
		} finally {
			LOGGER.info("Stopping");
			context.setResponseReader(null);
		}
	}

}
