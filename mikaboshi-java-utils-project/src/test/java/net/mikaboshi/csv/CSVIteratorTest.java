package net.mikaboshi.csv;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.CharArrayReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;
import java.util.Iterator;
import java.util.NoSuchElementException;

import org.apache.commons.io.IOUtils;
import org.junit.Test;

public class CSVIteratorTest {

	@Test
	public void testNoNext() throws InterruptedException, IOException {
		final String testFile = "src/test/resources/net/mikaboshi/csv/CSVIteratorTest.csv";
		
		Reader reader = null;
		final PrintWriter writer = new PrintWriter(new FileWriter(testFile));
		
		CSVStrategy csvStrategy = new StandardCSVStrategy();
		
		try {
			new Thread() {
				@Override
				public void run() {
					for (int i = 0; i < 50; i++) {
						try {
							writer.println("xxx,yyy,zzz");
							writer.flush();
							Thread.sleep(10);
						} catch (Exception e) {
							e.printStackTrace();
							return;
						}
					}
				}
			}.start();
			
			Thread.sleep(100);
			
			reader = new FileReader(testFile);
			
			Iterator<String[]> iter =
				csvStrategy.csvLines(reader).iterator();
			
			int lineCount = 0;
			for (int i = 0; i < 100; i++) {
				if (!iter.hasNext()) {
					Thread.sleep(10);
					continue;
				}
				
//				String[] items = 
					iter.next();
				lineCount++;
//				System.out.println(StringUtils.join(items, ","));
			}
			
			assertEquals(50, lineCount);

		} finally {
			IOUtils.closeQuietly(reader);
			IOUtils.closeQuietly(writer);
		}
	}
	
	/**
	 * 空文字
	 * @throws IOException 
	 */
	@Test
	public void testBlank() throws IOException {
		Reader reader = new CharArrayReader("".toCharArray());
		
		StandardCSVStrategy csvStrategy = new StandardCSVStrategy();
		
		StandardCSVStrategy.CSVIterator iter = 
				(StandardCSVStrategy.CSVIterator) csvStrategy.csvLines(reader).iterator();
		
		iter.mark(8192);
		
		assertFalse(iter.hasNext());
		
		iter.reset();
		
		assertFalse(iter.hasNext());
	}
	
	/**
	 * 1項目×1行
	 * @throws IOException 
	 */
	@Test
	public void test1Item1Line() throws IOException {
		Reader reader = new CharArrayReader("abc".toCharArray());
		
		StandardCSVStrategy csvStrategy = new StandardCSVStrategy();
		
		StandardCSVStrategy.CSVIterator iter = 
			(StandardCSVStrategy.CSVIterator) csvStrategy.csvLines(reader).iterator();
		
		iter.mark(8192);
		
		assertTrue(iter.hasNext());
		
		iter.reset();
		
		assertTrue(iter.hasNext());
		
		String[] data = iter.next();
		assertEquals(1, data.length);
		assertEquals("abc", data[0]);
		
		reader.reset();
		
		assertTrue(iter.hasNext());
		
		data = iter.next();
		assertEquals(1, data.length);
		assertEquals("abc", data[0]);

		assertFalse(iter.hasNext());
	}
	
	/**
	 * 次が無い状態でnext()呼び出し
	 * @throws IOException 
	 */
	@Test(expected = NoSuchElementException.class)
	public void testNoNextReset() throws IOException {
		Reader reader = new CharArrayReader("abc".toCharArray());
		
		StandardCSVStrategy csvStrategy = new StandardCSVStrategy();
		
		StandardCSVStrategy.CSVIterator iter = 
			(StandardCSVStrategy.CSVIterator) csvStrategy.csvLines(reader).iterator();
		
		iter.mark(8192);
		
		try {
			assertTrue(iter.hasNext());
			
			String[] data = iter.next();
			assertEquals(1, data.length);
			assertEquals("abc", data[0]);
			
			assertFalse(iter.hasNext());
			
			iter.reset();
			
			assertTrue(iter.hasNext());
			
			data = iter.next();
			assertEquals(1, data.length);
			assertEquals("abc", data[0]);
			
		} catch (Exception e) {
			fail("ここまでは例外はでないはず");
		}
		
		// ここで例外発生
		iter.next();
	}
	
