package net.kwami.ppfe;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.gson.GsonBuilder;

public class SqlResult {
	private Set<String> columnNames = new HashSet<String>();
	private List<List<String>> rows = new ArrayList<List<String>>();

	public SqlResult() {
		super();
	}

	public void addColumn(String column) {
		columnNames.add(column);
	}

	public List<String> addRow() {
		List<String> row = new ArrayList<String>();
		rows.add(row);
		return row;
	}

	public Set<String> getColumnNames() {
		return columnNames;
	}

	public void setColumnNames(Set<String> columnNames) {
		this.columnNames = columnNames;
	}

	public List<List<String>> getRows() {
		return rows;
	}

	public void setRows(List<List<String>> rows) {
		this.rows = rows;
	}

	@Override
	public String toString() {
		return new GsonBuilder().create().toJson(this);
	}
}
