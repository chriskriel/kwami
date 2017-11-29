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

	public SqlInterpreter() throws Exception {
		super();
	}

	@Override
	public void run() {
		ds = getContainer().getDataSource();
		PpfeResponse ppfeResponse = new PpfeResponse();
		PpfeRequest ppfeRequest;
		logger.info("Getting PpfeRequests");
		while ((ppfeRequest = getContainer().getRequest()) != null) {
			try {
				ppfeResponse = process(ppfeRequest);
			} catch (Exception e) {
				ppfeResponse.getOutcome().setReturnCode(ReturnCode.FAILURE);
				ppfeResponse.getOutcome().setMessage(e.toString());
				logger.error(e, e.toString());
			}
			Outcome outcome = getContainer().sendReply(ppfeRequest.getContext(), ppfeResponse.getData());
			String msg = "Outcome of sendReply(): %s";
			if (outcome.getReturnCode() == ReturnCode.SUCCESS) {
				logger.trace(msg, outcome.toString());
			} else {
				logger.error(msg, outcome.toString());
			}
		}
	}

	public PpfeResponse process(PpfeRequest ppfeRequest) throws Exception {
		logger.debug("Request data: %s", ppfeRequest.getData().toString());
		PpfeResponse ppfeResponse = new PpfeResponse();
		MyProperties requestData = ppfeRequest.getData();
		String sqlStatement = requestData.getProperty("SQL", "");
		try (Connection conn = ds.getConnection(); PreparedStatement ps = conn.prepareStatement(sqlStatement)) {
			int sqlParameterCount = requestData.getIntProperty("PARM-CNT", 0);
			for (int i = 0; i < sqlParameterCount; i++) {
				String key = "P" + String.valueOf(i);
				ps.setString(i + 1, requestData.getProperty(key));
			}
			ResultSet rs = ps.executeQuery();
			ResultSetMetaData meta = rs.getMetaData();
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
			logger.debug("Response data: %s", ppfeResponse.getData().toString());
		}
		return ppfeResponse;
	}
}
