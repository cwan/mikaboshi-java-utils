package net.mikaboshi.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Comparator;

import org.apache.commons.lang.StringUtils;
import org.junit.Test;

public class SortedListTest {

	@Test
	public void testCompare_NoComparetor() {
		
		SortedList<Integer> list = SortedList.newInstance(Integer.class);
		
		assertTrue( list.compare(1, 2) < 0 );
		assertTrue( list.compare(2, 1) > 0 );
		assertTrue( list.compare(2, 2) == 0 );
	}
	
	@Test
	public void testCompare_WithComparetor() {
		
		Comparator<Integer> reverseComparator = new Comparator<Integer>() {

			public int compare(Integer o1, Integer o2) {
				return - o1.compareTo(o2);
			}
		};
		
		SortedList<Integer> list = new SortedList<Integer>(reverseComparator);
		
		assertTrue( list.compare(1, 2) > 0 );
		assertTrue( list.compare(2, 1) < 0 );
		assertTrue( list.compare(2, 2) == 0 );
	}
	
	@Test
	public void testBinarySearch_NoDuplication() {
		
		SortedList<Integer> list = SortedList.newInstance(Integer.class);
		
		assertEquals( -1, list.binarySearch(1) );
		
		list.add(1);	// => [ 1 ]
		
		assertEquals( 0, list.binarySearch(1) );
		assertEquals( -1, list.binarySearch(0) );
		assertEquals( -2, list.binarySearch(2) );
		
		list.add(2);	// => [ 1, 2 ]
		
		assertEquals( 0, list.binarySearch(1) );
		assertEquals( -1, list.binarySearch(0) );
		assertEquals( 1, list.binarySearch(2) );
		assertEquals( -3, list.binarySearch(3) );
		
		list.add(0);	// => [ 0, 1, 2 ]
		
		assertEquals( 0, list.binarySearch(0) );
		assertEquals( 1, list.binarySearch(1) );
		assertEquals( 2, list.binarySearch(2) );
		
		list.add(4);	// => [ 0, 1, 2, 4 ]
		
		assertEquals( 0, list.binarySearch(0) );
		assertEquals( 1, list.binarySearch(1) );
		assertEquals( 2, list.binarySearch(2) );
		assertEquals( -4, list.binarySearch(3) );
		assertEquals( 3, list.binarySearch(4) );
		
		list.add(3);	// => [ 0, 1, 2, 3, 4 ]
		
		assertEquals( 0, list.binarySearch(0) );
		assertEquals( 1, list.binarySearch(1) );
		assertEquals( 2, list.binarySearch(2) );
		assertEquals( 3, list.binarySearch(3) );
		assertEquals( 4, list.binarySearch(4) );
	}
	
	@Test
	public void testBinarySearch_WithDuplication() {
		
		SortedList<Integer> list = SortedList.newInstance(Integer.class);
		
		assertEquals( -1, list.binarySearch(1) );
		
		list.add(1);	// => [ 1 ]
		list.add(1);	// => [ 1, 1 ]
		
		assertEquals( -1, list.binarySearch(0) );
		assertTrue( list.binarySearch(1) >= 0 );
		assertEquals( -3, list.binarySearch(2) );
		
		list.add(2);	// => [ 1, 1, 2 ]
		
		assertEquals( -1, list.binarySearch(0) );
		assertTrue( list.binarySearch(1) >= 0 );
		assertTrue( list.binarySearch(2) >= 0 );
		assertEquals( -4, list.binarySearch(3) );
	}
	
