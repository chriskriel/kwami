package net.kwami.ppfe;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Deque;
import java.util.Enumeration;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.tandem.sqlmx.SQLMXDataSource;

import net.kwami.pathsend.PathwayClient;
import net.kwami.utils.Configurator;
import net.kwami.utils.MyLogger;
import net.kwami.utils.MyProperties;

public class BorderContainer extends HttpServlet implements PpfeContainer {

	private static class DataSourceConfig {
		public String blobTableName;
		public String clobTableName;
		public String catalog;
		public String schema;
		public int initialPoolSize;
		public int maxIdleTime;
		public int maxPoolSize;
		public int maxStatements;
		public int minPoolSize;
	}

	private static final String INVOKED = "INVOKED";
	private static final String APP_KEY = "app";
	private static final String INPUT_PARM = "json";
	private static final long serialVersionUID = 1L;
	private static final MyLogger LOGGER = new MyLogger(BorderContainer.class);
	private ThreadLocal<ThreadData> threadLocal = new ThreadLocal<>();
	private final ThreadLocal<List<PathwayClient>> threadPathwayClients = new ThreadLocal<>();
	private SQLMXDataSource dataSource;

	public BorderContainer() {
		super();
	}

	@Override
	public void init() throws ServletException {
		try {
			DataSourceConfig cfg = Configurator.get(DataSourceConfig.class);
			dataSource = new SQLMXDataSource();
			dataSource.setBlobTableName(cfg.blobTableName);
			dataSource.setCatalog(cfg.catalog);
			dataSource.setSchema(cfg.schema);
			dataSource.setClobTableName(cfg.clobTableName);
			dataSource.setInitialPoolSize(cfg.initialPoolSize);
			dataSource.setMaxIdleTime(cfg.maxIdleTime);
			dataSource.setMaxPoolSize(cfg.maxPoolSize);
			dataSource.setMaxStatements(cfg.maxStatements);
			dataSource.setMinPoolSize(cfg.minPoolSize);
		} catch (SQLException e) {
			throw new ServletException(e);
		}
	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		processHttpRequest(request, response);
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		processHttpRequest(request, response);
	}

	@Override
	public String getServletInfo() {
		return "PPFE Container";
	}

	private PpfeApplication createApplication(String appName) throws Exception {
		LOGGER.trace("Container: ");
		ThreadData threadData = threadLocal.get();
		if (appName == null)
			throw new Exception(String.format("A %s= parameter is required with the HTTP request", APP_KEY));
		PpfeApplication ppfeApp = threadData.getApplications().get(appName);
		if (ppfeApp != null)
			return ppfeApp;
		ContainerConfig config = Configurator.get(ContainerConfig.class);
		Application appConfig = config.getApplications().get(appName);
		if (appConfig == null)
			throw new Exception(String.format("No Application called '%s' has been configured", appName));
		@SuppressWarnings("rawtypes")
		Class appClass = Class.forName(appConfig.getClassName());
		ppfeApp = (PpfeApplication) appClass.newInstance();
		ppfeApp.setContainer(this);
		ppfeApp.setAppName(appName);
		threadData.getAppNameStack().push(appName + ": ");
		threadData.addApplication(appName, ppfeApp);
		return ppfeApp;
	}

	private void processHttpRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException {
		LOGGER.trace(Thread.currentThread().getName());
		ThreadData threadData = threadLocal.get();
		if (threadData == null) {
			threadData = new ThreadData();
			threadLocal.set(threadData);
		}
		threadData.setHttpRequest(request);
		threadData.setHttpResponse(response);
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
			threadData.setHttpRequest(null);
			threadData.setHttpResponse(null);
			threadData.getInputStack().clear();
			threadData.getOutputStack().clear();
		}
	}

	@Override
	public PpfeContainer sendRequest(String destinationName, MyProperties requestParameters,
			PpfeResponse ppfeResponse) {
		ContainerConfig config = Configurator.get(ContainerConfig.class);
		MyProperties responseParameters = ppfeResponse.getData();
		Outcome outcome = ppfeResponse.getOutcome();
		Destination destSelected = config.getDestinations().get(destinationName);
		if (destSelected == null) {
			outcome.setReturnCode(ReturnCode.FAILURE);
			outcome.setMessage(String.format("Destination '%s' was requested but there is no configuration for it",
					destinationName));
			return this;
		}
		try {
			LOGGER.trace("sending to: %s", destSelected.getUri());
			PathwayClient pwClient = getPathwayClient(destSelected);
			pwClient.transceive(0, requestParameters, responseParameters);
			ppfeResponse.setData(responseParameters);
		} catch (Exception e) {
			String err = e.toString();
			if (err.contains("File system error 40"))
				err = "TIMEOUT: " + err;
			LOGGER.error(e, "sending request");
			outcome.setReturnCode(ReturnCode.FAILURE);
			outcome.setMessage(err);
		}
		return this;
	}

	private PathwayClient getPathwayClient(Destination destSelected) throws Exception {
		if (threadPathwayClients.get() == null)
			threadPathwayClients.set(new ArrayList<PathwayClient>());
		for (PathwayClient client : threadPathwayClients.get()) {
			if (client.getServerPath().equalsIgnoreCase(destSelected.getUri())) {
				return client;
			}
		}
		ContainerConfig config = Configurator.get(ContainerConfig.class);
		Application appConfig = config.getApplications().get(destSelected.getApplicationName());
		if (appConfig == null)
			throw new Exception(String.format("destination app '%s' has not been configured", destSelected.getApplicationName()));
		PathwayClient pwClient = null;
		int timeoutCentiSecs = Integer.parseInt(String.valueOf(destSelected.getClientTimeoutMillis())) / 10;
		pwClient = new PathwayClient(destSelected.getUri(), timeoutCentiSecs, destSelected.getLatencyThresholdMillis(),
				appConfig.getMaxRequestSize(), appConfig.getMaxResponseSize());
		threadPathwayClients.get().add(pwClient);
		return pwClient;
	}

	@Override
	public boolean getRequest(PpfeRequest ppfeRequest) {
		ThreadData threadData = threadLocal.get();
		LOGGER.trace(threadData.getAppNameStack().peek());
		HttpServletRequest request = threadData.getHttpRequest();
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
		HttpServletResponse response = threadData.getHttpResponse();
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
