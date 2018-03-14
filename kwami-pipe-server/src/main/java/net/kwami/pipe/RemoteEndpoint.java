package net.kwami.pipe;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;

import net.kwami.utils.MyLogger;

public class RemoteEndpoint implements Comparable<RemoteEndpoint> {

	public static final String MACHINE_ADDRESS = "machine-address";
	private static final MyLogger logger = new MyLogger(RemoteEndpoint.class);
	private final String remoteHost;
	private final int remotePort;
	private final int localPort;
	private boolean forThisMachine = false;

	public RemoteEndpoint(int localPort, InetSocketAddress remoteSocketAddress) {
		super();
		this.remoteHost = remoteSocketAddress.getAddress().getHostAddress();
		this.remotePort = remoteSocketAddress.getPort();
		this.localPort = localPort;
		try {
			String ppfeLocalAddress = InetAddress.getByName(MACHINE_ADDRESS).getHostAddress();
			if (ppfeLocalAddress.equals(remoteHost))
				forThisMachine = true;
		} catch (UnknownHostException e1) {
		}
	}

	@Override
	public String toString() {
		return String.format("/%d:%s:%d", localPort, remoteHost, remotePort);
	}

	@Override
	public int compareTo(RemoteEndpoint o) {
		for (int i = 0; i < remoteHost.length(); i++) {
			int diff = remoteHost.charAt(i) - o.remoteHost.charAt(i);
			if (diff != 0)
				return diff;
		}
		int len = remoteHost.length() - o.remoteHost.length();
		if (len != 0)
			return len;
		return remotePort - o.remotePort;
	}

	public boolean isforThisServer() {
		if (forThisMachine && localPort == remotePort)
			return true;
		return false;
	}

	public byte[] toBytes() {
		return toString().getBytes();
	}

	public String getRemoteHost() {
		return remoteHost;
	}

	public int getRemotePort() {
		return remotePort;
	}

	public boolean isForThisMachine() {
		return forThisMachine;
	}

	public void setForThisMachine(boolean forThisMachine) {
		this.forThisMachine = forThisMachine;
	}

	public int getLocalPort() {
		return localPort;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + localPort;
		result = prime * result + ((remoteHost == null) ? 0 : remoteHost.hashCode());
		result = prime * result + remotePort;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		logger.trace(this.toString());
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		RemoteEndpoint other = (RemoteEndpoint) obj;
		if (localPort != other.localPort)
			return false;
		if (remoteHost == null) {
			if (other.remoteHost != null)
				return false;
		} else if (!remoteHost.equals(other.remoteHost))
			return false;
		if (remotePort != other.remotePort)
			return false;
		return true;
	}
}
