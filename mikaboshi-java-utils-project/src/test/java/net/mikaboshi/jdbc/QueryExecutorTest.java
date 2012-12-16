package net.mikaboshi.jdbc;

import static org.junit.Assert.*;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import net.mikaboshi.jdbc.QueryExecutor;
import net.mikaboshi.jdbc.ResultSetToMapListHandler;

import org.junit.Test;

@SuppressWarnings("boxing")
public class QueryExecutorTest extends HSQLDBTestCase {

	@Test
	public void testExecutePreparedStatement() throws SQLException {
		
		String sql = "select * from SAMPLE_TAB1 where id < ?";
		Object[] params = new Object[] {new Integer(3)};
		
		ResultSetToMapListHandler handler = new ResultSetToMapListHandler();
		
		new QueryExecutor(getConnection(), handler).execute(sql, params);
		
		List<Map<String, Object>> resultList = handler.getResultList();
		assertEquals(2, resultList.size());
		
		assertEquals(1, resultList.get(0).get("id"));
		assertEquals("AAA", resultList.get(0).get("name"));
		
		assertEquals(2, resultList.get(1).get("id"));
		assertEquals("あああ", resultList.get(1).get("name"));
	}
	
	@Test
	public void testExecutePreparedStatement_NullParam() throws SQLException {
		
		String sql = "select * from EMP where MGR = ?";
		Object[] params = new Object[] {null};
		
		ResultSetToMapListHandler handler = new ResultSetToMapListHandler();
		
		new QueryExecutor(getConnection(), handler).execute(sql, params);
		
		List<Map<String, Object>> resultList = handler.getResultList();
		assertEquals(0, resultList.size());
	}
	
	@Test
	public void testQueryWithoutParams() throws SQLException {
		List<Map<String, Object>> resultList =
			QueryExecutor.query(getConnection(),
				"select * from SAMPLE_TAB1 order by id");
		
		assertEquals(3, resultList.size());
	}

	@Test
	public void testQueryWithParams() throws SQLException {
		List<Map<String, Object>> resultList =
			QueryExecutor.query(getConnection(),
				"select * from SAMPLE_TAB1 where id = ?",
				new Object[] {2});
		
		assertEquals(1, resultList.size());
		assertEquals(2, resultList.get(0).get("ID"));
		assertEquals("あああ", resultList.get(0).get("name"));
	}
}
