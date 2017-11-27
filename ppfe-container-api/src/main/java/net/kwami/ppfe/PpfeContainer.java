package net.kwami.ppfe;

import org.apache.tomcat.jdbc.pool.DataSource;

public interface PpfeContainer {

	PpfeResponse sendRequest(String destination, PpfeRequest request);

	PpfeRequest getRequest();

	Outcome sendReply(Object requestContext, PpfeResponse response);
	
	DataSource getDataSource();

}