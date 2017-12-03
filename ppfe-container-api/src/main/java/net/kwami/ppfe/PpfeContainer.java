package net.kwami.ppfe;

import java.sql.Connection;

import net.kwami.utils.MyProperties;

public interface PpfeContainer {

	PpfeContainer sendRequest(String destination, MyProperties requestParameters, PpfeResponse ppfeResponse);

	boolean getRequest(PpfeRequest ppfeRequest);

	PpfeContainer sendReply(Object requestContext, MyProperties responseParameters, Outcome outcome);
	
	Connection getDatabaseConnection();

}