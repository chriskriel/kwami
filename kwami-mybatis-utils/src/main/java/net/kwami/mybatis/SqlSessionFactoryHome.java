package net.kwami.mybatis;

import javax.sql.DataSource;

import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;

import com.google.gson.GsonBuilder;

import net.kwami.utils.MyLogger;

public class SqlSessionFactoryHome {

	private static final MyLogger logger = new MyLogger(SqlSessionFactoryHome.class);
	private String environmentName = "development";
	private String mappersPackage = "net.kwami.mybatis";
	private String[] packageNamesToAlias = { "net.kwami.mybatis" };

	public SqlSessionFactoryHome() {
	}

	public SqlSessionFactory get(DataSource ds) {
		Environment environment = new Environment(environmentName, new JdbcTransactionFactory(), ds);
		Configuration configuration = new Configuration(environment);
		configuration.setMapUnderscoreToCamelCase(true);
		for (String packageName : packageNamesToAlias) {
			logger.debug("packageName=%s", packageName);
			configuration.getTypeAliasRegistry().registerAliases(packageName);
		}
		configuration.addMappers(mappersPackage);
		return new SqlSessionFactoryBuilder().build(configuration);
	}

	public String getEnvironmentName() {
		return environmentName;
	}

	public void setEnvironmentName(String environmentName) {
		this.environmentName = environmentName;
	}

	public String getMappersPackage() {
		return mappersPackage;
	}

	public void setMappersPackage(String mappersPackage) {
		this.mappersPackage = mappersPackage;
	}

	public String[] getClassNamesToAlias() {
		return packageNamesToAlias;
	}

	public void setClassNamesToAlias(String[] classNamesToAlias) {
		this.packageNamesToAlias = classNamesToAlias;
	}

	@Override
	public String toString() {
		return new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create().toJson(this);
	}
}
