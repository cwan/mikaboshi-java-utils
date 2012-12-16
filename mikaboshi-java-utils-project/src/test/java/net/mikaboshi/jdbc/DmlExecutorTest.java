package net.mikaboshi.jdbc;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

import net.mikaboshi.jdbc.DbUtils;
import net.mikaboshi.jdbc.DmlFileExecutor;

import org.junit.Test;

public class DmlExecutorTest {

	@Test
	public void testExecuteFile() throws SQLException, IOException, ClassNotFoundException {
		
		Connection conn = null;
		
		try {
			conn = DbUtils.getConnection(DbTestCase.getTestPath("jdbc_hsqldb_in-memory.properties"));
		
			File testFile1 = DbTestCase.getTestFile("truncate_test_tables.sql"); 
			DmlFileExecutor.execute(conn, testFile1, "UTF-8", ";", true);
			
			File testFile2 = DbTestCase.getTestFile("insert_test_records.sql"); 
			DmlFileExecutor.execute(conn, testFile2, "UTF-8", ";", true);

		} finally {
			DbUtils.closeQuietly(conn);
		}
	}
}
