package net.kwami.pipe.server;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import net.kwami.utils.MyProperties;

public class Command {
	public static enum Cmd {
		CONNECT, SHUTDOWN, RESTART, QUERY, RESPONSE
	}

	private static Gson gson = new GsonBuilder().create();

	public static Command read(SocketChannel socketChannel, ByteBuffer workBuffer) throws IOException {
		workBuffer.clear();
		socketChannel.read(workBuffer);
		return fromBytes(workBuffer.array(), workBuffer.position());
	}

	public static final Command fromBytes(byte[] bytes, int length) {
		String json = new String(bytes, 0, length);
		return gson.fromJson(json, Command.class);
	}

	private Cmd command = Cmd.CONNECT;
	private MyProperties parameters;

	public Command() {
	}

	public Command(Cmd command) {
		this.command = command;
	}

	public final void write(final SocketChannel socketChannel, final ByteBuffer commandBuffer) throws IOException {
		commandBuffer.clear();
		commandBuffer.put(this.toString().getBytes());
		commandBuffer.flip();
		socketChannel.write(commandBuffer);
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
		return gson.toJson(this);
	}

}
