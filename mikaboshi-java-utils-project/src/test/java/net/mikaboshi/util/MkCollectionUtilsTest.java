package net.mikaboshi.util;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.junit.Test;

public class MkCollectionUtilsTest {

	@Test
	public void testToArray_クラス指定_要素数1() {
		
		List<String> list = new ArrayList<String>();
		list.add("aaa");
		
		String[] array = MkCollectionUtils.toArray(list, String.class);
		
		assertEquals(1, array.length);
		assertEquals("aaa", array[0]);
	}
	
	@Test
	public void testToArray_クラス指定_要素数0() {
		
		List<String> list = new ArrayList<String>();
		
		String[] array = MkCollectionUtils.toArray(list, String.class);
		
		assertEquals(0, array.length);
	}
	
	@Test
	public void testToArray_クラス無指定_要素数1() {
		
		List<String> list = new ArrayList<String>();
		list.add("aaa");
		
		String[] array = MkCollectionUtils.toArray(list);
		
		assertEquals(1, array.length);
		assertEquals("aaa", array[0]);
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testToArray_クラス無指定_要素数0() {
		
		List<String> list = new ArrayList<String>();
		
		MkCollectionUtils.toArray(list);
	}
	
	@Test
	public void testToArray_クラス指定_要素数3_不均一() {
		
		List<Object> list = new ArrayList<Object>();
		list.add("aaa");
		list.add(new Date());
		list.add(3);
		
		Object[] array = MkCollectionUtils.toArray(list, Object.class);
		
		assertEquals(3, array.length);
	}
	
	@Test(expected = ArrayStoreException.class)
	public void testToArray_クラス無指定_要素数3_不均一() {
		
		List<Object> list = new ArrayList<Object>();
		list.add("aaa");
		list.add(new Date());
		list.add(3);
		
		MkCollectionUtils.toArray(list);
	}
	
}
