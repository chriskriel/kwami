package net.kwami.ppfe;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
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

	private static final String APP_URI = "app:";
	private static final String OUTPUT_DEQUE = "outputDeque";
	private static final String INPUT_DEQUE = "inputDeque";
	private static final String INVOKED = "INVOKED";
	private static final String APP_KEY = "app";
	private static final String INPUT_PARM = "json";
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

	public PpfeApplication createApplication(String appName) throws Exception {
		LOGGER.trace(Thread.currentThread().getName());
		if (threadApplications.get() == null)
			threadApplications.set(new ArrayList<PpfeApplication>());
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

	private void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException {
		LOGGER.trace(Thread.currentThread().getName());
		threadServletRequest.set(request);
		threadServletResponse.set(response);
		Deque<String> inputDeque = new ArrayDeque<String>();
		request.setAttribute(INPUT_DEQUE, inputDeque);
		request.setAttribute(OUTPUT_DEQUE, new ArrayDeque<String>());
		try {
			Enumeration<?> parameters = request.getParameterNames();
			while (parameters.hasMoreElements()) {
				String name = parameters.nextElement().toString();
				LOGGER.trace("parm=%s,value=%s", name, request.getParameter(name).toString());
				if (name.equals(INPUT_PARM))
					inputDeque.push(request.getParameter(name).toString());
			}
			if (inputDeque.isEmpty())
				throw new Exception("expected parameter " + INPUT_PARM + "={...} was not found");
			String appName = request.getParameter(APP_KEY);
			createApplication(appName.trim()).run();
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
		LOGGER.trace(Thread.currentThread().getName());
		HttpServletRequest request = threadServletRequest.get();
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
			} else if (destSelected.getUri().startsWith(APP_URI)) {
				String appName = destSelected.getUri().substring(4);
				@SuppressWarnings("unchecked")
				Deque<String> inputDeque = (Deque<String>) request.getAttribute(INPUT_DEQUE);
				inputDeque.push(requestParameters.toString());
				request.setAttribute(INVOKED, "true");
				createApplication(appName).run();
				@SuppressWarnings("unchecked")
				Deque<String> outputDeque = (Deque<String>) request.getAttribute(OUTPUT_DEQUE);
				responseStr = outputDeque.pop();
			} else
				throw new Exception(String.format("invalid URI of '%s' for destination name '%s'",
						destSelected.getUri(), destSelected.getName()));
			Gson gson = new GsonBuilder().create();
			MyProperties props = gson.fromJson(responseStr, MyProperties.class);
			ppfeResponse.setData(props);
		} catch (Exception e) {
			LOGGER.error(e, "unable to send a request to the destination named '%s'", destSelected.getName());
			outcome.setReturnCode(ReturnCode.FAILURE);
			outcome.setMessage(e.toString());
		}
		return this;
	}

	@Override
	public boolean getRequest(PpfeRequest ppfeRequest) {
		LOGGER.trace(Thread.currentThread().getName());
		HttpServletRequest request = threadServletRequest.get();
		@SuppressWarnings("unchecked")
		Deque<String> inputDeque = (Deque<String>) request.getAttribute(INPUT_DEQUE);
		if (inputDeque.isEmpty())
			return false;
		String jsonData = inputDeque.pop();
		Gson gson = new GsonBuilder().create();
		MyProperties requestData = gson.fromJson(jsonData, MyProperties.class);
		ppfeRequest.setData(requestData);
		if (request.getAttribute(INVOKED) != null) {
			ppfeRequest.setContext(INVOKED);
			request.removeAttribute(INVOKED);
		}
		return true;
	}

	@Override
	public PpfeContainer sendReply(Object requestContext, MyProperties responseParameters, Outcome outcome) {
		LOGGER.trace(Thread.currentThread().getName());
		HttpServletRequest request = threadServletRequest.get();
		HttpServletResponse response = threadServletResponse.get();
		outcome.setMessage("replied with '%s'");
		try {
			String json = responseParameters.toString();
			LOGGER.debug("about to reply with '%s'", json);
			if (requestContext.toString().equals(INVOKED)) {
				@SuppressWarnings("unchecked")
				Deque<String> outputDeque = (Deque<String>) request.getAttribute(OUTPUT_DEQUE);
				outputDeque.push(json);
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
		LOGGER.trace(Thread.currentThread().getName());
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
		LOGGER.trace(Thread.currentThread().getName());
		try {
			return dataSource.getConnection();
		} catch (SQLException e) {
			LOGGER.error(e);
		}
		return null;
	}
}
