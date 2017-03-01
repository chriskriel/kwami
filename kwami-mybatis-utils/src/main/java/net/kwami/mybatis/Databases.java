package net.kwami.mybatis;

import java.util.HashSet;
import java.util.Set;

import com.google.gson.GsonBuilder;

public class Databases {

	private Set<Database> databases;
	
	public void add(Database config) {
		if (databases == null)
			databases = new HashSet<Database>();
		databases.add(config);
	}
 
	public Set<Database> getDatabases() {
		return databases;
	}

	public void setDatabases(Set<Database> databases) {
		this.databases = databases;
	}

	@Override
	public String toString() {
		return new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create().toJson(this);
	}
}
