package net.mikaboshi.jdbc;

import java.sql.SQLException;

import net.mikaboshi.csv.StandardCSVStrategy;
import net.mikaboshi.jdbc.QueryExecutor;
import net.mikaboshi.jdbc.ResultSetHandler;
import net.mikaboshi.jdbc.ResultSetToCSVHandler;
import net.mikaboshi.jdbc.SimpleFormatter;

import org.junit.Test;



public class ResultSetToCSVHandlerTest extends HSQLDBTestCase {

	@Test
	public void testSimpple() throws SQLException {
		ResultSetHandler handler =
			new ResultSetToCSVHandler(
					getWriter(),
					false,
					false,
					false,
					new SimpleFormatter(),
					new StandardCSVStrategy());
		
		QueryExecutor queryExec = new QueryExecutor(getConnection(), handler);
		queryExec.execute("select * from EMP");
	}

	@Test
	public void testVerbose() throws SQLException {
		ResultSetHandler handler =
			new ResultSetToCSVHandler(
					getWriter(),
					true,
					true,
					true,
					new SimpleFormatter(),
					new StandardCSVStrategy());
		
		QueryExecutor queryExec = new QueryExecutor(getConnection(), handler);
		queryExec.execute("select * from EMP");
	}
}
