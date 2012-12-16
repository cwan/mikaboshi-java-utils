package net.mikaboshi.jdbc;

import static org.junit.Assert.*;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import net.mikaboshi.jdbc.QueryExecutor;
import net.mikaboshi.jdbc.ResultSetToMapListHandler;

import org.junit.Test;

@SuppressWarnings("boxing")
public class ResultSetToMapListHandlerTest extends HSQLDBTestCase {

	@Test
	public void testGetResultList() throws SQLException {
		
		String sql = "select * from SAMPLE_TAB1 where id < ?";
		Object[] params = new Object[] {new Integer(3)};
		
		ResultSetToMapListHandler handler = new ResultSetToMapListHandler();
		
		new QueryExecutor(getConnection(), handler).execute(sql, params);
		
		List<Map<String, Object>> resultList = handler.getResultList();
		assertEquals(2, resultList.size());
		
		String[] columnNames = handler.getColumnNames();
		assertEquals(2, columnNames.length);
		assertTrue("id".equalsIgnoreCase(columnNames[0]));
		assertTrue("name".equalsIgnoreCase(columnNames[1]));
	}
}
