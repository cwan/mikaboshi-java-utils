package net.mikaboshi.csv;

import java.io.BufferedReader;
import java.io.CharArrayReader;
import java.io.Reader;
import java.util.Iterator;
import java.util.NoSuchElementException;

import net.mikaboshi.csv.CSVStrategy;
import net.mikaboshi.csv.StandardCSVStrategy;

import org.junit.Test;

import static junit.framework.Assert.*;

/**
 * StandardCSVStrategyのiterator（CSVデータ読み込み）のテストケース。
 *
 */
public class StandardCSVStrategyIteratorTest {

	/**
	 * 空文字
	 */
	@Test
	public void testBlank() {
		Reader reader = new CharArrayReader("".toCharArray());
		
		CSVStrategy csvStrategy = new StandardCSVStrategy();
		
		Iterator<String[]> iter = csvStrategy.csvLines(reader).iterator();
		
		assertFalse(iter.hasNext());
	}

	/**
	 * 1項目×1行
	 */
	@Test
	public void test1Item1Line() {
		Reader reader = new CharArrayReader("abc".toCharArray());
		
		CSVStrategy csvStrategy = new StandardCSVStrategy();
		
		Iterator<String[]> iter = csvStrategy.csvLines(reader).iterator();
		
		assertTrue(iter.hasNext());
		assertTrue(iter.hasNext());
		
		String[] data = iter.next();
		assertEquals(1, data.length);
		assertEquals("abc", data[0]);
		
		assertFalse(iter.hasNext());
		assertFalse(iter.hasNext());
	}
	
	/**
	 * BufferedReader使用
	 */
	@Test
	public void test1BufferedReader() {
		Reader reader = new CharArrayReader("abc".toCharArray());
		reader = new BufferedReader(reader);
		
		CSVStrategy csvStrategy = new StandardCSVStrategy();
		
		Iterator<String[]> iter = csvStrategy.csvLines(reader).iterator();
		
		assertTrue(iter.hasNext());
		
		String[] data = iter.next();
		assertEquals(1, data.length);
		assertEquals("abc", data[0]);
		
		assertFalse(iter.hasNext());
	}
	
	/**
	 * 次が無い状態でnext()呼び出し
	 */
	@Test(expected = NoSuchElementException.class)
	public void testNoNext() {
		Reader reader = new CharArrayReader("abc".toCharArray());
		
		CSVStrategy csvStrategy = new StandardCSVStrategy();
		
		Iterator<String[]> iter = csvStrategy.csvLines(reader).iterator();
		
		try {
			assertTrue(iter.hasNext());
			
			String[] data = iter.next();
			assertEquals(1, data.length);
			assertEquals("abc", data[0]);
			
			assertFalse(iter.hasNext());
		} catch (Exception e) {
			fail("ここまでは例外はでないはず");
		}
		
		// ここで例外発生
		iter.next();
	}
	
	/**
	 * 2項目×1行
	 */
	@Test
	public void test2Items1Line() {
		Reader reader = new CharArrayReader("abc,def".toCharArray());
		
		CSVStrategy csvStrategy = new StandardCSVStrategy();
		
		Iterator<String[]> iter = csvStrategy.csvLines(reader).iterator();
		
		assertTrue(iter.hasNext());
		assertTrue(iter.hasNext());
		
		String[] data = iter.next();
		assertEquals(2, data.length);
		assertEquals("abc", data[0]);
		assertEquals("def", data[1]);
		
		assertFalse(iter.hasNext());
		assertFalse(iter.hasNext());
	}
	
	/**
	 * 末尾がカンマ
	 */
	@Test
	public void testEndWithCommna() {
		Reader reader = new CharArrayReader("abc,def,".toCharArray());
		
		CSVStrategy csvStrategy = new StandardCSVStrategy();
		
		Iterator<String[]> iter = csvStrategy.csvLines(reader).iterator();
		
		assertTrue(iter.hasNext());
		assertTrue(iter.hasNext());
		
		String[] data = iter.next();
		assertEquals(3, data.length);
		assertEquals("abc", data[0]);
		assertEquals("def", data[1]);
		assertEquals("", data[2]);
		
		assertFalse(iter.hasNext());
	}
	
