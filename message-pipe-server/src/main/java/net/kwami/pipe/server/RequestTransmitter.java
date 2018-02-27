package net.kwami.pipe.server;

import java.io.IOException;
import java.nio.ByteBuffer;

import net.kwami.utils.MyLogger;

public class RequestTransmitter extends ManagedThread {
	private static final MyLogger logger = new MyLogger(RequestTransmitter.class);
	private Client context;
	private final ByteBuffer workBuffer;

	public RequestTransmitter(Client context) {
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
					MessageKey msgKey = context.getTransmitQueue().take();
					Message msg = context.getActiveMessageList().get(msgKey.getMsgId());
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
