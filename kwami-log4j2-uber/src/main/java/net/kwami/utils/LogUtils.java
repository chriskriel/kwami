package net.kwami.utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class LogUtils {
	private static final Logger ALERTS = LogManager.getLogger("alerts.log");
	
	public static final void alert(String simpleClassName, int alertNumber, String alertMessage) {
		ALERTS.info("{}-{}: {}", simpleClassName, alertNumber, alertMessage);
	}

}
