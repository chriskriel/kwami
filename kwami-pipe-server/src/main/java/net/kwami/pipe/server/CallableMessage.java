package net.kwami.pipe.server;

import net.kwami.pipe.Message;
import net.kwami.pipe.Pipe;

/**
 * A CallableMessage contains the request message and the Pipe on which the
 * request arrived (and which must be responded to). This makes it possible to
 * respond directly on the Pipe by overriding the ThreadPoolExecutor's
 * afterExecute method.
 * 
 * @author Chris Kriel
 *
 */
public class CallableMessage {
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
