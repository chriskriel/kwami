package net.kwami.utils;

import org.apache.logging.log4j.message.Message;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class JsonMessage implements Message {

	private static final long serialVersionUID = 1L;
	private static final Gson GSON = new GsonBuilder().disableHtmlEscaping().create();
	private static final Gson GSON_PRETTY = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
	
	@SuppressWarnings("unchecked")
	public static final <T> T getObj(String json, Class<?> T) {
		return (T) GSON.fromJson(json, T);
	}
	
	public static final String getJson(Object obj) {
		return getJson(obj, false);
	}
	
	public static final String getJson(Object obj, boolean prettyFormat) {
		if (prettyFormat)
			return GSON_PRETTY.toJson(obj);
		return GSON.toJson(obj);
	}

	private final Object obj;
	private final boolean prettyFormat;

	public JsonMessage(final Object obj) {
		this(obj, false);
	}

	public JsonMessage(final Object obj, boolean prettyFormat) {
		this.obj = obj;
		this.prettyFormat = prettyFormat;
	}
	
	public JsonMessage(final String jsonStr, Class<?> classOfT) {
		prettyFormat = false;
		obj = GSON.fromJson(jsonStr, classOfT);
	}

	@Override
	public String getFormat() {
		return null;
	}

	@Override
	public String getFormattedMessage() {
		if (prettyFormat)
			return GSON_PRETTY.toJson(obj);
		return GSON.toJson(obj);
	}

	@Override
	public Object[] getParameters() {
		return null;
	}

	@Override
	public Throwable getThrowable() {
		return null;
	}

	@Override
	public String toString() {
		return getFormattedMessage();
	}
	
	@SuppressWarnings("unchecked")
	public <T> T getObject(Class<?> T) {
		return (T) obj;
	}

}
