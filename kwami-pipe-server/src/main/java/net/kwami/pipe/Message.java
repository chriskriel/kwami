package net.kwami.pipe;

import java.util.Arrays;

public final class Message {
	public static enum Status {
		WAIT, DONE, NEW
	}

	private long id;
	private byte[] data;
	private Status status = Status.NEW;

	public Message(final long id, final byte[] data, final int length) {
		super();
		this.id = id;
		this.data = Arrays.copyOf(data, length);
	}

	public Message(final long id, String data) {
		super();
		this.id = id;
		this.data = data.getBytes();
	}

	public final byte[] getDataBytes() {
		return data;
	}

	public final void setDataBytes(final byte[] bytes, int length) {
		this.data = Arrays.copyOf(data, length);
	}

	public final long getId() {
		return id;
	}

	public final void setId(final long id) {
		this.id = id;
	}

	public final String getData() {
		return new String(data, 0, data.length);
	}

	public final void setData(final String data) {
		this.data = data.getBytes();
	}

	public final Status getStatus() {
		return status;
	}

	public final void setStatus(final Status status) {
		this.status = status;
	}

}
