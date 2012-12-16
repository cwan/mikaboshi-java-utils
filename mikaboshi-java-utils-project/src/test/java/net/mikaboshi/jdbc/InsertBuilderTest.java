package net.mikaboshi.jdbc;

import java.sql.SQLException;

import net.mikaboshi.jdbc.InsertBuilder;
import net.mikaboshi.jdbc.QueryExecutor;

import org.junit.Test;

public class InsertBuilderTest extends HSQLDBTestCase {

	@Test
	public void test1() throws SQLException {
		InsertBuilder insertBuilder = new InsertBuilder(getWriter(), "hoge_piyo", true);
		
		new QueryExecutor(getConnection(), insertBuilder)
			.execute("select count(*) as table_num from EMP;");
	}
}
