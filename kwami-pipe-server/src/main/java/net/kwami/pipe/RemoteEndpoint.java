package net.kwami.pipe;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;

public class RemoteEndpoint implements Comparable<RemoteEndpoint> {

	public static final String MACHINE_ADDRESS = "machine-address";
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
		return String.format("PIPE/%s:%d", remoteHost, remotePort);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null)
			return false;
		if (!(obj instanceof RemoteEndpoint))
			return false;
		RemoteEndpoint other = (RemoteEndpoint) obj;
		if (this.remoteHost.equals(other.remoteHost) && this.remotePort == other.remotePort)
			return true;
		return false;
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
}
