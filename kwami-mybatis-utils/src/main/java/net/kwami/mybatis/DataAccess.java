package net.kwami.mybatis;

import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;

import net.kwami.mybatis.SqlSessionFactoryHome;
import net.kwami.utils.Configurator;

public abstract class DataAccess {

	private static final Map<String, SqlSessionFactory> sessionFactories = new HashMap<String, SqlSessionFactory>();
	private static final Map<String, DataSource> datasources = new HashMap<String, DataSource>();
	private static final Map<String, ThreadLocal<SqlSession>> sessions = new HashMap<String, ThreadLocal<SqlSession>>();
	static {
		Databases databases = Configurator.get(Databases.class);
		for (Database db : databases.getDatabases()) {
			MyPooledDataSource ds = new MyPooledDataSource(db.getDsResourceName());
			datasources.put(db.getShortName(), ds);
			if (db.getFactoryResourceName() != null) {
				SqlSessionFactoryHome sc = Configurator.get(SqlSessionFactoryHome.class,
						db.getFactoryResourceName());
				SqlSessionFactory sessionFactory = sc.get(ds);
				sessionFactories.put(db.getShortName(), sessionFactory);
				ThreadLocal<SqlSession> localSession = new ThreadLocal<SqlSession>();
				localSession.set(null);
				sessions.put(db.getShortName(), localSession);
			}
		}
	}

	public static DataSource getDataSource(String shortName) {
		return datasources.get(shortName);
	}

	public static void openSession(String shortName, boolean autoCommit) {
		SqlSession session = sessionFactories.get(shortName).openSession(autoCommit);
		sessions.get(shortName).set(session);
	}

	public static void closeSession(String shortName) {
		ThreadLocal<SqlSession> localSession = sessions.get(shortName);
		localSession.get().close();
		localSession.set(null);
	}

	public static SqlSession getSession(String shortName) {
		ThreadLocal<SqlSession> localSession = sessions.get(shortName);
		if (localSession.get() == null) {
			openSession(shortName, false);
		}
		return localSession.get();
	}
}
