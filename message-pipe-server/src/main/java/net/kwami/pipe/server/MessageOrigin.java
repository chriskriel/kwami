package net.kwami.pipe.server;

import net.kwami.pipe.MessagePipe;
import net.kwami.pipe.RemoteEndpoint;

public class MessageOrigin implements Comparable<MessageOrigin> {
	private final long msgId;
	private final MessagePipe messagePipe;

	public MessageOrigin(long msgId, MessagePipe origin) {
		super();
		this.msgId = msgId;
		this.messagePipe = origin;
	}

	public long getMsgId() {
		return msgId;
	}

	public MessagePipe getMessagePipe() {
		return messagePipe;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null)
			return false;
		if (!(obj instanceof MessageOrigin))
			return false;
		MessageOrigin other = (MessageOrigin) obj;
		if (!messagePipe.equals(other.messagePipe))
			return false;
		if (msgId == other.msgId)
			return true;
		return false;
	}

	@Override
	public int compareTo(MessageOrigin o) {
		RemoteEndpoint thisEndpoint = messagePipe.getRemoteEndpoint();
		RemoteEndpoint otherEndpoint = o.getMessagePipe().getRemoteEndpoint();
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
