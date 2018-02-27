package net.kwami.pipe.server;

public interface Router {

	/**
	 * Determines the destination for a request.
	 * 
	 * @param appName
	 *            The application for which the destination must be determined.
	 * @return The destination in terms of a PipeKey or null if the application is
	 *         available in the local container
	 * @throws Exception
	 *             When the destination is not known.
	 */
	public RemoteEndpoint selectDestination(String appName) throws Exception;

}
