package net.kwami;

public class DynamicSqlServer extends PathwayServer {

	public DynamicSqlServer() throws Exception {
		super();
	}

	@Override
	public PpfeApplication createApplication() throws Exception {
		return new SqlInterpreter(this);
	}

	public static void main(String[] args) throws Exception {
		new DynamicSqlServer();
		Thread.sleep(5000); // so everybody has a change to terminate
	}
}
