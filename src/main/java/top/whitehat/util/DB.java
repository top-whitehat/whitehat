package top.whitehat.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class DB implements AutoCloseable {

	/** Field information */
	public static class Field {
		public int serial;
		public String name;
		public String type;
		public boolean notNull;
		public boolean primaryKey;
		public boolean isNullable;
		public boolean isAutoIncrement;
		public int size;
	}

	/** Table information */
	public static class Table {
		public List<Field> fields = new ArrayList<DB.Field>();

		public Field field(String name) {
			for (Field f : fields) {
				if (name.equalsIgnoreCase(f.name))
					return f;
			}
			return null;
		}
	}

	// ------- members--------

	protected String url;

	protected Connection connection;

	public DB(String dbPath) throws SQLException {
		open(dbPath);
	}

	public DB open(String url) throws SQLException {
		this.url = url;

		// if the url is a sqlite .db file
		if (url.endsWith(".db") && !url.contains("sqlite:")) { 
			url = "jdbc:sqlite:" + url;
		}

		connection = DriverManager.getConnection(url);
		return this;
	}
	

	/** Get table names */
	public List<String>  getTableNames() throws SQLException {
		List<String> tableNames = new ArrayList<String>();

		// use system table: sqlite_master
		String sql = "SELECT name FROM sqlite_master WHERE type='table' AND name NOT LIKE 'sqlite_%'";
		try (Statement stmt = connection.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
			while (rs.next()) {
				tableNames.add(rs.getString("name"));
			}
		}
		
		return tableNames;
	}

	/**
	 * Get a Table object
	 * 
	 * @throws SQLException
	 */
	public Table table(String tableName) throws SQLException {
		// use PRAGMA table_info to get field information
		String pragmaSQL = "PRAGMA table_info(" + tableName + ")";
		try (Statement stmt = connection.createStatement(); ResultSet rs = stmt.executeQuery(pragmaSQL)) {
			Table result = new Table();
			while (rs.next()) {
				Field field = new Field();
				field.serial = rs.getInt("cid"); // serial number of the field
				field.name = rs.getString("name");
				field.type = rs.getString("type");
				field.notNull = rs.getInt("notnull") == 1;
				field.primaryKey = rs.getInt("pk") == 1;
				result.fields.add(field);
			}
			return result;
		}
	}

	/** close DB */
	public void close() throws SQLException {
		connection.close();
	}

	/** execute SQL query, return DBResultSet object */
	public DBResultSet query(String sql) throws SQLException {
		Statement stmt = connection.createStatement();
		ResultSet rs = stmt.executeQuery(sql);
		return new DBResultSet(rs);
	}

}
