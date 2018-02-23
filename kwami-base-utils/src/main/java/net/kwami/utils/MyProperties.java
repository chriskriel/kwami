package net.kwami.utils;

import java.util.Properties;
import com.google.gson.GsonBuilder;

/*
	MyProperties is a java.util.Properties specialization that provides some methods
	to format and validate properties (mostly numeric properties).
*/
public class MyProperties extends Properties {

	private static final String ERR_MSG = "returning defaultValue %d because property '%s', with value '%s', cannot be parsed as %s";
	private static final long serialVersionUID = 1L;
	private static final MyLogger LOGGER = new MyLogger(MyProperties.class);
	

	/*
		Allows the use of JSON strings as properties. This method will instantiate
		the JSON string as an object of the type of the default value.
		@param property
			the name of the property that has a JSON string as a value
		@param defaultValue
			the default value if the property is null. The class of this
			parameter also determines the class of the returned value
		@return
			an object of the same class as the default value passed in
	*/
	@SuppressWarnings("unchecked")
	public final <T> T getJsonProperty(String property, T defaultValue) {
		String s = getProperty(property);
		if (s == null)
			return defaultValue;
		return (T)new GsonBuilder().create().fromJson(s, defaultValue.getClass());
	}

	/*
		Allows the use of JSON strings as properties. This method will serialize
		the object as a JSON string using the type of the value.
		@param property
			the name of the property that has a JSON string as a value
		@param value
			the object to serialize
		@return
			an object of the same class as the default value passed in
	*/
	public final <T> void setJsonProperty(String property, T value) {
		super.setProperty(property, new GsonBuilder().create().toJson(value));
	}

	public final void setBooleanProperty(String property, boolean value) {
		setProperty(property, String.valueOf(value));
	}

	public final boolean getBooleanProperty(String property, boolean defaultValue) {
		String s = getProperty(property);
		if (s == null)
			return defaultValue;
		s = s.trim();
		boolean b = s.equalsIgnoreCase("true") || s.equals("1");
		return b;
	}

	public final void setByteProperty(String property, byte value) {
		setProperty(property, String.valueOf(value));
	}

	public final byte getByteProperty(String property, byte defaultValue) {
		String s = getProperty(property);
		if (s == null)
			return defaultValue;
		try {
			return Byte.parseByte(s);
		} catch (NumberFormatException e) {
			LOGGER.error(ERR_MSG, defaultValue, property, s, "a byte");
		}
		return defaultValue;
	}

	public final void setShortProperty(String property, short value) {
		setProperty(property, String.valueOf(value));
	}

	public final short getShortProperty(String property, short defaultValue) {
		String s = getProperty(property);
		if (s == null)
			return defaultValue;
		try {
			return Short.parseShort(s);
		} catch (NumberFormatException e) {
			LOGGER.error(ERR_MSG, defaultValue, property, s, "a short");
		}
		return defaultValue;
	}

	public final void setIntProperty(String property, int value) {
		setProperty(property, String.valueOf(value));
	}

	public final int getIntProperty(String property, int defaultValue) {
		String s = getProperty(property);
		if (s == null)
			return defaultValue;
		try {
			return Integer.parseInt(s);
		} catch (NumberFormatException e) {
			LOGGER.error(ERR_MSG, defaultValue, property, s, "an int");
		}
		return defaultValue;
	}

	public final void setLongProperty(String property, long value) {
		setProperty(property, String.valueOf(value));
	}

	public final long getLongProperty(String property, long defaultValue) {
		String s = getProperty(property);
		if (s == null)
			return defaultValue;
		try {
			return Long.parseLong(s);
		} catch (NumberFormatException e) {
			LOGGER.error(ERR_MSG, defaultValue, property, s, "a long");
		}
		return defaultValue;
	}
	
        @Override
	public final String getProperty(String key) {
		return this.getProperty(key, null);
	}
	
        @Override
	public final String getProperty(String key, String defaultValue) {
		String original;
		if (defaultValue == null)
			original = super.getProperty(key);
		else
			original = super.getProperty(key, defaultValue);
		if (original == null)
			return null;
		if (!original.contains("${"))
			return original;
		String newValue = replaceVarWithSystemProperty(original);
		LOGGER.debug("key=%s,original='%s',new='%s'", key, original, newValue);
		return newValue;
	}

	private String replaceVarWithSystemProperty(String original) {
		StringBuilder bldr = new StringBuilder(original);
		int start = bldr.indexOf("${");
		int end = bldr.indexOf("}", start + 2);
		if (end < 0)
			return original;
		String sysProp = System.getProperty(bldr.substring(start + 2, end));
		if (sysProp == null)
			return original;
		bldr.replace(start, end + 1, sysProp);
		String newValue = bldr.toString();
		return newValue;
	}

	@Override
	public final String toString() {
		return new GsonBuilder().create().toJson(this);
	}

}
