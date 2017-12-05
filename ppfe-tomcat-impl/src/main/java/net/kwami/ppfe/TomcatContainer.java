package net.kwami.ppfe;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.tomcat.jdbc.pool.DataSource;
import org.apache.tomcat.jdbc.pool.PoolProperties;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import net.kwami.utils.Configurator;
import net.kwami.utils.MyLogger;
import net.kwami.utils.MyProperties;

public class TomcatContainer extends HttpServlet implements PpfeContainer {

	private static final String INCLUDED = "INCLUDED";
	private static final String EOF = "PPFE_END";
	private static final String APP_KEY = "app";
	private static final String PARM_NAME = "json";
	private static final long serialVersionUID = 1L;
	private static final MyLogger LOGGER = new MyLogger(TomcatContainer.class);
	private ThreadLocal<HttpServletRequest> threadServletRequest = new ThreadLocal<>();
	private ThreadLocal<HttpServletResponse> threadServletResponse = new ThreadLocal<>();
	private ThreadLocal<List<PpfeApplication>> threadApplications = new ThreadLocal<>();
	private DataSource dataSource;

	public TomcatContainer() {
		super();
	}

	@Override
	public void init() throws ServletException {
		PoolProperties pp = Configurator.get(PoolProperties.class, "/DataSourceConfig.js");
		dataSource = new DataSource();
		dataSource.setPoolProperties(pp);
	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		processRequest(request, response);
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		processRequest(request, response);
	}

	@Override
	public String getServletInfo() {
		return "PPFE Container";
	}

	public PpfeApplication createApplication() throws Exception {
		if (threadApplications.get() == null)
			threadApplications.set(new ArrayList<PpfeApplication>());
		String appName = threadServletRequest.get().getParameter(APP_KEY);
		if (appName == null)
			throw new Exception(String.format("A %s= parameter is required with the HTTP request", APP_KEY));
		ContainerConfig config = Configurator.get(ContainerConfig.class);
		Application appConfig = null;
		for (Application app : config.getApplications()) {
			if (app.getName().equals(appName)) {
				appConfig = app;
				break;
			}
		}
		if (appConfig == null)
			throw new Exception(String.format("No Application called '%s' has been configured", appName));
		for (PpfeApplication threadApp : threadApplications.get()) {
			if (threadApp.getAppName().equals(appConfig.getName()))
				return threadApp;
		}
		@SuppressWarnings("rawtypes")
		Class appClass = Class.forName(appConfig.getClassName());
		PpfeApplication ppfeApp = (PpfeApplication) appClass.newInstance();
		ppfeApp.setContainer(this);
		ppfeApp.setAppName(appConfig.getName());
		threadApplications.get().add(ppfeApp);
		return ppfeApp;
	}

	private void preparePpfeMessage(HttpServletRequest request, HttpServletResponse response,
			PpfeRequest ppfeRequest) throws Exception {
		Gson gson = new GsonBuilder().create();
		Object obj = request.getAttribute(PARM_NAME);
		ppfeRequest.setContext(INCLUDED);
		if (obj == null) {
			ppfeRequest.setContext("");
			obj = request.getParameter(PARM_NAME);
		}
		String jsonData = obj.toString().trim();
		MyProperties requestData = gson.fromJson(jsonData, MyProperties.class);
		ppfeRequest.setData(requestData);
		return;
	}

	private void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException {
		threadServletRequest.set(request);
		threadServletResponse.set(response);
		try {
			Enumeration<?> parameters = request.getParameterNames();
			while (parameters.hasMoreElements()) {
				String name = parameters.nextElement().toString();
				LOGGER.trace("parm=%s,value=%s", name, request.getParameter(name).toString());
			}
			createApplication().run();
		} catch (Exception e) {
			LOGGER.error(e, e.toString());
			throw new ServletException(e);
		} finally {
			threadServletRequest.set(null);
			threadServletResponse.set(null);
		}
	}

	@Override
	public PpfeContainer sendRequest(String destination, MyProperties requestParameters, PpfeResponse ppfeResponse) {
		HttpServletRequest request = threadServletRequest.get();
		HttpServletResponse response = threadServletResponse.get();
		Outcome outcome = ppfeResponse.getOutcome();
		ContainerConfig config = Configurator.get(ContainerConfig.class);
		Destination destSelected = null;
		for (Destination dest : config.getDestinations()) {
			if (dest.getName().equals(destination)) {
				destSelected = dest;
				break;
			}
		}
		if (destSelected == null) {
			outcome.setReturnCode(ReturnCode.FAILURE);
			outcome.setMessage(String.format("The specified destination of '%s' is not configured", destination));
			return this;
		}
		String responseStr = null;
		try {
			if (destSelected.getRemote() != null) {
				HttpClient httpClient = new HttpClient(destSelected.getRemote().getScheme(),
						destSelected.getRemote().getHostName(), destSelected.getRemote().getPort());
				responseStr = httpClient.post(destSelected.getUri(), requestParameters.toString());
			} else {
				threadServletRequest.get().getRequestDispatcher(destSelected.getUri()).include(request, response);
				responseStr = request.getAttribute(PARM_NAME).toString();
			}
			Gson gson = new GsonBuilder().create();
			MyProperties props = gson.fromJson(responseStr, MyProperties.class);
			ppfeResponse.setData(props);
		} catch (Exception e) {
			LOGGER.error(e, "unable to sendRequest()");
			outcome.setReturnCode(ReturnCode.FAILURE);
			outcome.setMessage(e.toString());
		}
		return this;
	}

	@Override
	public boolean getRequest(PpfeRequest msg) {
		HttpServletRequest request = threadServletRequest.get();
		HttpServletResponse response = threadServletResponse.get();
		if (request.getAttribute(EOF) != null)
			return false;
		request.setAttribute(EOF, "end");
		try {
			preparePpfeMessage(request, response, msg);
		} catch (Exception e) {
			LOGGER.error(e, "unable to get a request");
			return false;
		}
		return true;
	}

	@Override
	public PpfeContainer sendReply(Object requestContext, MyProperties responseParameters, Outcome outcome) {
		HttpServletRequest request = threadServletRequest.get();
		HttpServletResponse response = threadServletResponse.get();
		outcome.setMessage("replied with '%s'");
		try {
			String body = responseParameters.toString();
			LOGGER.debug("about to reply with '%s'", body);
			if (requestContext.toString().equals(INCLUDED)) {
				request.setAttribute(PARM_NAME, body);
			} else {
				writeHttpResponse(response, body);
			}
			outcome.setReturnCode(ReturnCode.SUCCESS);
			outcome.setMessage(String.format(outcome.getMessage(), body));
		} catch (Exception ex) {
			LOGGER.error(ex, "unable to get a sendReply()");
			outcome.setReturnCode(ReturnCode.FAILURE);
			outcome.setMessage(ex.toString());
		}
		return this;
	}

	private void writeHttpResponse(HttpServletResponse response, String outParms) throws IOException {
		PrintWriter out = response.getWriter();
		try {
			response.setContentType("text/html;charset=UTF-8");
			out.println(outParms);
			out.flush();
		} finally {
			out.close();
		}
	}

	@Override
	public Connection getDatabaseConnection() {
		try {
			return dataSource.getConnection();
		} catch (SQLException e) {
			LOGGER.error(e);
		}
		return null;
	}
}
