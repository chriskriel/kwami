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
	private static MyLogger logger = new MyLogger(SqlInterpreter.class);
	private DataSource ds;
	PpfeMessage message;

	public SqlInterpreter() throws Exception {
		super();
		ds = getContainer().getDataSource();
	}

	@Override
	public void run() {
		logger.info("Going to process messages");
		while ((message = getContainer().getRequest()) != null) {
			try {
				process(message);
			} catch (Exception e) {
				message.setData(null);
				message.getOutcome().setReturnCode(ReturnCode.FAILURE);
				message.getOutcome().setMessage(e.toString());
				logger.error(e, e.toString());
			}
			Outcome outcome = getContainer().sendReply(message);
			String msg = "Outcome on sending reply: %s";
			if (outcome.getReturnCode() == ReturnCode.SUCCESS) {
				logger.debug(msg, outcome.toString());
			} else {
				logger.error(msg, outcome.toString());
			}
		}
	}

	public void process(PpfeMessage message) {
		String sqlStatement = "";
		MyProperties data = message.getData();
		sqlStatement = data.getProperty("SQL");
		try (Connection conn = ds.getConnection(); PreparedStatement ps = conn.prepareStatement(sqlStatement)) {
			int sqlParameterCount = data.getIntProperty("PARM-CNT", 0);
			for (int i = 0; i <= sqlParameterCount; i++) {
				String key = "P" + i;
				ps.setString(i, data.getProperty(key));
			}
			data = new MyProperties();
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
			data.setProperty("SQL-RESULT", sqlResult.toString());
			data.setProperty("RETURN_CODE", "0");
			return;
		} catch (Exception e) {
			logger.error(e, e.toString());
		}
	}
}
