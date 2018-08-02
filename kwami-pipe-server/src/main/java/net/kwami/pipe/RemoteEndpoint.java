package net.kwami.pipe;

import java.net.InetSocketAddress;

public class RemoteEndpoint {

	private final String remoteHost;
	private final String addressStr;
	private final InetSocketAddress socketAddress;
	
	public static String getMachineAddress() {
		String machineAddress = System.getenv("MACHINE_ADDRESS");
		if (machineAddress == null)
			machineAddress = "127.0.0.1";
		return machineAddress;
	}

	public RemoteEndpoint(String remoteHost, int remotePort) {
		this.remoteHost = remoteHost;
		addressStr = String.format("%s:%d", remoteHost, remotePort);
		socketAddress = new InetSocketAddress(remoteHost, remotePort);
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

	@Override
	public String toString() {
		return addressStr;
	}

	public InetSocketAddress getSocketAddress() {
		return socketAddress;
	}

	public boolean isOnThisMachine() {
		String machineAddress = getMachineAddress();
		if (machineAddress.equals(remoteHost))
			return true;
		return false;
	}

	public String getAddressStr() {
		return addressStr;
	}
}