	/**
	 * 末尾がカンマ（複数行）
	 */
	@Test
	public void testEndWithCommnaMultiLines() {
		Reader reader = new CharArrayReader("abc,def,\r\nghi,".toCharArray());
		
		CSVStrategy csvStrategy = new StandardCSVStrategy();
		
		Iterator<String[]> iter = csvStrategy.csvLines(reader).iterator();
		
		assertTrue(iter.hasNext());
		assertTrue(iter.hasNext());
		
		String[] data1 = iter.next();
		assertEquals(3, data1.length);
		assertEquals("abc", data1[0]);
		assertEquals("def", data1[1]);
		assertEquals("", data1[2]);
		
		String[] data2 = iter.next();
		assertEquals(2, data2.length);
		assertEquals("ghi", data2[0]);
		assertEquals("", data2[1]);
		
		assertFalse(iter.hasNext());
	}
	
	/**
	 * 3項目×1行
	 * 引用符あり
	 */
	@Test
	public void test3Items1LineQuoted() {
		Reader reader = new CharArrayReader("\"abc\",\"de,f\",\"gh\"\"ijk\"".toCharArray());
		
		CSVStrategy csvStrategy = new StandardCSVStrategy();
		
		Iterator<String[]> iter = csvStrategy.csvLines(reader).iterator();
		
		assertTrue(iter.hasNext());
		
		String[] data = iter.next();
		assertEquals(3, data.length);
		assertEquals("abc", data[0]);
		assertEquals("de,f", data[1]);
		assertEquals("gh\"ijk", data[2]);
		
		assertFalse(iter.hasNext());
	}
	
	/**
	 * 2項目×2行（CRLF）
	 */
	@Test
	public void test2Items2Lines() {
		Reader reader = new CharArrayReader("abc,def\r\nghi,jkl".toCharArray());
		
		CSVStrategy csvStrategy = new StandardCSVStrategy();
		
		Iterator<String[]> iter = csvStrategy.csvLines(reader).iterator();
		
		assertTrue(iter.hasNext());
		
		String[] data1 = iter.next();
		assertEquals(2, data1.length);
		assertEquals("abc", data1[0]);
		assertEquals("def", data1[1]);
		
		assertTrue(iter.hasNext());
		
		String[] data2 = iter.next();
		assertEquals(2, data2.length);
		assertEquals("ghi", data2[0]);
		assertEquals("jkl", data2[1]);
		
		assertFalse(iter.hasNext());
	}
	
	/**
	 * 1項目×3行（LF）
	 */
	@Test
	public void test1Item3Lines() {
		Reader reader = new CharArrayReader("abc\ndef\nghi".toCharArray());
		
		CSVStrategy csvStrategy = new StandardCSVStrategy();
		
		Iterator<String[]> iter = csvStrategy.csvLines(reader).iterator();
		
		assertTrue(iter.hasNext());
		
		String[] data1 = iter.next();
		assertEquals(1, data1.length);
		assertEquals("abc", data1[0]);
		
		assertTrue(iter.hasNext());
		
		String[] data2 = iter.next();
		assertEquals(1, data2.length);
		assertEquals("def", data2[0]);
		
		assertTrue(iter.hasNext());
		
		String[] data3 = iter.next();
		assertEquals(1, data3.length);
		assertEquals("ghi", data3[0]);
		
		assertFalse(iter.hasNext());
	}

	/**
	 * hasNext()を呼び出さないでnext()を呼び出す
	 */
	@Test
	public void test2Items2LinesNoCheck() {
		Reader reader = new CharArrayReader("abc,def\r\nghi,jkl".toCharArray());
		
		CSVStrategy csvStrategy = new StandardCSVStrategy();
		
		Iterator<String[]> iter = csvStrategy.csvLines(reader).iterator();
		
		String[] data1 = iter.next();
		assertEquals(2, data1.length);
		assertEquals("abc", data1[0]);
		assertEquals("def", data1[1]);
		
		String[] data2 = iter.next();
		assertEquals(2, data2.length);
		assertEquals("ghi", data2[0]);
		assertEquals("jkl", data2[1]);
		
		assertFalse(iter.hasNext());
	}
	
