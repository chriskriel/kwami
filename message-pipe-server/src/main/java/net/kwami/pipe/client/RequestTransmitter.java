package net.kwami.pipe.client;

import java.io.IOException;
import java.nio.ByteBuffer;

import net.kwami.pipe.Message;
import net.kwami.pipe.server.ManagedThread;
import net.kwami.utils.MyLogger;

public class RequestTransmitter extends ManagedThread {
	private static final MyLogger logger = new MyLogger(RequestTransmitter.class);
	private PipeClient context;
	private final ByteBuffer workBuffer;

	public RequestTransmitter(PipeClient context) {
		super();
		this.context = context;
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
					long msgId = context.getTransmitQueue().take();
					Message msg = context.getOutstandingRequests().get(msgId);
					while (msg.getStatus() != Message.Status.WAIT)
						Thread.sleep(2);
					context.getMessagePipe().write(workBuffer, msg);
				} catch (InterruptedException e) {
					logger.error(e);
					continue;
				} catch (IOException e) {
					logger.error(e);
					break;
				}
			}
		} finally {
			context.setRequestTransmitter(null);
		}
	}
}
