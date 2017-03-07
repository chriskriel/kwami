package net.kwami.tcp;

public class Application {
	private String appName;
	private String hostName;
	private int port;
	
	public Application(String appName, String hostName, int port) {
		super();
		this.hostName = hostName;
		this.port = port;
		this.appName = appName;
	}

}
