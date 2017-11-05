package net.kwami.ppfe;

import net.kwami.utils.MyLogger;

public abstract class PpfeApplication implements Runnable {
	private static MyLogger logger = new MyLogger(PpfeApplication.class);
	private PpfeContainer ppfeContainer = null;
	private int messageBufferSize = 8092;

	public PpfeApplication() {
		super();
	}

	public PpfeApplication(PpfeContainer server) throws Exception {
		super();
		this.ppfeContainer = server;
	}

	public void run() {
		PpfeMessage message = null;
		logger.info("Going to get data");
		while ((message = ppfeContainer.getRequest(messageBufferSize)) != null) {
			try {
				process(message);
			} catch (Exception e) {
				logger.error(e, e.toString());
			}
			Outcome outcome = ppfeContainer.sendReply(message);
			String msg = "Outcome on sending reply: %s";
			if (outcome.getReturnCode()==ReturnCode.SUCCESS) {
				logger.debug(msg, outcome.toString());
			} else {
				logger.error(msg, outcome.toString());			
			}
		}
	}

	public abstract void process(PpfeMessage message);

	public int getMessageBufferSize() {
		return messageBufferSize;
	}

	public void setMessageBufferSize(int messageBufferSize) {
		this.messageBufferSize = messageBufferSize;
	}
}
