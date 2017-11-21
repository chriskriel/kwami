package net.kwami.ppfe;

import org.apache.tomcat.jdbc.pool.DataSource;

public interface PpfeContainer {

	PpfeApplication createApplication(PpfeMessage firstRequest) throws Exception;

	PpfeMessage sendRequest(String destination, PpfeMessage message, long timeoutMillis);

	PpfeMessage getRequest(int maxBuf);

	Outcome sendReply(PpfeMessage message);
	
	DataSource getDataSource();

}