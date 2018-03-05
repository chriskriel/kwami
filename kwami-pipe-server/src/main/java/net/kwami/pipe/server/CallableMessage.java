package net.kwami.pipe.server;

import net.kwami.pipe.Message;
import net.kwami.pipe.Pipe;
import net.kwami.pipe.RemoteEndpoint;

public class CallableMessage implements Comparable<CallableMessage> {
	private final Message msg;
	private final Pipe pipe;

	public CallableMessage(Message msg, Pipe origin) {
		super();
		this.msg = msg;
		this.pipe = origin;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null)
			return false;
		if (!(obj instanceof CallableMessage))
			return false;
		CallableMessage other = (CallableMessage) obj;
		if (!pipe.equals(other.pipe))
			return false;
		if (msg.getId() == other.msg.getId())
			return true;
		return false;
	}

	@Override
	public int compareTo(CallableMessage o) {
		RemoteEndpoint thisEndpoint = pipe.getRemoteEndpoint();
		RemoteEndpoint otherEndpoint = o.getPipe().getRemoteEndpoint();
		int i = thisEndpoint.compareTo(otherEndpoint);
		if (i != 0)
			return i;
		long l = msg.getId() - o.msg.getId();
		if (l < 0)
			return -1;
		if (l > 0)
			return 1;
		return 0;
	}

	public Message getMsg() {
		return msg;
	}

	public Pipe getPipe() {
		return pipe;
	}

}
