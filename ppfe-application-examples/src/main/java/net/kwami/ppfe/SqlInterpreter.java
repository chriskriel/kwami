package net.kwami.ppfe;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.List;

import org.apache.tomcat.jdbc.pool.DataSource;

import net.kwami.utils.MyLogger;
import net.kwami.utils.MyProperties;

public class SqlInterpreter extends PpfeApplication {
	private static final MyLogger logger = new MyLogger(SqlInterpreter.class);
	private DataSource ds;
	private PpfeResponse ppfeResponse = new PpfeResponse();
	private PpfeRequest ppfeRequest = new PpfeRequest();
	private Outcome outcome = new Outcome();

	public SqlInterpreter() throws Exception {
		super();
	}

	@Override
	public void run() {
		PpfeContainer container = getContainer();
		ds = container.getDataSource();
		while (container.getRequest(ppfeRequest)) {
			try {
				ppfeResponse = process(ppfeRequest);
			} catch (Exception e) {
				ppfeResponse.getOutcome().setReturnCode(ReturnCode.FAILURE);
				ppfeResponse.getOutcome().setMessage(e.toString());
				logger.error(e, e.toString());
			}
			container.sendReply(ppfeRequest.getContext(), ppfeResponse.getData(), outcome);
			String msg = "Outcome of sendReply(): %s";
			if (outcome.getReturnCode() == ReturnCode.SUCCESS) {
				logger.trace(msg, outcome.toString());
			} else {
				logger.error(msg, outcome.toString());
			}
			container.getRequest(ppfeRequest);
		}
	}

	public PpfeResponse process(PpfeRequest ppfeRequest) throws Exception {
		logger.trace("Request data: %s", ppfeRequest.getData().toString());
		ppfeResponse.clear();
		MyProperties requestData = ppfeRequest.getData();
		String sqlStatement = requestData.getProperty("SQL", "");
		Connection conn = ds.getConnection();
		PreparedStatement ps = conn.prepareStatement(sqlStatement);
		try {
			int sqlParameterCount = requestData.getIntProperty("PARM-CNT", 0);
			for (int i = 0; i < sqlParameterCount; i++) {
				String key = "P" + String.valueOf(i);
				ps.setString(i + 1, requestData.getProperty(key));
			}
			long before = System.currentTimeMillis();
			ResultSet rs = ps.executeQuery();
			ResultSetMetaData meta = rs.getMetaData();
			long latency = System.currentTimeMillis() - before;
			logger.debug("SQL/MX latency: %dms", latency);
			SqlResult sqlResult = new SqlResult();
			for (int i = 1; i <= meta.getColumnCount(); i++) {
				sqlResult.addColumn(meta.getColumnName(i));
			}
			while (rs.next()) {
				List<String> row = sqlResult.addRow();
				for (int i = 1; i <= meta.getColumnCount(); i++) {
					row.add(rs.getString(i));
				}
			}
			ppfeResponse.getData().setProperty("SQL-RESULT", sqlResult.toString());
			ppfeResponse.getData().setProperty("RETURN_CODE", "0");
			logger.trace("Response data: %s", ppfeResponse.getData().toString());
		} finally {
			ps.close();
			conn.close();
		}
		return ppfeResponse;
	}
}
