package net.kwami.tcp;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.pool2.impl.GenericObjectPool;

import com.google.gson.GsonBuilder;

public class Listener {

	private transient ServerSocket serverSocket;
	private int listeningPort = 10000;
	private String bindAddress = "127.0.0.1";
	private String[] balancingPools = { "pool127:0:0:2:1000.js", "pool127:0:0:3:1000.js" };
	private transient String myName;
	private transient List<GenericObjectPool<Socket>> socketPools = new ArrayList<GenericObjectPool<Socket>>();

	public Listener() {
		super();
	}

	public Listener(String configFileName) throws Exception {
		try {
			myName = String.format("Listener %s:%s", bindAddress, listeningPort);
			InetSocketAddress endpoint = new InetSocketAddress(bindAddress, listeningPort);
			serverSocket = new ServerSocket();
			serverSocket.bind(endpoint);
			for (String poolName : balancingPools) {
				SocketPoolConfig poolConfig = Configurator.get(SocketPoolConfig.class, poolName);
				SocketFactory socketFactory = new SocketFactory(poolConfig);
				GenericObjectPool<Socket> socketPool = new GenericObjectPool<Socket>(socketFactory, poolConfig);
				socketPools.add(socketPool);
			}
			System.out.println("ServerSocket for " + myName + " is now active");
		} catch (Exception e) {
			System.out.println(myName + " Exception follows");
			throw e;
		}
	}

	public int getListeningPort() {
		return listeningPort;
	}

	public void setListeningPort(int listeningPort) {
		this.listeningPort = listeningPort;
	}

	public String getBindAddress() {
		return bindAddress;
	}

	public void setBindAddress(String bindAddress) {
		this.bindAddress = bindAddress;
	}

	public String[] getBalancingPools() {
		return balancingPools;
	}

	public void setBalancingPools(String[] balancingPools) {
		this.balancingPools = balancingPools;
	}

	@Override
	public String toString() {
		return new GsonBuilder().setPrettyPrinting().create().toJson(this);
	}

	public void run() {
		while (true) {
			try {
				Socket socket = serverSocket.accept();
				System.out.println(myName + " received connection request");
//				ExternalConnection extConn = new ExternalConnection(socket);
//				extConn.start();
//				OutputStreamWriter osw = new OutputStreamWriter(socket.getOutputStream());
//				BufferedWriter toClient = new BufferedWriter(osw);
//				toClient.write(myName + " is alive and accessible\n");
//				toClient.flush();
//				socket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public static void main(String[] args) {
		if (args.length == 0) {
			System.out.println("Usage: java Listener <configFileName>");
			return;
		}
		try {
			Listener lt = Configurator.get(Listener.class, args[0]);
			lt.run();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
