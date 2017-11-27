package net.kwami.ppfe;

import net.kwami.utils.MyLogger;

public class RelayApplication extends PpfeApplication {
	private static MyLogger logger = new MyLogger(PpfeApplication.class);

	public RelayApplication() throws Exception {
		super();
	}

	@Override
	public void run() {
		PpfeRequest ppfeRequest = null;
		Object requestContext = null;
		PpfeResponse ppfeResponse = new PpfeResponse();
		logger.trace("get PpfeRequests");
		while ((ppfeRequest = getContainer().getRequest()) != null) {
			try {
				requestContext = ppfeRequest.getContext();
				ppfeResponse = getContainer().sendRequest("Sql", ppfeRequest);
			} catch (Exception e) {
				ppfeResponse.getOutcome().setReturnCode(ReturnCode.FAILURE);
				ppfeResponse.getOutcome().setMessage(e.toString());
				logger.error(e, e.toString());
			}
			ppfeRequest.setContext(requestContext);
			Outcome outcome = getContainer().sendReply(ppfeRequest.getContext(), ppfeResponse);
			String msg = "Outcome on sending reply: %s";
			if (outcome.getReturnCode() == ReturnCode.SUCCESS) {
				logger.trace(msg, outcome.toString());
			} else {
				logger.error(msg, outcome.toString());
			}
		}
	}
}
