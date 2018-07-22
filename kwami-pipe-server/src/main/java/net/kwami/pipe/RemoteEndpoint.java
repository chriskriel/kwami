package net.kwami.pipe;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;

public class RemoteEndpoint {

	public static final String MACHINE_ADDRESS = "machine-address";
	private boolean forThisMachine = false;
	private final String addressStr;
	private final InetSocketAddress socketAddress;

	public RemoteEndpoint(String remoteHost, int remotePort) {
		addressStr = String.format("%s:%d", remoteHost, remotePort);
		socketAddress = new InetSocketAddress(remoteHost, remotePort);
		try {
			String ppfeLocalAddress = InetAddress.getByName(MACHINE_ADDRESS).getHostAddress();
			if (ppfeLocalAddress.equals(remoteHost))
				forThisMachine = true;
		} catch (UnknownHostException e1) {
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((addressStr == null) ? 0 : addressStr.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		RemoteEndpoint other = (RemoteEndpoint) obj;
		if (addressStr == null) {
			if (other.addressStr != null)
				return false;
		} else if (!addressStr.equalsIgnoreCase(other.addressStr))
			return false;
		return true;
	}

	public InetSocketAddress getSocketAddress() {
		return socketAddress;
	}

	public boolean isForThisMachine() {
		return forThisMachine;
	}

	public void setForThisMachine(boolean forThisMachine) {
		this.forThisMachine = forThisMachine;
	}
}