	/**
	 * 2項目×1行
	 * @throws IOException 
	 */
	@Test
	public void test2Items1Line() throws IOException {
		Reader reader = new CharArrayReader("abc,def".toCharArray());
		
		StandardCSVStrategy csvStrategy = new StandardCSVStrategy();
		
		StandardCSVStrategy.CSVIterator iter = 
			(StandardCSVStrategy.CSVIterator) csvStrategy.csvLines(reader).iterator();
		
		iter.mark(8192);
		
		assertTrue(iter.hasNext());
		
		String[] data = iter.next();
		assertEquals(2, data.length);
		assertEquals("abc", data[0]);
		assertEquals("def", data[1]);
		
		assertFalse(iter.hasNext());
		
		iter.reset();
		
		assertTrue(iter.hasNext());
		
		data = iter.next();
		assertEquals(2, data.length);
		assertEquals("abc", data[0]);
		assertEquals("def", data[1]);
		
		assertFalse(iter.hasNext());
	}
	
	/**
	 * 末尾がカンマ
	 * @throws IOException 
	 */
	@Test
	public void testEndWithCommna() throws IOException {
		Reader reader = new CharArrayReader("abc,def,".toCharArray());
		
		StandardCSVStrategy csvStrategy = new StandardCSVStrategy();
		
		StandardCSVStrategy.CSVIterator iter = 
			(StandardCSVStrategy.CSVIterator) csvStrategy.csvLines(reader).iterator();
		
		iter.mark(8192);
		
		assertTrue(iter.hasNext());
		
		String[] data = iter.next();
		assertEquals(3, data.length);
		assertEquals("abc", data[0]);
		assertEquals("def", data[1]);
		assertEquals("", data[2]);
		
		assertFalse(iter.hasNext());
		
		iter.reset();
		
		assertTrue(iter.hasNext());
		
		data = iter.next();
		assertEquals(3, data.length);
		assertEquals("abc", data[0]);
		assertEquals("def", data[1]);
		assertEquals("", data[2]);
	}
	
	/**
	 * 末尾がカンマ（複数行）
	 * @throws IOException 
	 */
	@Test
	public void testEndWithCommnaMultiLines() throws IOException {
		Reader reader = new CharArrayReader("abc,def,\r\nghi,".toCharArray());
		
		StandardCSVStrategy csvStrategy = new StandardCSVStrategy();
		
		StandardCSVStrategy.CSVIterator iter = 
			(StandardCSVStrategy.CSVIterator) csvStrategy.csvLines(reader).iterator();
		
		assertTrue(iter.hasNext());
		
		String[] data1 = iter.next();
		assertEquals(3, data1.length);
		assertEquals("abc", data1[0]);
		assertEquals("def", data1[1]);
		assertEquals("", data1[2]);
		
		iter.mark(8192);
		
		String[] data2 = iter.next();
		assertEquals(2, data2.length);
		assertEquals("ghi", data2[0]);
		assertEquals("", data2[1]);
		
		assertFalse(iter.hasNext());
		
		iter.reset();
		
		assertTrue(iter.hasNext());
		
		String[] data3 = iter.next();
		assertEquals(2, data3.length);
		assertEquals("ghi", data3[0]);
		assertEquals("", data3[1]);
		
		assertFalse(iter.hasNext());
	}
	
	/**
	 * 3項目×1行
	 * 引用符あり
	 * @throws IOException 
	 */
	@Test
	public void test3Items1LineQuoted() throws IOException {
		Reader reader = new CharArrayReader("\"abc\",\"de,f\",\"gh\"\"ijk\"".toCharArray());
		
		StandardCSVStrategy csvStrategy = new StandardCSVStrategy();
		
		StandardCSVStrategy.CSVIterator iter = 
			(StandardCSVStrategy.CSVIterator) csvStrategy.csvLines(reader).iterator();
		
		assertTrue(iter.hasNext());
		
		iter.mark(8192);
		
		String[] data = iter.next();
		assertEquals(3, data.length);
		assertEquals("abc", data[0]);
		assertEquals("de,f", data[1]);
		assertEquals("gh\"ijk", data[2]);
		
		assertFalse(iter.hasNext());
		
		iter.reset();
		
		data = iter.next();
		assertEquals(3, data.length);
		assertEquals("abc", data[0]);
		assertEquals("de,f", data[1]);
		assertEquals("gh\"ijk", data[2]);
		
		assertFalse(iter.hasNext());
	}
	
