package net.kwami.ppfe;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class Tester {

	public static void main(String[] args) {
		ContainerConfig config = new ContainerConfig();
		config.addContainer("basic", "net.kwami.ppfe.BasicContainer");;
		config.addContainer("pathsendRouter", "net.kwami.ppfe.PathsendContainer");
		Application app = new Application();
		app.setClassName("net.kwami.ppfe.RelayApplication");
		app.setMaxRequestSize(512);
		app.setMaxResponseSize(2048);
		config.addApplication("router", app);
		app = new Application();
		app.setClassName("net.kwami.ppfe.SqlInterpreter");
		app.setMaxRequestSize(512);
		app.setMaxResponseSize(2048);
		config.addApplication("sqlInterpreter", app);
		Destination dest = new Destination();
		dest.setApplicationName("router");
		dest.setUri("/localRouter");
		config.addDestination("localRouter", dest);
		dest = new Destination();
		dest.setApplicationName("sqlInterpreter");
		Destination.Remote remoteDest = new Destination.Remote();
		remoteDest.setScheme("http");
		remoteDest.setHostName("host2.bcx.co.za");
		remoteDest.setPort(18080);
		dest.setUri("/ppfe");
		dest.setRemote(remoteDest);
		config.addDestination("remoteSql", dest);
		String json = config.toString();
		System.out.println(json);
		Gson gson = new GsonBuilder().create();
		config = gson.fromJson(json, ContainerConfig.class);
		System.out.println(json);
	}
}
