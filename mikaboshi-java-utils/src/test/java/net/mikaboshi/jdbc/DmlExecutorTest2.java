package net.mikaboshi.jdbc;

import static org.junit.Assert.assertEquals;

import java.sql.SQLException;

import net.mikaboshi.jdbc.DmlExecutor;

import org.junit.Test;

public class DmlExecutorTest2 extends HSQLDBTestCase {

	@Test
	public void testExecute() throws SQLException {
		int before = getRecordCount("SAMPLE_TAB1"); 
			
		DmlExecutor.execute(getConnection(),
				"INSERT INTO SAMPLE_TAB1 VALUES (?, ?)",
				new Object[] {4, "testExecute"});
		
		int after = getRecordCount("SAMPLE_TAB1");
		
		assertEquals(before + 1, after);
	}

}
