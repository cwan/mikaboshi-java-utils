package net.mikaboshi.jdbc.schema;

import java.sql.SQLException;

import net.mikaboshi.csv.StandardCSVStrategy;
import net.mikaboshi.jdbc.HSQLDBTestCase;
import net.mikaboshi.jdbc.ResultSetHandler;
import net.mikaboshi.jdbc.ResultSetToCSVHandler;
import net.mikaboshi.jdbc.SimpleFormatter;

import org.junit.Test;

public class MetadataWriterTest extends HSQLDBTestCase {

	@Test
	public void test1() throws SQLException {
		ResultSetHandler handler = 
			new ResultSetToCSVHandler(
					getWriter(),
					true,
					true,
					true,
					new SimpleFormatter(),
					new StandardCSVStrategy());
		
		MetadataWriter metadataWriter = 
			new MetadataWriter(getConnection(), handler);
		
		metadataWriter.doWrite(null, "", "EMP", null);
	}
}
