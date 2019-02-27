package net.kwami.utils;

import java.lang.reflect.Method;
import java.util.Properties;

public abstract class PropertySetter {
	private static final MyLogger LOGGER = new MyLogger(PropertySetter.class);
	
	public static final void set(final Object obj, final Properties props) {
		for (String key : props.stringPropertyNames()) {
			LOGGER.trace("setting property '%s'", key);
			String value = props.getProperty(key);
			StringBuilder setterName = new StringBuilder(key);
			setterName.replace(0, 1, setterName.substring(0, 1).toUpperCase());
			setterName.insert(0, "set");
			try {
				Method method = findMethod(obj, setterName.toString());
				Class<?>[] parmTypes = method.getParameterTypes();
				if (parmTypes[0] == Integer.class || parmTypes[0] == int.class) {
					method.invoke(obj, Integer.valueOf(value));
				} else if (parmTypes[0] == Long.class || parmTypes[0] == long.class) {
					method.invoke(obj, Long.valueOf(value));
				} else if (parmTypes[0] == Short.class || parmTypes[0] == short.class) {
					method.invoke(obj, Short.valueOf(value));
				} else if (parmTypes[0] == Boolean.class || parmTypes[0] == boolean.class) {
					method.invoke(obj, Boolean.valueOf(value));
				} else if (parmTypes[0] == Byte.class || parmTypes[0] == byte.class) {
					method.invoke(obj, Byte.valueOf(value));
				} else if (parmTypes[0] == String.class) {
					method.invoke(obj, value);
				} else {
					throw new Exception();
				}
			} catch (Exception e) {
				LOGGER.error("ignoring setter '%s'", setterName.toString());
			}
		}
	}
	
	private static Method findMethod(final Object obj, final String methodName) throws Exception {
		Method[] methods = obj.getClass().getMethods();
		for (Method m : methods) {
			if (methodName.equals(m.getName()))
				return m;
		}
		throw new Exception();
	}

}
