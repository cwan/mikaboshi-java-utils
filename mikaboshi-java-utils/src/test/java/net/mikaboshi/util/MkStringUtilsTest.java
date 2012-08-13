package net.mikaboshi.util;


import static org.junit.Assert.assertEquals;

import org.junit.Test;

@SuppressWarnings("boxing")
public class MkStringUtilsTest {

	@Test(expected=NullPointerException.class)
	public void testSliceStrIsNull() {
		MkStringUtils.slice(null, 1);
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testSliceSizeZero() {
		MkStringUtils.slice("a", 0);
	}
	
	@Test
	public void testSliceBank() {
		String[] result = MkStringUtils.slice("", 1);
		
		assertEquals(1, result.length);
		assertEquals("", result[0]);
	}
	
	@Test
	public void testSlice3by1() {
		String[] result = MkStringUtils.slice("abc", 1);

		assertEquals(3, result.length);
		assertEquals("a", result[0]);
		assertEquals("b", result[1]);
		assertEquals("c", result[2]);
	}

	@Test
	public void testSlice3by2() {
		String[] result = MkStringUtils.slice("abc", 2);

		assertEquals(2, result.length);
		assertEquals("ab", result[0]);
		assertEquals("c", result[1]);
	}

	@Test
	public void testSlice3by3() {
		String[] result = MkStringUtils.slice("abc", 3);

		assertEquals(1, result.length);
		assertEquals("abc", result[0]);
	}

	@Test
	public void testSlice3by4() {
		String[] result = MkStringUtils.slice("abc", 4);

		assertEquals(1, result.length);
		assertEquals("abc", result[0]);
	}
	
	@Test
	public void testSlice10by4jp() {
		String[] result = MkStringUtils.slice("あいうえおかきくけこ", 4);

		assertEquals(3, result.length);
		assertEquals("あいうえ", result[0]);
		assertEquals("おかきく", result[1]);
		assertEquals("けこ", result[2]);
	}
	
	@Test
	public void testSlice10by5jp() {
		String[] result = MkStringUtils.slice("あいうえおかきくけこ", 5);

		assertEquals(2, result.length);
		assertEquals("あいうえお", result[0]);
		assertEquals("かきくけこ", result[1]);
	}
	
	@Test
	public void testTrimAll() {
		String[] array = new String[] {" a b c", "aaa   ", "\nx\r\r\n"};
		
		MkStringUtils.trimAll(array);
		
		assertEquals("a b c", array[0]);
		assertEquals("aaa", array[1]);
		assertEquals("x", array[2]);
	}
	
	@Test
	public void testCutTail0() {
		String result = MkStringUtils.cutTail("abcdefg", 0);
		assertEquals("abcdefg", result);
	}
	
	@Test
	public void testCutTail1() {
		String result = MkStringUtils.cutTail("abcdefg", 1);
		assertEquals("abcdef", result);
	}
	
	@Test
	public void testCutTail7() {
		String result = MkStringUtils.cutTail("abcdefg", 7);
		assertEquals("", result);
	}
	
	@Test
	public void testCutTailOver() {
		String result = MkStringUtils.cutTail("abcdefg", 10);
		assertEquals("", result);
	}
	
	@Test
	public void escapeHtml() {
		
		String string = "<script type=\"text/javascript\">alert('a&b');</script>";
		String expected = "&lt;script type=&quot;text/javascript&quot;&gt;alert(&#39;a&amp;b&#39;);&lt;/script&gt;";
		
		assertEquals(expected, MkStringUtils.escapeHtml(string));
	}
	
}
