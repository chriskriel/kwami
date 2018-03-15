package net.kwami.pipe;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;

public class RemoteEndpoint extends InetSocketAddress {

	private static final long serialVersionUID = 1L;
	public static final String MACHINE_ADDRESS = "machine-address";
	private boolean forThisMachine = false;

	public RemoteEndpoint(String remoteHost, int remotePort) {
		super(remoteHost, remotePort);
		try {
			String ppfeLocalAddress = InetAddress.getByName(MACHINE_ADDRESS).getHostAddress();
			if (ppfeLocalAddress.equals(remoteHost))
				forThisMachine = true;
		} catch (UnknownHostException e1) {
		}
	}

	public boolean isForThisMachine() {
		return forThisMachine;
	}

	public void setForThisMachine(boolean forThisMachine) {
		this.forThisMachine = forThisMachine;
	}
}
