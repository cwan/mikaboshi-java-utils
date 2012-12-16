package net.mikaboshi.log;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class SimpleFileLoggerTest {
	
	private static final String LOG_PATH = "SimpleFileLoggerTest.log";
	
	@Before
	public void setUp() {
		deleteLogFile();
	}
	
	@After
	public void tearDown() {
		deleteLogFile();
	}
	
	private void deleteLogFile() {
		File logFile = new File(LOG_PATH);
		
		if (logFile.exists()) {
			logFile.delete();
		}
	}

	@Test
	public void testPut() throws IOException {
		SimpleFileLogger logger = 
			new SimpleFileLogger(LOG_PATH, true, true);
		
		logger.put("aaa");
		logger.put("bbb");
		logger.close();
		
		String content =
			FileUtils.readFileToString(new File(LOG_PATH));
		
		String expected =
			"aaa" + IOUtils.LINE_SEPARATOR + "bbb" + IOUtils.LINE_SEPARATOR;
		
		assertEquals(expected, content);
	}
	
	@Test
	public void testPutf() throws IOException {
		SimpleFileLogger logger = 
			new SimpleFileLogger(LOG_PATH, true, true);
		
		logger.putf("aaa %d bbb", 1);
		logger.putf("ccc %s eee", "fff");
		logger.close();
		
		String content =
			FileUtils.readFileToString(new File(LOG_PATH));
		
		String expected =
			"aaa 1 bbb" + IOUtils.LINE_SEPARATOR + 
			"ccc fff eee" + IOUtils.LINE_SEPARATOR;
		
		assertEquals(expected, content);
	}

	@Test
	public void testRotate() throws IOException {
		SimpleFileLogger logger = new SimpleFileLogger(
				"SimpleFileLoggerRotateTest.log", true, true);
		
		logger.clean();
		
		logger.setRotetaSize(1000);
		
		for (int i = 0; i < 1000; i++) {
			logger.put("" + i);
		}
	}
}
