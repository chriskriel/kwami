package net.kwami.pipe.server;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;

public class PipeKey implements Comparable<PipeKey> {

	public static final String MACHINE_ADDRESS = "MachineAddress";
	private final String host;
	private final int port;
	private final int localCommandPort;
	private boolean forThisMachine = false;

	public PipeKey(int localCommandPort, InetSocketAddress socketAddress) {
		super();
		this.host = socketAddress.getAddress().getHostAddress();
		this.port = socketAddress.getPort();
		this.localCommandPort = localCommandPort;
		try {
			String ppfeLocalAddress = InetAddress.getByName(PipeKey.MACHINE_ADDRESS).getHostAddress();
			if (ppfeLocalAddress.equals(host))
				forThisMachine = true;
		} catch (UnknownHostException e1) {
		}
	}

	@Override
	public String toString() {
		return String.format("PIPE:%s:%d", host, port);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null)
			return false;
		if (!(obj instanceof PipeKey))
			return false;
		PipeKey other = (PipeKey) obj;
		if (this.host.equals(other.host) && this.port == other.port)
			return true;
		return false;
	}

	@Override
	public int compareTo(PipeKey o) {
		for (int i = 0; i < host.length(); i++) {
			int diff = host.charAt(i) - o.host.charAt(i);
			if (diff != 0)
				return diff;
		}
		int len = host.length() - o.host.length();
		if (len != 0)
			return len;
		return port - o.port;
	}

	public boolean isForThisContainer() {
		if (forThisMachine && localCommandPort == port)
			return true;
		return false;
	}

	public byte[] toBytes() {
		return toString().getBytes();
	}

	public String getHost() {
		return host;
	}

	public int getPort() {
		return port;
	}

	public boolean isForThisMachine() {
		return forThisMachine;
	}

	public void setForThisMachine(boolean forThisMachine) {
		this.forThisMachine = forThisMachine;
	}
}
