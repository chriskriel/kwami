package net.kwami.pipe.server;

import java.util.concurrent.atomic.AtomicLong;

public class MessageKey implements Comparable<MessageKey> {
	private static final AtomicLong nextMsgId = new AtomicLong();
	private final long msgId;
	private final PipeKey pipeKey;

	public MessageKey(PipeKey pipeKey) {
		super();
		this.msgId = nextMsgId.incrementAndGet();
		this.pipeKey = pipeKey;
	}

	public MessageKey(long msgId, PipeKey pipeKey) {
		super();
		this.msgId = msgId;
		this.pipeKey = pipeKey;
	}

	public long getMsgId() {
		return msgId;
	}

	public PipeKey getPipeKey() {
		return pipeKey;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null)
			return false;
		if (!(obj instanceof MessageKey))
			return false;
		MessageKey other = (MessageKey) obj;
		if (!pipeKey.equals(other.pipeKey))
			return false;
		if (msgId == other.msgId)
			return true;
		return false;
	}

	@Override
	public int compareTo(MessageKey o) {
		int i = pipeKey.compareTo(o.pipeKey);
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
