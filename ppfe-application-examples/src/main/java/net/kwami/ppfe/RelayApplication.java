package net.kwami.ppfe;

import java.util.Properties;

import net.kwami.utils.Configurator;
import net.kwami.utils.MyLogger;

public class RelayApplication extends PpfeApplication {
	private static MyLogger logger = new MyLogger(PpfeApplication.class);

	public RelayApplication() throws Exception {
		super();
	}

	@Override
	public void run() {
		PpfeMessage message = null;
		logger.info("Going to get data");
		while ((message = getContainer().getRequest()) != null) {
			try {
				process(message);
			} catch (Exception e) {
				message.setData(null);
				message.getOutcome().setReturnCode(ReturnCode.FAILURE);
				message.getOutcome().setMessage(e.toString());
				logger.error(e, e.toString());
			}
			Outcome outcome = getContainer().sendReply(message);
			String msg = "Outcome on sending reply: %s";
			if (outcome.getReturnCode() == ReturnCode.SUCCESS) {
				logger.debug(msg, outcome.toString());
			} else {
				logger.error(msg, outcome.toString());
			}
		}
	}

	private void process(PpfeMessage message) {
		Properties properties = Configurator.get(Properties.class);
		PpfeMessage response = getContainer().sendRequest(properties.getProperty("nextServerPath"), message, 2000);
		message.setData(response.getData());
		message.setOutcome(response.getOutcome());
	}
}
