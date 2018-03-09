package net.kwami.pipe.server;

import net.kwami.pipe.Message;
import net.kwami.pipe.Pipe;
import net.kwami.pipe.RemoteEndpoint;

/**
 * A CallableMessage contains the request message and the Pipe on which the
 * request arrived (and which must be responded to). This makes it possible to
 * respond directly on the Pipe by overriding the ThreadPoolExecutor's
 * afterExecute method.
 * 
 * @author Chris Kriel
 *
 */
public class CallableMessage implements Comparable<CallableMessage> {
	private final Message msg;
	private final Pipe pipe;

	/**
	 * Creates a new CallableMessage
	 * @param msg
	 *            The request message received by the server.
	 * @param origin
	 *            The Pipe on which the request message was received and on which
	 *            the future response must be sent.
	 */
	public CallableMessage(Message msg, Pipe origin) {
		super();
		this.msg = msg;
		this.pipe = origin;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
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

	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
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

	/**
	 * Getter method.
	 * 
	 * @return The request message received by the server.
	 */
	public Message getMsg() {
		return msg;
	}

	/**
	 * Getter method.
	 * 
	 * @return The Pipe on which the request message was received and on which the
	 *         future response must be sent.
	 */
	public Pipe getPipe() {
		return pipe;
	}

}
