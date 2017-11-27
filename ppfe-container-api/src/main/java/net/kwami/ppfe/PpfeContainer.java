package net.kwami.ppfe;

import org.apache.tomcat.jdbc.pool.DataSource;

public interface PpfeContainer {

	PpfeMessage sendRequest(String destination, PpfeMessage message);

	PpfeMessage getRequest();

	Outcome sendReply(PpfeMessage message);
	
	DataSource getDataSource();

}