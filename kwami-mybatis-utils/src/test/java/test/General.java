package test;

import net.kwami.mybatis.Database;
import net.kwami.mybatis.Databases;

public class General {

	public static void main(String[] args) throws Exception {
		Databases dbs = new Databases();
		dbs.add(new Database("FRAMEWORK", "FrameworkDs.js", "FrameworkSqlSessionFactory.js"));
		dbs.add(new Database("FRS", "FrsDs.js", "FrsSqlSessionFactory.js"));
		dbs.add(new Database("SRS", "SrsDs.js", null));
		System.out.println(dbs.toString());
	}
	
}
