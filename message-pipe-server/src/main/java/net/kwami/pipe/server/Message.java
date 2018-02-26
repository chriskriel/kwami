package net.kwami.pipe.server;

public class Message {
	public static enum Status {
		WAIT, DONE, NEW
	}
	private long id;
	private String data;
	private Status status = Status.NEW;

	public Message(long id, String data) {
		super();
		this.id = id;
		this.data = data;
	}

	public byte[] getDataBytes() {
		return data.getBytes();
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getData() {
		return data;
	}

	public void setData(String data) {
		this.data = data;
	}

	public Status getStatus() {
		return status;
	}

	public void setStatus(Status status) {
		this.status = status;
	}

}
