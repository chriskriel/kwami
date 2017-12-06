package net.kwami.ppfe;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Deque;
import java.util.Enumeration;

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

	private static final String APP_URI = "app:";
	private static final String INVOKED = "INVOKED";
	private static final String APP_KEY = "app";
	private static final String INPUT_PARM = "json";
	private static final long serialVersionUID = 1L;
	private static final MyLogger LOGGER = new MyLogger(TomcatContainer.class);
	private ThreadLocal<ThreadData> threadLocal = new ThreadLocal<>();
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

	private PpfeApplication createApplication(String appName) throws Exception {
		ThreadData threadData = threadLocal.get();
		LOGGER.trace("Container");
		if (appName == null)
			throw new Exception(String.format("A %s= parameter is required with the HTTP request", APP_KEY));
		ContainerConfig config = Configurator.get(ContainerConfig.class);
		Application appConfig = config.getApplications().get(appName);
		if (appConfig == null)
			throw new Exception(String.format("No Application called '%s' has been configured", appName));
		for (PpfeApplication threadApp : threadData.getApplications()) {
			if (threadApp.getAppName().equals(appName))
				return threadApp;
		}
		@SuppressWarnings("rawtypes")
		Class appClass = Class.forName(appConfig.getClassName());
		PpfeApplication ppfeApp = (PpfeApplication) appClass.newInstance();
		ppfeApp.setContainer(this);
		ppfeApp.setAppName(appName);
		threadData.getAppNameStack().push(appName + ": ");
		threadData.getApplications().add(ppfeApp);
		return ppfeApp;
	}

	private void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException {
		LOGGER.trace(Thread.currentThread().getName());
		ThreadData threadData = threadLocal.get();
		if (threadData == null) {
			threadData = new ThreadData();
			threadLocal.set(threadData);
		}
		threadData.setRequest(request);
		threadData.setResponse(response);
		Deque<String> inputStack = threadData.getInputStack();
		try {
			Enumeration<?> parameters = request.getParameterNames();
			while (parameters.hasMoreElements()) {
				String name = parameters.nextElement().toString();
				LOGGER.trace("parm=%s,value=%s", name, request.getParameter(name).toString());
				if (name.equals(INPUT_PARM))
					inputStack.push(request.getParameter(name).toString());
			}
			if (inputStack.isEmpty())
				throw new Exception("expected parameter " + INPUT_PARM + "={...} was not found");
			String appName = request.getParameter(APP_KEY);
			createApplication(appName.trim()).run();
			threadData.getAppNameStack().pop();
		} catch (Exception e) {
			LOGGER.error(e);
			throw new ServletException(e);
		} finally {
			threadData.setRequest(null);
			threadData.setResponse(null);
			threadData.getInputStack().clear();
			threadData.getOutputStack().clear();
		}
	}

	@Override
	public PpfeContainer sendRequest(String destination, MyProperties requestParameters, PpfeResponse ppfeResponse) {
		ThreadData threadData = threadLocal.get();
		LOGGER.trace(threadData.getAppNameStack().peek());
		HttpServletRequest request = threadData.getRequest();
		Outcome outcome = ppfeResponse.getOutcome();
		ContainerConfig config = Configurator.get(ContainerConfig.class);
		Destination destSelected = config.getDestinations().get(destination);
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
			} else if (destSelected.getUri().startsWith(APP_URI)) {
				String appName = destSelected.getUri().substring(4);
				threadData.getInputStack().push(requestParameters.toString());
				request.setAttribute(INVOKED, "true");
				createApplication(appName.trim()).run();
				threadData.getAppNameStack().pop();
				responseStr = threadData.getOutputStack().pop();
			} else
				throw new Exception(String.format("invalid URI of '%s' for destination name '%s'",
						destSelected.getUri(), destination));
			Gson gson = new GsonBuilder().create();
			MyProperties props = gson.fromJson(responseStr, MyProperties.class);
			ppfeResponse.setData(props);
		} catch (Exception e) {
			LOGGER.error(e, "unable to send a request to the destination named '%s'", destination);
			outcome.setReturnCode(ReturnCode.FAILURE);
			outcome.setMessage(e.toString());
		}
		return this;
	}

	@Override
	public boolean getRequest(PpfeRequest ppfeRequest) {
		ThreadData threadData = threadLocal.get();
		LOGGER.trace(threadData.getAppNameStack().peek());
		HttpServletRequest request = threadData.getRequest();
		Deque<String> inputStack = threadData.getInputStack();
		if (inputStack.isEmpty())
			return false;
		String jsonData = inputStack.pop();
		Gson gson = new GsonBuilder().create();
		MyProperties requestData = gson.fromJson(jsonData, MyProperties.class);
		ppfeRequest.setData(requestData);
		if (request.getAttribute(INVOKED) != null) {
			ppfeRequest.setContext(INVOKED);
			request.removeAttribute(INVOKED);
		} else
			ppfeRequest.setContext("");
		return true;
	}

	@Override
	public PpfeContainer sendReply(Object requestContext, MyProperties responseParameters, Outcome outcome) {
		ThreadData threadData = threadLocal.get();
		LOGGER.trace(threadData.getAppNameStack().peek());
		HttpServletResponse response = threadData.getResponse();
		outcome.setMessage("replied with '%s'");
		try {
			String json = responseParameters.toString();
			LOGGER.debug("about to reply with '%s'", json);
			if (requestContext.toString().equals(INVOKED)) {
				threadData.getOutputStack().push(json);
			} else {
				writeHttpResponse(response, json);
			}
			outcome.setReturnCode(ReturnCode.SUCCESS);
			outcome.setMessage(String.format(outcome.getMessage(), json));
		} catch (Exception ex) {
			LOGGER.error(ex, "unable to do a sendReply()");
			outcome.setReturnCode(ReturnCode.FAILURE);
			outcome.setMessage(ex.toString());
		}
		return this;
	}

	private void writeHttpResponse(HttpServletResponse response, String outParms) throws IOException {
		ThreadData threadData = threadLocal.get();
		LOGGER.trace(threadData.getAppNameStack().peek());
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
		ThreadData threadData = threadLocal.get();
		LOGGER.trace(threadData.getAppNameStack().peek());
		try {
			return dataSource.getConnection();
		} catch (SQLException e) {
			LOGGER.error(e);
		}
		return null;
	}
}