	/**
	 * 引用中の改行
	 */
	@Test
	public void testNewLineCodeInQuote() {
		Reader reader = new CharArrayReader(
				"\"abc\r\ndef\",ghi\r\n123\r\nあいう,\"えお\rかき\nく\",\"けこ\"".toCharArray());
		
		CSVStrategy csvStrategy = new StandardCSVStrategy();
		
		Iterator<String[]> iter = csvStrategy.csvLines(reader).iterator();
		
		String[] data1 = iter.next();
		assertEquals(2, data1.length);
		assertEquals("abc\r\ndef", data1[0]);
		assertEquals("ghi", data1[1]);
		
		String[] data2 = iter.next();
		assertEquals(1, data2.length);
		assertEquals("123", data2[0]);
		
		String[] data3 = iter.next();
		assertEquals(3, data3.length);
		assertEquals("あいう", data3[0]);
		assertEquals("えお\rかき\nく", data3[1]);
		assertEquals("けこ", data3[2]);
		
		assertFalse(iter.hasNext());
	}
	
	/**
	 * 引用中の引用符（不正なフォーマット）
	 */
	@Test
	public void testQuoteInQuote() {
		Reader reader = new CharArrayReader(
				"abc,\"def\"ghi\r\njkl,mn".toCharArray());
		
		CSVStrategy csvStrategy = new StandardCSVStrategy();
		
		Iterator<String[]> iter = csvStrategy.csvLines(reader).iterator();
		
		String[] data1 = iter.next();
		assertEquals(2, data1.length);
		assertEquals("abc", data1[0]);
		assertEquals("\"def\"ghi", data1[1]);
		
		String[] data2 = iter.next();
		assertEquals(2, data2.length);
		assertEquals("jkl", data2[0]);
		assertEquals("mn", data2[1]);
		assertFalse(iter.hasNext());
	}
	
	/**
	 * Iterator#remove()のテスト
	 */
	@Test(expected = UnsupportedOperationException.class)
	public void testRemove() {
		Reader reader = new CharArrayReader("abc,def\r\nghi,jkl".toCharArray());
		
		CSVStrategy csvStrategy = new StandardCSVStrategy();
		
		Iterator<String[]> iter = csvStrategy.csvLines(reader).iterator();
		
		String[] data1 = iter.next();
		assertEquals(2, data1.length);
		assertEquals("abc", data1[0]);
		assertEquals("def", data1[1]);
		
		iter.remove();
	}
	
	/**
	 * 空文字項目が途中にあるケース
	 */
	@Test
	public void testBlankItem() {
		Reader reader = new CharArrayReader("abc,,def,\"\",ghi".toCharArray());
		
		CSVStrategy csvStrategy = new StandardCSVStrategy();
		
		Iterator<String[]> iter = csvStrategy.csvLines(reader).iterator();
		
		String[] data = iter.next();
		assertEquals(5, data.length);
		assertEquals("abc", data[0]);
		assertEquals("", data[1]);
		assertEquals("def", data[2]);
		assertEquals("", data[3]);
		assertEquals("ghi", data[4]);
		
		assertFalse(iter.hasNext());
	}

	/**
	 * 2項目×2行（複数文字の区切り文字）
	 */
	@Test
	public void test2Items2LinesMultiDelimiter() {
		Reader reader = new CharArrayReader("abc##def\r\nghi##\"jk##l\"".toCharArray());
		
		CSVStrategy csvStrategy = new StandardCSVStrategy();
		csvStrategy.setDelimiter("##");
		
		Iterator<String[]> iter = csvStrategy.csvLines(reader).iterator();
		
		assertTrue(iter.hasNext());
		
		String[] data1 = iter.next();
		assertEquals(2, data1.length);
		assertEquals("abc", data1[0]);
		assertEquals("def", data1[1]);
		
		assertTrue(iter.hasNext());
		
		String[] data2 = iter.next();
		assertEquals(2, data2.length);
		assertEquals("ghi", data2[0]);
		assertEquals("jk##l", data2[1]);
		
		assertFalse(iter.hasNext());
	}
	
}
