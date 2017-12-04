package net.kwami.ppfe;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
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

	private static final String PPFE_END = "PPFE_END";
	private static final String PPFE_APP = "ppfeApp";
	private static final String PPFE_PARM = "PPFE_JSON";
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
		threadApplications.set(new ArrayList<PpfeApplication>());
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
		String appName = threadServletRequest.get().getParameter(PPFE_APP);
		if (appName == null) 
			throw new Exception(String.format("A %s= parameter is required with the HTTP request", PPFE_APP));
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
		for (PpfeApplication threadApp:threadApplications.get()) {
			if (threadApp.getAppName().equals(appConfig.getName()))
				return threadApp;
		}
		@SuppressWarnings("rawtypes")
		Class appClass = Class.forName(appConfig.getClassName());
		PpfeApplication ppfeApp = (PpfeApplication)appClass.newInstance();
		ppfeApp.setContainer(this);
		ppfeApp.setAppName(appConfig.getName());
		threadApplications.get().add(ppfeApp);
		return ppfeApp;
	}

	private PpfeRequest preparePpfeMessage(HttpServletRequest request, HttpServletResponse response) throws Exception {
		PpfeRequest ppfeRequest = new PpfeRequest();
		Gson gson = new GsonBuilder().create();
		String jsonData = request.getAttribute(PPFE_PARM).toString().trim();
		MyProperties requestData;
		if (jsonData == null) {
			jsonData = request.getParameter(PPFE_PARM).toString().trim();
		}
		requestData = gson.fromJson(jsonData, MyProperties.class);
		ppfeRequest.setData(requestData);
		return ppfeRequest;
	}

	private void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException {
		threadServletRequest.set(request);
		threadServletResponse.set(response);
		try {
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
				responseStr = request.getAttribute(PPFE_PARM).toString();
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
		if (request.getAttribute(PPFE_END) != null)
			return false;
		request.setAttribute(PPFE_END, "end");
		try {
			msg = preparePpfeMessage(request, response);
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
			if (request.getAttribute(PPFE_PARM) != null) {
				request.setAttribute(PPFE_PARM, body);
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
