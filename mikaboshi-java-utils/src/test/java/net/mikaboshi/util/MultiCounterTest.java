package net.mikaboshi.util;

import net.mikaboshi.util.MultiCounter;

import org.junit.Test;
import static org.junit.Assert.assertEquals;

public class MultiCounterTest {

	@Test
	public void testIncrement() {
		MultiCounter counter = new MultiCounter();

		assertEquals(0, counter.getCount("A"));
		counter.increment("A");
		assertEquals(1, counter.getCount("A"));
		counter.increment("A");
		assertEquals(2, counter.getCount("A"));
	}
	
	@Test
	public void testIncrementBy2() {
		MultiCounter counter = new MultiCounter();

		assertEquals(0, counter.getCount("A"));
		counter.increment("A", 2);
		assertEquals(2, counter.getCount("A"));
		counter.increment("A", 2);
		assertEquals(4, counter.getCount("A"));
	}
	
	@Test
	public void testDecrement() {
		MultiCounter counter = new MultiCounter();

		assertEquals(0, counter.getCount("A"));
		counter.increment("A", -1);
		assertEquals(-1, counter.getCount("A"));
		counter.increment("A", -1);
		assertEquals(-2, counter.getCount("A"));
	}
	
	@Test
	public void testMulti() {
		MultiCounter counter = new MultiCounter();

		assertEquals(0, counter.getCount("A"));
		assertEquals(0, counter.getCount("B"));
		counter.increment("A");
		counter.increment("A");
		counter.increment("B");
		assertEquals(2, counter.getCount("A"));
		assertEquals(1, counter.getCount("B"));
		counter.increment("B");
		counter.increment("B");
		counter.increment("B");
		assertEquals(2, counter.getCount("A"));
		assertEquals(4, counter.getCount("B"));
	}
	
	@Test
	public void testSetDefault() {
		MultiCounter counter = new MultiCounter(100);

		assertEquals(100, counter.getCount("A"));
		assertEquals(100, counter.getCount("B"));
		counter.increment("A");
		counter.increment("B");
		assertEquals(101, counter.getCount("A"));
		assertEquals(101, counter.getCount("B"));
		counter.increment("A");
		counter.increment("B");
		assertEquals(102, counter.getCount("A"));
		assertEquals(102, counter.getCount("B"));
	}
	
	@Test
	public void testReset() {
		MultiCounter counter = new MultiCounter();

		counter.increment("A");
		assertEquals(1, counter.getCount("A"));
		counter.reset("A");
		counter.reset("B");
		assertEquals(0, counter.getCount("A"));
		assertEquals(0, counter.getCount("B"));
		counter.increment("A");
		counter.increment("B");
		assertEquals(1, counter.getCount("A"));
		assertEquals(1, counter.getCount("B"));
	}
}
