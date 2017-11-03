package net.kwami;

import org.apache.tomcat.jdbc.pool.DataSource;

public interface PpfeContainer {

	PpfeApplication createApplication() throws Exception;

	PpfeMessage sendRequest(String destination, PpfeMessage message);

	PpfeMessage getRequest(int maxBuf);

	int sendReply(PpfeMessage message);
	
	DataSource getDataSource();

}