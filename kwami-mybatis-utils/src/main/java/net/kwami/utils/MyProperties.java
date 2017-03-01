package net.kwami.utils;

import java.util.Properties;

public class MyProperties extends Properties {

	private static final String ERR_MSG = "returning defaultValue %d because property '%s', with value '%s', cannot be parsed as %s";
	private static final long serialVersionUID = 1L;
	private static final MyLogger logger = new MyLogger(MyProperties.class);

	public void setShortProperty(String property, short value) {
		setProperty(property, String.valueOf(value));
	}

	public short getShortProperty(String property, short defaultValue) {
		String s = getProperty(property);
		if (s == null)
			return defaultValue;
		try {
			return Short.parseShort(s);
		} catch (NumberFormatException e) {
			logger.error(ERR_MSG, defaultValue, property, s, "a short");
		}
		return defaultValue;
	}

	public void setIntProperty(String property, int value) {
		setProperty(property, String.valueOf(value));
	}

	public int getIntProperty(String property, int defaultValue) {
		String s = getProperty(property);
		if (s == null)
			return defaultValue;
		try {
			return Integer.parseInt(s);
		} catch (NumberFormatException e) {
			logger.error(ERR_MSG, defaultValue, property, s, "an int");
		}
		return defaultValue;
	}

	public void setLongProperty(String property, long value) {
		setProperty(property, String.valueOf(value));
	}

	public long getLongProperty(String property, long defaultValue) {
		String s = getProperty(property);
		if (s == null)
			return defaultValue;
		try {
			return Long.parseLong(s);
		} catch (NumberFormatException e) {
			logger.error(ERR_MSG, defaultValue, property, s, "a long");
		}
		return defaultValue;
	}
}