	@Test
	public void testIndexOf_NoDuplication() {
		
		SortedList<Integer> list = SortedList.newInstance(Integer.class);
		
		assertEquals( -1, list.indexOf(1) );
		assertEquals( -1, list.lastIndexOf(1) );
		
		list.add(1);	// => [ 1 ]
		
		assertEquals( 0, list.indexOf(1) );
		assertEquals( 0, list.lastIndexOf(1) );
		assertEquals( -1, list.indexOf(2) );
		assertEquals( -1, list.lastIndexOf(2) );
		
		list.add(2);	// => [ 1, 2 ]
		
		assertEquals( 0, list.indexOf(1) );
		assertEquals( 0, list.lastIndexOf(1) );
		assertEquals( 1, list.indexOf(2) );
		assertEquals( 1, list.lastIndexOf(2) );
		assertEquals( -1, list.indexOf(3) );
		assertEquals( -1, list.lastIndexOf(3) );
		
		list.add(0);	// => [ 0, 1, 2 ]
		
		assertEquals( 0, list.indexOf(0) );
		assertEquals( 0, list.lastIndexOf(0) );
		assertEquals( 1, list.indexOf(1) );
		assertEquals( 1, list.lastIndexOf(1) );
		assertEquals( 2, list.indexOf(2) );
		assertEquals( 2, list.lastIndexOf(2) );
		
		list.add(4);	// => [ 0, 1, 2, 4 ]
		
		assertEquals( 0, list.indexOf(0) );
		assertEquals( 0, list.lastIndexOf(0) );
		assertEquals( 1, list.indexOf(1) );
		assertEquals( 1, list.lastIndexOf(1) );
		assertEquals( 2, list.indexOf(2) );
		assertEquals( 2, list.lastIndexOf(2) );
		assertEquals( 3, list.indexOf(4) );
		assertEquals( 3, list.lastIndexOf(4) );
		
		list.add(3);	// => [ 0, 1, 2, 3, 4 ]
		
		assertEquals( 0, list.indexOf(0) );
		assertEquals( 0, list.lastIndexOf(0) );
		assertEquals( 1, list.indexOf(1) );
		assertEquals( 1, list.lastIndexOf(1) );
		assertEquals( 2, list.indexOf(2) );
		assertEquals( 2, list.lastIndexOf(2) );
		assertEquals( 3, list.indexOf(3) );
		assertEquals( 3, list.lastIndexOf(3) );
		assertEquals( 4, list.indexOf(4) );
		assertEquals( 4, list.lastIndexOf(4) );
	}
	
	@Test
	public void testIndexOf_WithDuplication() {
		
		SortedList<Integer> list = SortedList.newInstance(Integer.class);
		
		assertEquals( -1, list.indexOf(1) );
		assertEquals( -1, list.lastIndexOf(1) );
		
		list.add(1);	// => [ 1 ]
		list.add(1);	// => [ 1, 1 ]
		
		assertEquals( -1, list.indexOf(0) );
		assertEquals( -1, list.lastIndexOf(0) );
		assertEquals( 0, list.indexOf(1) );
		assertEquals( 1, list.lastIndexOf(1) );
		assertEquals( -1, list.indexOf(2) );
		assertEquals( -1, list.lastIndexOf(2) );
		
		list.add(2);	// => [ 1, 1, 2 ]
		
		assertEquals( -1, list.indexOf(0) );
		assertEquals( -1, list.lastIndexOf(0) );
		assertEquals( 0, list.indexOf(1) );
		assertEquals( 1, list.lastIndexOf(1) );
		assertEquals( 2, list.indexOf(2) );
		assertEquals( 2, list.lastIndexOf(2) );
		assertEquals( -1, list.indexOf(3) );
		assertEquals( -1, list.lastIndexOf(3) );
	}
	
	@Test(expected = NullPointerException.class)
	public void testAdd_Null() {
		
		SortedList<String> list = SortedList.newInstance(String.class);
		
		list.add("a");
		list.add(null);
	}
	
	@Test
	public void testAdd_NullComparator() {
		
		Comparator<String> nullComparator = new Comparator<String>() {

			public int compare(String o1, String o2) {
				
				if (o1 == null && o2 == null) {
					return 0;
				}
				
				if (o1 != null && o2 == null) {
					return -1;
				}
				
				if (o1 == null && o2 != null) {
					return 1;
				}
				
				return o1.compareTo(o2);
			}
		};
		
		SortedList<String> list = new SortedList<String>(nullComparator);
		
		list.add("h");
		list.add(null);
		list.add("c");
		list.add(null);
		list.add("w");
		list.add(null);
		list.add("a");
		list.add(null);
		list.add("c");
		
		String result = StringUtils.join(list, ",");
		
		assertEquals( "a,c,c,h,w,,,,", result);
	}
	
}
