package net.kwami.tcp;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;

public class SocketFactory extends BasePooledObjectFactory<Socket> {
	SocketPoolConfig config;

	public SocketFactory(SocketPoolConfig config) {
		super();
		this.config = config;
	}

	@Override
	public Socket create() throws Exception {
		Socket s = new Socket();
		s.setSoTimeout(config.getConnectTimeoutMs());
		InetSocketAddress addr = new InetSocketAddress(config.getServerIp(), config.getConnectTimeoutMs());
		s.connect(addr, config.getConnectTimeoutMs());
		return s;
	}

	@Override
	public void destroyObject(PooledObject<Socket> p) throws Exception {
		p.getObject().close();
	}

	@Override
	public boolean validateObject(PooledObject<Socket> p) {
		Socket s = p.getObject();
		if (s.isClosed())
			return false;
		if (!s.isConnected())
			return false;
		if (s.isInputShutdown())
			return false;
		if (s.isOutputShutdown())
			return false;
		try {
			s.getInputStream();
		} catch (IOException e) {
			return false;
		}
		return true;
	}

	@Override
	public PooledObject<Socket> wrap(Socket obj) {
		return new DefaultPooledObject<Socket>(obj);
	}

}
