package net.kwami.pipe.server;

import net.kwami.pipe.MessagePipe;
import net.kwami.pipe.RemoteEndpoint;

public class MessageOrigin implements Comparable<MessageOrigin> {
	private final long msgId;
	private final MessagePipe origin;

	public MessageOrigin(long msgId, MessagePipe origin) {
		super();
		this.msgId = msgId;
		this.origin = origin;
	}

	public long getMsgId() {
		return msgId;
	}

	public MessagePipe getOrigin() {
		return origin;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null)
			return false;
		if (!(obj instanceof MessageOrigin))
			return false;
		MessageOrigin other = (MessageOrigin) obj;
		if (!origin.equals(other.origin))
			return false;
		if (msgId == other.msgId)
			return true;
		return false;
	}

	@Override
	public int compareTo(MessageOrigin o) {
		RemoteEndpoint thisEndpoint = origin.getRemoteEndpoint();
		RemoteEndpoint otherEndpoint = o.getOrigin().getRemoteEndpoint();
		int i = thisEndpoint.compareTo(otherEndpoint);
		if (i != 0)
			return i;
		long l = msgId - o.msgId;
		if (l < 0)
			return -1;
		if (l > 0)
			return 1;
		return 0;
	}

}
