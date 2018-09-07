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
	 * Allows the use of JSON strings as properties. This method will instantiate
	 * the JSON string as an object of the type of the default value.
	 * 
	 * @param key the name of the property that has a JSON string as a value
	 * 
	 * @param defaultValue the default value if the property is null. The class of
	 * this parameter also determines the class of the returned value
	 * 
	 * @return an object of the same class as the default value passed in
	 */
	@SuppressWarnings("unchecked")
	public final <T> T getJsonProperty(Object key, T defaultValue) {
		String s = getProperty(key.toString());
		if (s == null)
			return defaultValue;
		return (T) new GsonBuilder().create().fromJson(s, defaultValue.getClass());
	}

	/*
	 * Allows the use of JSON strings as properties. This method will serialize the
	 * object as a JSON string using the type of the value.
	 * 
	 * @param key the name of the property that has a JSON string as a value
	 * 
	 * @param value the object to serialize
	 * 
	 * @return an object of the same class as the default value passed in
	 */
	public final <T> void setJsonProperty(Object key, T value) {
		setProperty(key.toString(), new GsonBuilder().create().toJson(value));
	}

	public final void setBooleanProperty(Object key, boolean value) {
		setProperty(key.toString(), String.valueOf(value));
	}

	public final boolean getBooleanProperty(Object key, boolean defaultValue) {
		String s = getProperty(key.toString());
		if (s == null)
			return defaultValue;
		s = s.trim();
		boolean b = s.equalsIgnoreCase("true") || s.equals("1") || s.equalsIgnoreCase("on");
		return b;
	}

	public final void setByteProperty(Object key, byte value) {
		setProperty(key.toString(), String.valueOf(value));
	}

	public final byte getByteProperty(Object key, byte defaultValue) {
		String s = getProperty(key.toString());
		if (s == null)
			return defaultValue;
		try {
			return Byte.parseByte(s);
		} catch (NumberFormatException e) {
			LOGGER.error(ERR_MSG, defaultValue, key.toString(), s, "a byte");
		}
		return defaultValue;
	}

	public final void setShortProperty(Object key, short value) {
		setProperty(key.toString(), String.valueOf(value));
	}

	public final short getShortProperty(Object key, short defaultValue) {
		String s = getProperty(key.toString());
		if (s == null)
			return defaultValue;
		try {
			return Short.parseShort(s);
		} catch (NumberFormatException e) {
			LOGGER.error(ERR_MSG, defaultValue, key.toString(), s, "a short");
		}
		return defaultValue;
	}

	public final void setIntProperty(Object key, int value) {
		setProperty(key.toString(), String.valueOf(value));
	}

	public final int getIntProperty(Object key, int defaultValue) {
		String s = getProperty(key.toString());
		if (s == null)
			return defaultValue;
		try {
			return Integer.parseInt(s);
		} catch (NumberFormatException e) {
			LOGGER.error(ERR_MSG, defaultValue, key.toString(), s, "an int");
		}
		return defaultValue;
	}

	public final void setLongProperty(Object key, long value) {
		setProperty(key.toString(), String.valueOf(value));
	}

	public final long getLongProperty(Object key, long defaultValue) {
		String s = getProperty(key.toString());
		if (s == null)
			return defaultValue;
		try {
			return Long.parseLong(s);
		} catch (NumberFormatException e) {
			LOGGER.error(ERR_MSG, defaultValue, key.toString(), s, "a long");
		}
		return defaultValue;
	}
	
	public final String getProperty(Object keyObj) {
		return getProperty(keyObj.toString());
	}

	@Override
	public final String getProperty(String key) {
		return this.getProperty(key, null);
	}

	public final String getProperty(Object keyObj, String defaultValue) {
		return getProperty(keyObj.toString(), defaultValue);
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
	
	public final Object setProperty(Object keyObj, Object valueObj) {
		return super.setProperty(keyObj.toString(), valueObj.toString());
	}

	public final String removeProperty(Object keyObj) {
		return (String) remove(keyObj.toString());
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
		return bldr.toString();
	}

	@Override
	public final String toString() {
		return new GsonBuilder().create().toJson(this);
	}

}
