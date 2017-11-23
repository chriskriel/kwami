package net.kwami.ppfe;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Properties;

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
	private static ThreadLocal<HttpServletRequest> threadServletRequest = new ThreadLocal<>();
	private static ThreadLocal<HttpServletResponse> threadServletResponse = new ThreadLocal<>();
	private DataSource dataSource;

	public TomcatContainer() {
		super();
	}

	@Override
	public void init() throws ServletException {
		prepareDataSource();
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
		return "SQL Interpreter Container";
	}

	public PpfeApplication createApplication() throws Exception {
		String appName = threadServletRequest.get().getParameter(PPFE_APP);
		if (appName == null) 
			throw new Exception(String.format("A %s= parameter is required with the HTTP request", PPFE_APP));
		ContainerConfig config = Configurator.get(ContainerConfig.class);
		Application thisApp = null;
		for (Application app : config.getApplications()) {
			if (app.getName().equals(appName)) {
				thisApp = app;
				break;
			}
		}
		if (thisApp == null)
			throw new Exception(String.format("No Application called '%s' has been configured", appName));
		@SuppressWarnings("rawtypes")
		Class appClass = Class.forName(thisApp.getClassName());
		PpfeApplication ppfeApp = (PpfeApplication)appClass.newInstance();
		ppfeApp.setContainer(this);
		return ppfeApp;
	}

	private PpfeMessage preparePpfeMessage(HttpServletRequest request, HttpServletResponse response) throws Exception {
		PpfeMessage message = new PpfeMessage();
		Gson gson = new GsonBuilder().create();
		String jsonData = request.getAttribute(PPFE_PARM).toString().trim();
		MyProperties requestData;
		if (jsonData == null) {
			jsonData = request.getParameter(PPFE_PARM).toString().trim();
		}
		requestData = gson.fromJson(jsonData, MyProperties.class);
		message.setData(requestData);
		return message;
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

	private void prepareDataSource() {
		Properties properties = Configurator.get(Properties.class);
		String schemaName = properties.getProperty("schemaName", "Dev.Rms");
		String resourceName = String.format("/%s.js", schemaName);
		LOGGER.info("configuring schema from the file %s", resourceName);
		PoolProperties pp = Configurator.get(PoolProperties.class, resourceName);
		DataSource ds = new DataSource();
		ds.setPoolProperties(pp);
		this.dataSource = ds;
	}

	@Override
	public PpfeMessage sendRequest(String destination, PpfeMessage message, long timeoutMillis) {
		HttpServletRequest request = threadServletRequest.get();
		HttpServletResponse response = threadServletResponse.get();
		PpfeMessage responseMsg = new PpfeMessage(message);
		Outcome outcome = responseMsg.getOutcome();
		responseMsg.setData(null);
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
			responseMsg.setData(null);
			return responseMsg;
		}
		String responseStr = null;
		try {
			if (destSelected.getRemote() != null) {
				HttpClient httpClient = new HttpClient(destSelected.getRemote().getScheme(),
						destSelected.getRemote().getHostName(), destSelected.getRemote().getPort());
				responseStr = httpClient.post(destSelected.getUri(), message.getData().toString());
			} else {
				threadServletRequest.get().getRequestDispatcher(destSelected.getUri()).include(request, response);
				responseStr = request.getAttribute(PPFE_PARM).toString();
			}
			Gson gson = new GsonBuilder().create();
			MyProperties props = gson.fromJson(responseStr, MyProperties.class);
			responseMsg.setData(props);
		} catch (Exception e) {
			outcome.setReturnCode(ReturnCode.FAILURE);
			outcome.setMessage(e.toString());
			responseMsg.setData(null);
		}
		return responseMsg;
	}

	@Override
	public synchronized PpfeMessage getRequest() {
		HttpServletRequest request = threadServletRequest.get();
		HttpServletResponse response = threadServletResponse.get();
		PpfeMessage msg = new PpfeMessage();		
		if (request.getAttribute(PPFE_END) != null)
			return null;
		request.setAttribute(PPFE_END, "X");
		try {
			msg = preparePpfeMessage(request, response);
		} catch (Exception e) {
			LOGGER.error(e, "unable to get request");
			Outcome outcome = msg.getOutcome();
			outcome.setReturnCode(ReturnCode.FAILURE);
			outcome.setMessage(e.toString());
			msg.setData(null);
		}
		return msg;
	}

	@Override
	public Outcome sendReply(PpfeMessage message) {
		HttpServletRequest request = threadServletRequest.get();
		HttpServletResponse response = threadServletResponse.get();
		Outcome outcome = new Outcome(ReturnCode.SUCCESS, "replied with '%s'");
		try {
			String body = message.getData().toString();
			LOGGER.debug("about to reply with '%s'", body);
			if (request.getAttribute(PPFE_PARM) != null) {
				request.setAttribute(PPFE_PARM, body);
			} else {
				writeHttpResponse(response, body);
			}
			outcome.setReturnCode(ReturnCode.SUCCESS);
			outcome.setMessage(String.format(outcome.getMessage(), body));
		} catch (Exception ex) {
			outcome.setReturnCode(ReturnCode.FAILURE);
			outcome.setMessage(ex.toString());
		}
		return outcome;
	}

	@Override
	public DataSource getDataSource() {
		return dataSource;
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
}
