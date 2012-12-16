package net.mikaboshi.util;

import static org.junit.Assert.*;

import org.junit.Test;

public class MkArrayUtilsTest {

	@Test
	public void testMergeNotNull() {
		String[] args = new String[] {"A", "B"};
		String arg = "C";
		
		String[] result = MkArrayUtils.merge(args, arg);
		
		assertEquals(3, result.length);
		assertEquals("A", result[0]);
		assertEquals("B", result[1]);
		assertEquals("C", result[2]);
	}
	
	@Test
	public void testMergeArgNull() {
		String[] args = new String[] {"A", "B"};
		String arg = null;
		
		String[] result = MkArrayUtils.merge(args, arg);
		
		assertEquals(2, result.length);
		assertEquals("A", result[0]);
		assertEquals("B", result[1]);
	}
	
	@Test
	public void testMergeArgsNull() {
		String[] args = null;
		String arg = "C";
		
		String[] result = MkArrayUtils.merge(args, arg);
		
		assertEquals(1, result.length);
		assertEquals("C", result[0]);
	}
	
	@Test
	public void testMergeArgsNullArgNull() {
		String[] args = null;
		String arg = null;
		
		String[] result = MkArrayUtils.merge(args, arg);
		
		assertNull(result);
	}
}
