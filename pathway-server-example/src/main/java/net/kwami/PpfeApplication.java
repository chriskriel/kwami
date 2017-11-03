package net.kwami;

import net.kwami.utils.MyLogger;

public abstract class PpfeApplication implements Runnable {
	private static MyLogger logger = new MyLogger(PpfeApplication.class);
	private PpfeContainer ppfeContainer = null;
	private int messageBufferSize = 8092;

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
			int bytesSent = ppfeContainer.sendReply(message);
			logger.debug("replied with " + bytesSent + " bytes");
		}
	}

	public abstract void process(PpfeMessage message);

	public PpfeContainer getService() {
		return ppfeContainer;
	}

	public void setService(PpfeContainer service) {
		this.ppfeContainer = service;
	}

	public int getMessageBufferSize() {
		return messageBufferSize;
	}

	public void setMessageBufferSize(int messageBufferSize) {
		this.messageBufferSize = messageBufferSize;
	}
}
