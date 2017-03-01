package net.kwami.mybatis;

import java.sql.Connection;
import java.sql.SQLException;

import org.apache.tomcat.jdbc.pool.DataSource;
import org.apache.tomcat.jdbc.pool.PoolProperties;

import net.kwami.utils.Configurator;

public class MyPooledDataSource extends DataSource {

	public MyPooledDataSource() {
		super();
	}

	public MyPooledDataSource(String resourceFile) {
		super();
		PoolProperties pp = Configurator.get(PoolProperties.class, resourceFile);
		setPoolProperties(pp);
	}
	
	public void returnConnection(Connection connection) {
		try {
			if (connection != null)
				connection.close();
		} catch (SQLException e) {
		}
	}
}
