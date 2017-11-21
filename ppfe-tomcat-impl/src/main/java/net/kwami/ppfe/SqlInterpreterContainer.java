package net.kwami.ppfe;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Enumeration;
import java.util.Properties;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.tomcat.jdbc.pool.DataSource;
import org.apache.tomcat.jdbc.pool.PoolProperties;
import net.kwami.utils.Configurator;
import net.kwami.utils.MyLogger;
import net.kwami.utils.ParameterBuffer;

public class SqlInterpreterContainer extends HttpServlet implements PpfeContainer {

	private static class Context {
		@SuppressWarnings("unused")
		HttpServletRequest request;
		@SuppressWarnings("unused")
		HttpServletResponse response;
	}

	private static final long serialVersionUID = 1L;
	private static final short SHORT0 = 0;
	private static final MyLogger LOGGER = new MyLogger(SqlInterpreterContainer.class);
	private DataSource dataSource;

	public SqlInterpreterContainer() {
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

	@Override
	public PpfeApplication createApplication(PpfeMessage firstRequest) throws Exception {
		return new SqlInterpreter(this, firstRequest);
	}

	private PpfeMessage preparePpfeMessage(HttpServletRequest request, HttpServletResponse response) throws Exception {
		PpfeMessage message = new PpfeMessage();
		ParameterBuffer ppfeInput = new ParameterBuffer(SHORT0);
		message.setData(ppfeInput);
		String s = null;
		Enumeration<?> nameObjects = request.getParameterNames();
		while (nameObjects.hasMoreElements()) {
			String name = nameObjects.nextElement().toString();
			s = request.getParameter(name);
			if (s != null && !s.equals(""))
				ppfeInput.addParameter(name, s, true);
		}
		Context context = new Context();
		context.request = request;
		context.response = response;
		message.setContext(context);
		return message;
	}

	private void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException {
		try {
			PpfeMessage firstRequest = preparePpfeMessage(request, response);
			createApplication(firstRequest).run();
		} catch (Exception e) {
			LOGGER.error(e, e.toString());
			throw new ServletException(e);
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
		String postData;
		PpfeMessage response = new PpfeMessage();
		Outcome outcome = response.getOutcome();
		ParameterBuffer data = null;
		String scheme;
		String hostName;
		int port;
		String uri;
		int start, end;
		StringBuilder sb = new StringBuilder(destination);
		try {
			postData = buildHtmlBody(message.getData());
			String s = destination.toLowerCase();
			if (s.startsWith("http")) {
				end = sb.indexOf(":");
				scheme = sb.substring(0, end);
				start = end + 3;  // skip '://' to get host-name
				end = sb.indexOf(":", start);
				hostName = sb.substring(start, end);
				start = end + 1; // skip ':' before port
				end = sb.indexOf("/", start);
				port = Integer.parseInt(sb.substring(start, end));
				uri = sb.substring(end);
				HttpClient httpClient = new HttpClient(scheme, hostName, port);
				httpClient.post(uri, postData);
			} else {
				// build parameters to forward to another servlet
			}
			response.setData(data);
		} catch (Exception e) {
			outcome.setReturnCode(ReturnCode.FAILURE);
			outcome.setMessage(e.toString());
		}
		return response;
	}

	@Override
	public synchronized PpfeMessage getRequest(int maxBuf) {
		return null;
	}

	@Override
	public Outcome sendReply(PpfeMessage message) {
		Context context = (Context) message.getContext();
		Outcome outcome = new Outcome(ReturnCode.SUCCESS, "replied with '%s'");
		try {
			String body = buildHtmlBody(message.getData());
			LOGGER.debug("about to reply with '%s'", body);
			writeHttpResponse(context.response, body);
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

	private String buildHtmlBody(ParameterBuffer ppfeOutput) throws IOException {
		StringBuilder outStrBuf = new StringBuilder();
		boolean firstTime = true;
		for (String key : ppfeOutput.keySet()) {
			if (firstTime) {
				firstTime = false;
			} else {
				outStrBuf.append('&');
			}
			String value = ppfeOutput.getStringValue(key);
			outStrBuf.append(key).append('=').append(value);
		}
		return outStrBuf.toString();
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
