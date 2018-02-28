package net.kwami.pipe.server;

import com.google.gson.GsonBuilder;

import net.kwami.utils.MyProperties;

public class Command {
	public static enum Cmd {
		CONNECT, SHUTDOWN, RESTART, QUERY, RESPONSE
	}

	public static final Command fromBytes(byte[] bytes, int length) {
		String json = new String(bytes, 0, length);
		return new GsonBuilder().create().fromJson(json, Command.class);
	}

	private Cmd command = Cmd.CONNECT;
	private MyProperties parameters;

	public Command() {
	}

	public Command(Cmd command) {
		this.command = command;
	}

	public byte[] getBytes() {
		return this.toString().getBytes();
	}

	public void addParameter(String key, String value) {
		if (parameters == null)
			parameters = new MyProperties();
		parameters.setProperty(key, value);
	}

	public Cmd getCommand() {
		return command;
	}

	public void setCommand(Cmd cmd) {
		this.command = cmd;
	}

	public MyProperties getParameters() {
		return parameters;
	}

	public void setParameters(MyProperties parameters) {
		this.parameters = parameters;
	}

	@Override
	public String toString() {
		return new GsonBuilder().create().toJson(this);
	}

}
