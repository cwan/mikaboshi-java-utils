package net.mikaboshi.util;

import static org.junit.Assert.*;

import org.junit.Test;

public class JsonFormatterTest {

	@Test
	public void test_1token() {
		
		String str = "abc";
		
		String expected = "abc";
		
		String actual = new JsonFormatter("  ", "\n").format(str);
		
		assertEquals(expected, actual);
	}
	
	@Test
	public void test_1property() {
		
		String str = "{ abc : 'ABC' }";
		
		String expected = "{\n  abc : 'ABC' \n}";
		
		String actual = new JsonFormatter("  ", "\n").format(str);
		
		assertEquals(expected, actual);
	}
	
	@Test
	public void test_2properties() {
		
		String str = "{ abc : 'ABC', \"def\": \"D,E F\" }";
		
		String expected = "{\n  abc : 'ABC',\n  \"def\" : \"D,E F\" \n}";
		
		String actual = new JsonFormatter("  ", "\n").format(str);
		
		assertEquals(expected, actual);
	}
	
	@Test
	public void test_array() {
		
		String str = "{ arr : [1, 2, 3] }";
		
		String expected = "{\n  arr : [\n    1,\n    2,\n    3\n  ] \n}";
		
		String actual = new JsonFormatter("  ", "\n").format(str);
		
		assertEquals(expected, actual);
	}
	
	@Test
	public void test_blankArray() {
		
		String str = "{ arr : [] }";
		
		String expected = "{\n  arr : [] \n}";
		
		String actual = new JsonFormatter("  ", "\n").format(str);
		
		assertEquals(expected, actual);
	}
	
}
