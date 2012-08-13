package net.mikaboshi.jdbc.count;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.sql.SQLException;

import net.mikaboshi.jdbc.HSQLDBTestCase;
import net.mikaboshi.jdbc.QueryExecutor;

import org.junit.Test;

public class CountResultSetHandlerTest extends HSQLDBTestCase {

	@Test
	public void testGetCount() throws SQLException {
		CountResultSetHandler counter = new CountResultSetHandler();
		
		QueryExecutor exec = new QueryExecutor(getConnection(), counter);
		
		exec.execute("select count(*) from EMP");
		
		assertEquals(14, counter.getCount());
	}
	
	@Test(expected = IllegalStateException.class)
	public void testGetCountBeforeExecute() {
		CountResultSetHandler counter = new CountResultSetHandler();
		counter.getCount();
		fail();
	}
}
