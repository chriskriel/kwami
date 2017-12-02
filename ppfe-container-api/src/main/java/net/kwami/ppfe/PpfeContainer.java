package net.kwami.ppfe;

import org.apache.tomcat.jdbc.pool.DataSource;

import net.kwami.utils.MyProperties;

public interface PpfeContainer {

	PpfeContainer sendRequest(String destination, MyProperties requestParameters, PpfeResponse ppfeResponse);

	boolean getRequest(PpfeRequest ppfeRequest);

	PpfeContainer sendReply(Object requestContext, MyProperties responseParameters, Outcome outcome);
	
	DataSource getDataSource();

}