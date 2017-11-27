package net.kwami.ppfe;

import net.kwami.utils.MyLogger;

public class RelayApplication extends PpfeApplication {
	private static MyLogger logger = new MyLogger(PpfeApplication.class);

	public RelayApplication() throws Exception {
		super();
	}

	@Override
	public void run() {
		PpfeMessage message = null;
		logger.trace("Going to get data");
		Object requestContext = null;
		while ((message = getContainer().getRequest()) != null) {
			try {
				requestContext = message.getContext();
				process(message);
			} catch (Exception e) {
				message.setData(null);
				message.getOutcome().setReturnCode(ReturnCode.FAILURE);
				message.getOutcome().setMessage(e.toString());
				logger.error(e, e.toString());
			}
			message.setContext(requestContext);
			Outcome outcome = getContainer().sendReply(message);
			String msg = "Outcome on sending reply: %s";
			if (outcome.getReturnCode() == ReturnCode.SUCCESS) {
				logger.trace(msg, outcome.toString());
			} else {
				logger.error(msg, outcome.toString());
			}
		}
	}

	private void process(PpfeMessage message) {
		PpfeMessage response = getContainer().sendRequest("Sql", message);
		message.setData(response.getData());
		message.setOutcome(response.getOutcome());
	}
}
