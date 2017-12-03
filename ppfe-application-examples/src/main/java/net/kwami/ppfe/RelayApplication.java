package net.kwami.ppfe;

import net.kwami.utils.MyLogger;

public class RelayApplication extends PpfeApplication {
	private static MyLogger logger = new MyLogger(PpfeApplication.class);
	private final PpfeRequest ppfeRequest = new PpfeRequest();
	private final PpfeResponse ppfeResponse = new PpfeResponse();
	private Outcome outcome = new Outcome();

	public RelayApplication() throws Exception {
		super();
	}

	@Override
	public void run() {
		PpfeContainer container = getContainer();
		Object requestContext = null;
		while (container.getRequest(ppfeRequest)) {
			logger.trace("ppfeRequest=%s", ppfeRequest.toString());
			if (ppfeRequest == null)
				return;
			ppfeResponse.clear();
			try {
				requestContext = ppfeRequest.getContext();
				long before = System.currentTimeMillis();
				container.sendRequest("Sql", ppfeRequest.getData(), ppfeResponse);
				logger.trace("JAVA-SQL latency: %dms", System.currentTimeMillis() - before);
			} catch (Exception e) {
				ppfeResponse.getOutcome().setReturnCode(ReturnCode.FAILURE);
				ppfeResponse.getOutcome().setMessage(e.toString());
				logger.error(e, e.toString());
			}
			container.sendReply(requestContext, ppfeResponse.getData(), outcome);
			String msg = "Outcome on sending reply: %s";
			if (outcome.getReturnCode() == ReturnCode.SUCCESS) {
				logger.trace(msg, outcome.toString());
			} else {
				logger.error(msg, outcome.toString());
			}
			ppfeRequest.clear();
		}
	}
}
