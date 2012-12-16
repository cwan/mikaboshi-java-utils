package net.mikaboshi.csv;

import static junit.framework.Assert.assertEquals;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;

import net.mikaboshi.csv.StandardCSVStrategy;

import org.apache.commons.io.IOUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class StandardCSVStrategyTest {
	
	private ByteArrayOutputStream outputStream;
	private PrintWriter writer;
	
	private StandardCSVStrategy csvStrategy;
	
	@Before
	public void setUp() {
		this.outputStream = new ByteArrayOutputStream();
		this.writer = new PrintWriter(this.outputStream);
		this.csvStrategy = new StandardCSVStrategy();
	}
	
	@After
	public void tearDown() throws IOException {
		this.outputStream.close();
		this.writer.close();
	}
	
	private String getOutput() {
		this.writer.flush();
		return new String(this.outputStream.toByteArray());
	}

	/**
	 * nullを出力変換
	 */
	@Test
	public void testEscapeNull() {
		assertEquals("", this.csvStrategy.escape(null));
	}
	
	/**
	 * 空文字を出力変換
	 */
	@Test
	public void testPEscapeEmpty() {
		assertEquals("", this.csvStrategy.escape(""));
	}
	
	/**
	 * 半角スペースを出力変換
	 */
	@Test
	public void testEscapeSpace() {
		assertEquals("　", this.csvStrategy.escape("　"));
	}
	
	/**
	 * 普通の文字を出力変換
	 */
	@Test
	public void testEscapeNormal() {
		assertEquals("abc123'あいう①②③～", 
				this.csvStrategy.escape("abc123'あいう①②③～"));
	}	
	
	/**
	 * カンマを出力変換
	 */
	@Test
	public void testEscapeComma() {
		assertEquals("\"abc,123,あいう\"", this.csvStrategy.escape("abc,123,あいう"));
	}	
	
	/**
	 * ダブルクォートを出力変換
	 */
	@Test
	public void testEscapeQQ() {
		assertEquals("\"abc\"\"123\"\"\"\"あいう\"", 
				this.csvStrategy.escape("abc\"123\"\"あいう"));
	}
	
	/**
	 * 1項目行出力
	 */
	@Test
	public void testPrintLine1Item() {
		String[] data = new String[] {"abc"};
		this.csvStrategy.printLine(data, this.writer);
		assertEquals("abc" + IOUtils.LINE_SEPARATOR, getOutput());
	}
	
	/**
	 * 3項目行出力
	 */
	@Test
	public void testPrintLine3Items() {
		String[] data = new String[] {"abc", "de,f", "ghi"};
		this.csvStrategy.printLine(data, this.writer);
		
		String expected = "abc,\"de,f\",ghi"
			+ IOUtils.LINE_SEPARATOR;
		assertEquals(expected, getOutput());
	}
	
	/**
	 * null出力（デフォルト）
	 */
	@Test
	public void testPrintNull() {
		String[] data = new String[] {"abc", null, "ghi"};
		this.csvStrategy.printLine(data, this.writer);
		
		String expected = "abc,,ghi"
			+ IOUtils.LINE_SEPARATOR;
		assertEquals(expected, getOutput());
	}
	
	/**
	 * alwaysQuote=true
	 */
	@Test
	public void testAlwaysQuote() {
		String[] data = new String[] {"abc", null, "ghi"};
		this.csvStrategy.setAlwaysQuote(true);
		this.csvStrategy.printLine(data, this.writer);
		
		String expected = "\"abc\",\"\",\"ghi\""
			+ IOUtils.LINE_SEPARATOR;
		assertEquals(expected, getOutput());
	}
	
	/**
	 * null出力（文字列指定）
	 */
	@Test
	public void testPrintCustomNull() {
		String[] data = new String[] {"abc", null, "ghi"};
		
		this.csvStrategy.setNullString("<NULL>");
		
		this.csvStrategy.printLine(data, this.writer);
		
		String expected = "abc,<NULL>,ghi"
			+ IOUtils.LINE_SEPARATOR;
		assertEquals(expected, getOutput());
	}
	
	/**
	 * 区切り文字（複数文字）指定出力
	 */
	@Test
	public void testPrintMultiStringDelimiter() {
		String[] data = new String[] {"abc", "def", "g###hi"};
		this.csvStrategy.setDelimiter("###");
		
		this.csvStrategy.printLine(data, this.writer);
		
		String expected = "abc###def###\"g###hi\""
			+ IOUtils.LINE_SEPARATOR;
		assertEquals(expected, getOutput());
	}
	
	/**
	 * 区切り文字（マルチバイト文字）指定出力
	 */
	@Test
	public void testPrintMultiByteStringDelimiter() {
		String[] data = new String[] {"abc", "def", "g■hi"};
		this.csvStrategy.setDelimiter("■");
		
		this.csvStrategy.printLine(data, this.writer);
		
		String expected = "abc■def■\"g■hi\""
			+ IOUtils.LINE_SEPARATOR;
		assertEquals(expected, getOutput());
	}
	
	/**
	 * 改行文字出力
	 */
	@Test
	public void testPrintLineSeparator() {
		String[] data = new String[] {"a\rb", "c\nd", "e\r\nf"};
		
		this.csvStrategy.printLine(data, this.writer);
		
		String expected = "\"a\rb\",\"c\nd\",\"e\r\nf\""
			+ IOUtils.LINE_SEPARATOR;
		assertEquals(expected, getOutput());
	}
}
