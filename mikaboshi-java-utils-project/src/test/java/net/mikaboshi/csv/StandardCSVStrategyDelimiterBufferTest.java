package net.mikaboshi.csv;

import static org.junit.Assert.*;

import net.mikaboshi.csv.StandardCSVStrategy;

import org.junit.Test;


public class StandardCSVStrategyDelimiterBufferTest {

	/**
	 * 空文字
	 */
	@Test(expected = IllegalArgumentException.class)
	public void testBlank() {
		new StandardCSVStrategy.DelimiterBuffer("");
	}
	
	/**
	 * 半角1文字
	 */
	@Test
	public void testAscii1() {
		StandardCSVStrategy.DelimiterBuffer buf 
			= new StandardCSVStrategy.DelimiterBuffer("#");
		
		buf.nextChar('a');
		assertFalse(buf.isDelimiter());
		
		buf.nextChar('#');
		assertTrue(buf.isDelimiter());
		
		buf.clear();
		
		buf.nextChar('a');
		assertFalse(buf.isDelimiter());
		
		buf.nextChar('#');
		assertTrue(buf.isDelimiter());
	}
	
	/**
	 * 半角2文字
	 */
	@Test
	public void testAscii2() {
		StandardCSVStrategy.DelimiterBuffer buf 
			= new StandardCSVStrategy.DelimiterBuffer("##");
		
		buf.nextChar('a');
		assertFalse(buf.isDelimiter());
		
		buf.nextChar('#');
		assertFalse(buf.isDelimiter());
		
		buf.clear();
		
		buf.nextChar('#');
		assertFalse(buf.isDelimiter());
		
		buf.nextChar('#');
		assertTrue(buf.isDelimiter());
		
		buf.clear();
		
		buf.nextChar('#');
		assertFalse(buf.isDelimiter());
	}
	
	/**
	 * 全角２文字
	 */
	@Test
	public void testMulti2() {
		StandardCSVStrategy.DelimiterBuffer buf 
			= new StandardCSVStrategy.DelimiterBuffer("△■");
		
		buf.nextChar('あ');
		assertFalse(buf.isDelimiter());
		
		buf.nextChar('△');
		assertFalse(buf.isDelimiter());
		
		buf.clear();
		
		buf.nextChar('△');
		assertFalse(buf.isDelimiter());
		
		buf.nextChar('■');
		assertTrue(buf.isDelimiter());
		
		buf.clear();
		
		buf.nextChar('#');
		assertFalse(buf.isDelimiter());
	}
}
