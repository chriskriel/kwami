package net.kwami.utils;

import java.util.Properties;
import com.google.gson.GsonBuilder;

public class MyProperties extends Properties {

	private static final String ERR_MSG = "returning defaultValue %d because property '%s', with value '%s', cannot be parsed as %s";
	private static final long serialVersionUID = 1L;
	private static final MyLogger logger = new MyLogger(MyProperties.class);
	
	@SuppressWarnings("unchecked")
	public <T> T getJsonProperty(String property, T defaultValue) {
		String s = getProperty(property);
		if (s == null)
			return defaultValue;
		return (T)new GsonBuilder().create().fromJson(s, defaultValue.getClass());
	}

	public void setByteProperty(String property, byte value) {
		setProperty(property, String.valueOf(value));
	}

	public byte getByteProperty(String property, byte defaultValue) {
		String s = getProperty(property);
		if (s == null)
			return defaultValue;
		try {
			return Byte.parseByte(s);
		} catch (NumberFormatException e) {
			logger.error(ERR_MSG, defaultValue, property, s, "a byte");
		}
		return defaultValue;
	}

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
