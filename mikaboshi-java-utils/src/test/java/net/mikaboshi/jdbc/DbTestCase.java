package net.mikaboshi.jdbc;


import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import net.mikaboshi.jdbc.DbUtils;
import net.mikaboshi.jdbc.DmlFileExecutor;
import net.mikaboshi.jdbc.QueryExecutor;
import net.mikaboshi.jdbc.count.RecordCountUtils;
import net.mikaboshi.jdbc.schema.SchemaUtils;

import org.apache.commons.io.IOUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;

public abstract class DbTestCase {
	
	@BeforeClass
	public static void beforeClass() {
		writer = new PrintWriter(System.out);
	}
	
	public static void afterClass() {
		IOUtils.closeQuietly(writer);
	}
	
	@Before
	public void setUp() throws SQLException, IOException, ClassNotFoundException {
		this.conn = DbUtils.getConnection(getTestPath(getJdbcPropFileName()));
		this.conn.setAutoCommit(false);
		
		if (!SchemaUtils.existsTable(
				getConnection().getMetaData(),
				null, null, "SAMPLE_TAB1")) {
			
			DmlFileExecutor.execute(
					this.conn,
					getTestFile("create_test_tables.sql"),
					"UTF-8",
					";",
					true);
			
			this.conn.commit();
			
			// create tableした場合は、一旦closeする必要がある
			DbUtils.closeQuietly(this.conn);
			
			this.conn = DbUtils.getConnection(getTestPath(getJdbcPropFileName()));
			this.conn.setAutoCommit(false);
			
		} else {
			DmlFileExecutor.execute(
					this.conn,
					getTestFile("truncate_test_tables.sql"),
					"UTF-8",
					";",
					true);
		}
		
		DmlFileExecutor.execute(
				this.conn,
				getTestFile("insert_test_records.sql"),
				"UTF-8",
				";",
				true);
		
		this.conn.commit();
	}
	
	@After
	public void tearDown() {
		getWriter().flush();
		DbUtils.rollbackQuietly(this.conn);
		DbUtils.closeQuietly(this.conn);
	}
	
	public static String getTestPath(String fileName) {
		return "src/test/resources/" 
			+ DbTestCase.class.getPackage().getName().replace('.', '/')
			+ "/" + fileName;
	}
	
	public static File getTestFile(String fileName) {
		return new File(getTestPath(fileName));
	}
	
	protected abstract String getJdbcPropFileName();
	
	private static PrintWriter writer;
	
	protected PrintWriter getWriter() {
		return writer;
	}
	
	private Connection conn;
	
	protected Connection getConnection() {
		return this.conn;
	}
	
	protected int getRecordCount(String tableName) throws SQLException {
		return RecordCountUtils.getAllRecordCount(getConnection(), tableName);
	}
	
	protected List<Map<String, Object>> getAllRecords(String tableName) throws SQLException {
		return QueryExecutor.query(getConnection(), "select * from " + tableName);
	}
}
