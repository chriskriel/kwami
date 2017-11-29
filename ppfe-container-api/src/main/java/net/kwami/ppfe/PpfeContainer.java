package net.kwami.ppfe;

import org.apache.tomcat.jdbc.pool.DataSource;

import net.kwami.utils.MyProperties;

public interface PpfeContainer {

	PpfeResponse sendRequest(String destination, MyProperties requestParameters);

	PpfeRequest getRequest();

	Outcome sendReply(Object requestContext, MyProperties responseParameters);
	
	DataSource getDataSource();

}