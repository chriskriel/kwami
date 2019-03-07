package net.kwami.pipe.client;

import java.io.IOException;
import java.nio.channels.AsynchronousCloseException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.kwami.pipe.Message;
import net.kwami.pipe.Pipe;
import net.kwami.pipe.server.ManagedThread;

public class RequestTransmitter extends ManagedThread {
	private static final Logger LOGGER = LogManager.getLogger(RequestTransmitter.class);
	private PipeClient context;

	public RequestTransmitter(PipeClient context) {
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
						this.wait();
					} catch (InterruptedException e) {
					}
				try {
					long msgId = context.getTransmitQueue().take();
					Message msg = context.getOutstandingRequests().get(msgId);
					// the client may have timed out and removed the message
					if (msg == null) {
						LOGGER.trace("msgId {} was a null message, outstanding: {}", msgId,
								context.getOutstandingRequests().size());
						continue;
					}
					while (msg.getStatus() != Message.Status.WAIT)
						Thread.sleep(2);
					context.getPipe().write(msg);
				} catch (InterruptedException e) {
					continue;
				} catch (IOException e) {
					if (e instanceof AsynchronousCloseException) {
						LOGGER.info("{} was closed by another thread, terminating",
								context.getPipe().getRemoteEndpoint().toString());
						break;
					}
					if (e.toString().contains(Pipe.END_OF_STREAM)) {
						LOGGER.info("{} was closed by the server, terminating",
								context.getPipe().getRemoteEndpoint().toString());
						break;
					}
				}
			}
		} finally {
			LOGGER.info("Stopping");
			context.setRequestTransmitter(null);
		}
	}
}
