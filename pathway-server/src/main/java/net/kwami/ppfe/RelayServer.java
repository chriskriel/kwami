package net.kwami.ppfe;

public class RelayServer extends PathwayServer {

	public RelayServer() throws Exception {
		super();
	}
	
	public PpfeApplication createApplication() throws Exception {
		return new RelayApplication(this);
	}
	
	public static void main(String[] args) throws Exception {
		new RelayServer();
		Thread.sleep(5000); // so all threads have a chance to terminate
	}
}