	/**
	 * 2項目×2行（CRLF）
	 * @throws IOException 
	 */
	@Test
	public void test2Items2Lines() throws IOException {
		Reader reader = new CharArrayReader("abc,def\r\nghi,jkl".toCharArray());
		
		StandardCSVStrategy csvStrategy = new StandardCSVStrategy();
		
		StandardCSVStrategy.CSVIterator iter = 
			(StandardCSVStrategy.CSVIterator) csvStrategy.csvLines(reader).iterator();
		
		assertTrue(iter.hasNext());
		
		iter.mark(8192);
		
		String[] data1 = iter.next();
		assertEquals(2, data1.length);
		assertEquals("abc", data1[0]);
		assertEquals("def", data1[1]);
		
		assertTrue(iter.hasNext());
		
		iter.reset();
		
		data1 = iter.next();
		assertEquals(2, data1.length);
		assertEquals("abc", data1[0]);
		assertEquals("def", data1[1]);
		
		iter.mark(8192);
		
		String[] data2 = iter.next();
		assertEquals(2, data2.length);
		assertEquals("ghi", data2[0]);
		assertEquals("jkl", data2[1]);
		
		assertFalse(iter.hasNext());
		
		iter.reset();
		
		data2 = iter.next();
		assertEquals(2, data2.length);
		assertEquals("ghi", data2[0]);
		assertEquals("jkl", data2[1]);
		
		assertFalse(iter.hasNext());
	}
	
	/**
	 * 1項目×3行（LF）
	 * @throws IOException 
	 */
	@Test
	public void test1Item3Lines() throws IOException {
		Reader reader = new CharArrayReader("abc\ndef\nghi".toCharArray());
		
		StandardCSVStrategy csvStrategy = new StandardCSVStrategy();
		
		StandardCSVStrategy.CSVIterator iter = 
			(StandardCSVStrategy.CSVIterator) csvStrategy.csvLines(reader).iterator();
		
		assertTrue(iter.hasNext());
		
		String[] data1 = iter.next();
		assertEquals(1, data1.length);
		assertEquals("abc", data1[0]);
		
		assertTrue(iter.hasNext());
		
		iter.mark(8192);
		
		String[] data2 = iter.next();
		assertEquals(1, data2.length);
		assertEquals("def", data2[0]);
		
		assertTrue(iter.hasNext());
		
		String[] data3 = iter.next();
		assertEquals(1, data3.length);
		assertEquals("ghi", data3[0]);
		
		assertFalse(iter.hasNext());
		
		iter.reset();
		
		data2 = iter.next();
		assertEquals(1, data2.length);
		assertEquals("def", data2[0]);
		
		assertTrue(iter.hasNext());
		
		data3 = iter.next();
		assertEquals(1, data3.length);
		assertEquals("ghi", data3[0]);
		
		assertFalse(iter.hasNext());
	}
	
	/**
	 * 2項目×2行（複数文字の区切り文字）
	 * @throws IOException 
	 */
	@Test
	public void test2Items2LinesMultiDelimiter() throws IOException {
		Reader reader = new CharArrayReader("abc##def\r\nghi##\"jk##l\"".toCharArray());
		
		StandardCSVStrategy csvStrategy = new StandardCSVStrategy();
		csvStrategy.setDelimiter("##");
		
		StandardCSVStrategy.CSVIterator iter = 
			(StandardCSVStrategy.CSVIterator) csvStrategy.csvLines(reader).iterator();
		
		assertTrue(iter.hasNext());
		
		iter.mark(8129);
		
		String[] data1 = iter.next();
		assertEquals(2, data1.length);
		assertEquals("abc", data1[0]);
		assertEquals("def", data1[1]);
		
		assertTrue(iter.hasNext());
		
		iter.reset();
		
		data1 = iter.next();
		assertEquals(2, data1.length);
		assertEquals("abc", data1[0]);
		assertEquals("def", data1[1]);
		
		assertTrue(iter.hasNext());
		
		iter.mark(8192);
		
		String[] data2 = iter.next();
		assertEquals(2, data2.length);
		assertEquals("ghi", data2[0]);
		assertEquals("jk##l", data2[1]);
		
		assertFalse(iter.hasNext());
		
		iter.reset();
		
		assertTrue(iter.hasNext());
		
		data2 = iter.next();
		assertEquals(2, data2.length);
		assertEquals("ghi", data2[0]);
		assertEquals("jk##l", data2[1]);
		
		assertFalse(iter.hasNext());
	}
	
}
