package net.kwami.mybatis;


public class Database {
	private String shortName;
	private String dataSourceFileName;
	private String sqlSessionFactoryFileName;
	
	public Database() {
		super();
	}

	public Database(String shortName, String dataSourceFileName, String sqlSessionFactoryFileName) {
		super();
		this.shortName = shortName;
		this.dataSourceFileName = dataSourceFileName;
		this.sqlSessionFactoryFileName = sqlSessionFactoryFileName;
	}

	public String getDsResourceName() {
		return "/" + dataSourceFileName;
	}

	public String getFactoryResourceName() {
		if (sqlSessionFactoryFileName == null)
			return null;
		return "/" + sqlSessionFactoryFileName;
	}

	
	public String getShortName() {
		return shortName;
	}

	
	public void setShortName(String shortName) {
		this.shortName = shortName;
	}

	public String getDataSourceFileName() {
		return dataSourceFileName;
	}
	
	public void setDataSourceFileName(String dataSourceFileName) {
		this.dataSourceFileName = dataSourceFileName;
	}
	
	public String getSqlSessionFactoryFileName() {
		return sqlSessionFactoryFileName;
	}
	
	public void setSqlSessionFactoryFileName(String sqlSessionFactoryFileName) {
		this.sqlSessionFactoryFileName = sqlSessionFactoryFileName;
	}
}
