package net.kwami;

import org.apache.tomcat.jdbc.pool.DataSource;

public class RelayServer extends PathwayServer {

	public RelayServer() throws Exception {
		super();
	}
	
	public PpfeApplication createApplication() throws Exception {
		return new RelayApplication(this);
	}

	@Override
	public DataSource getDataSource() {
		return null;
	}
	
	public static void main(String[] args) throws Exception {
		new RelayServer();
		Thread.sleep(5000); // so all threads have a chance to terminate
	}
}
