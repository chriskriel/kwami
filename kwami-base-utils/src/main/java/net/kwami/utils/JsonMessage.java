package net.kwami.utils;

import org.apache.logging.log4j.message.Message;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class JsonMessage implements Message {

	private static final long serialVersionUID = 1L;
	private static final Gson GSON = new GsonBuilder().disableHtmlEscaping().create();
	private static final Gson GSON_PRETTY = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

	private final Object obj;
	private final boolean prettyFormat;

	public JsonMessage(final Object obj) {
		this(obj, false);
	}

	public JsonMessage(final Object obj, boolean prettyFormat) {
		this.obj = obj;
		this.prettyFormat = prettyFormat;
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

}
