package net.kwami.pipe.server;

import java.util.concurrent.atomic.AtomicLong;

public class MessageKey implements Comparable<MessageKey> {
	private static final AtomicLong nextMsgId = new AtomicLong();
	private final long msgId;
	private final RemoteEndpoint remoteEndpoint;

	public MessageKey(RemoteEndpoint remoteEndpoint) {
		super();
		this.msgId = nextMsgId.incrementAndGet();
		this.remoteEndpoint = remoteEndpoint;
	}

	public MessageKey(long msgId, RemoteEndpoint remoteEndpoint) {
		super();
		this.msgId = msgId;
		this.remoteEndpoint = remoteEndpoint;
	}

	public long getMsgId() {
		return msgId;
	}

	public RemoteEndpoint getRemoteEndpoint() {
		return remoteEndpoint;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null)
			return false;
		if (!(obj instanceof MessageKey))
			return false;
		MessageKey other = (MessageKey) obj;
		if (!remoteEndpoint.equals(other.remoteEndpoint))
			return false;
		if (msgId == other.msgId)
			return true;
		return false;
	}

	@Override
	public int compareTo(MessageKey o) {
		int i = remoteEndpoint.compareTo(o.remoteEndpoint);
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
