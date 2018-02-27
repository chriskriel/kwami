package net.kwami.pipe.server;

import java.io.IOException;
import java.nio.ByteBuffer;

import net.kwami.utils.MyLogger;

public class ResponseReader extends ManagedThread {
	private static final MyLogger logger = new MyLogger(ResponseReader.class);
	private Client context;
	private final ByteBuffer workBuffer;

	public ResponseReader(Client context) {
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
					Message response = context.getMessagePipe().read(workBuffer);
					Message original = context.getActiveMessageList().get(response.getId());
					original.setData(response.getData());
					original.setStatus(Message.Status.DONE);
					original.notify();
				} catch (IOException e) {
					logger.error(e);
					break;
				}
			}
		} finally {
			context.setResponseReader(null);			
		}
	}

}
