package net.mikaboshi.jdbc;


public abstract class HSQLDBTestCase extends DbTestCase {

	protected String getJdbcPropFileName() {
		return "jdbc_hsqldb_in-memory.properties";
	}
}
