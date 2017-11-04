package net.kwami.ppfe;

import java.io.UnsupportedEncodingException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.List;

import org.apache.tomcat.jdbc.pool.DataSource;

import net.kwami.utils.MyLogger;
import net.kwami.utils.ParameterBuffer;

public class SqlInterpreter extends PpfeApplication {
	private static final short MSG_ID = 0;
	private static MyLogger logger = new MyLogger(SqlInterpreter.class);
	private DataSource ds;

	public SqlInterpreter(PpfeContainer server) throws Exception {
		super(server);
		ds = server.getDataSource();
	}

	public void process(PpfeMessage message) {
		String sqlStatement = "";
		ParameterBuffer data = message.getData();
		try {
			sqlStatement = data.getStringValue("SQL");
		} catch (UnsupportedEncodingException e1) {
		}
		try (Connection conn = ds.getConnection(); PreparedStatement ps = conn.prepareStatement(sqlStatement)) {
			int sqlParameterCount = data.getIntValue("PARM-CNT");
			for (int i = 0; i <= sqlParameterCount; i++) {
				String key = "P" + i;
				ps.setString(i, data.getStringValue(key));
			}
			data.initialize(MSG_ID);
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
			data.addParameter("SQL-RESULT", sqlResult.toString(), false);
			data.addParameter("RETURN_CODE", 0);
			return;
		} catch (Exception e) {
			logger.error(e, e.toString());
		}
	}
}
