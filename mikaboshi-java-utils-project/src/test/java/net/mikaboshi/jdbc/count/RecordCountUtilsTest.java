package net.mikaboshi.jdbc.count;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.sql.SQLException;

import net.mikaboshi.jdbc.HSQLDBTestCase;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.junit.Test;

public class RecordCountUtilsTest extends HSQLDBTestCase {

	@Test
	public void testCreateReport() throws IOException, SQLException {

		String[] inputLine = new String[] {
				"select count(*) from EMP;",
				"",
				"select 100 from sample_tab1;",
				""
		};
		
		String[] expectedOutputLine = new String[] {
				"14",
				"",
				"100",
				""
		};
		
		String inputSql = StringUtils.join(inputLine, IOUtils.LINE_SEPARATOR);
		
		Reader reader = new InputStreamReader(
			new ByteArrayInputStream(inputSql.getBytes()));
		
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		Writer writer = new OutputStreamWriter(baos);
		
		RecordCountUtils.createRecord(reader, writer, getConnection());
		
		writer.flush();
		
		String expected = StringUtils.join(expectedOutputLine, IOUtils.LINE_SEPARATOR);
		
		assertEquals(expected, baos.toString());
		
		IOUtils.closeQuietly(writer);
		IOUtils.closeQuietly(reader);
	}
	
}
