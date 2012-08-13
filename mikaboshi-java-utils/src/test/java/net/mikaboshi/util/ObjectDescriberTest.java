package net.mikaboshi.util;

import static org.junit.Assert.*;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.mikaboshi.util.ObjectDescriber.Mode;

import org.junit.Test;

public class ObjectDescriberTest {

	@Test
	public void testNull() throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		
		String string = null;
		
		assertEquals(
				"\"string\" : null", 
				new ObjectDescriber().toString("string", string));
	}
	
	@Test
	public void testString() throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		
		String string = "aaa";
		
		assertEquals(
				"\"string\" : " + getValue(string), 
				new ObjectDescriber().toString("string", string));
	}
	
	@Test
	public void testInteger() throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		
		Integer number = 12345;
		
		assertEquals(
				"\"number\" : " + getValue(number), 
				new ObjectDescriber().toString("number", number));
	}
	
	@Test
	public void testDouble() throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		
		Double number = 12.345d;
		
		assertEquals(
				"\"number\" : " + getValue(number), 
				new ObjectDescriber().toString("number", number));
	}
	
	@Test
	public void testStringArray() throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		
		String[] array = new String[] {"aaa", "bbb", "ccc"};
		
		String expected = "\"array\" : [" + getValue(array[0]) + ", " + getValue(array[1]) + ", " + getValue(array[2]) + "]";
		
		assertEquals(
				expected, 
				new ObjectDescriber().toString("array", array));
	}
	
	@Test
	public void testObjectArray() throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		
		Object[] array = new Object[] {"aaa", 12, null, true};
		
		String expected = "\"array\" : [" + getValue(array[0]) + ", " + getValue(array[1]) + ", " + getValue(array[2]) + ", " + getValue(array[3]) + "]";
		
		assertEquals(
				expected, 
				new ObjectDescriber().toString("array", array));
	}
	
	@Test
	public void testInnerArray() throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		
		InnerArray object = new InnerArray();
		
		String expected = String.format(
			"\"object\" : {\"arr\" : \"<[Ljava.lang.String;@%s>\"} \"<net.mikaboshi.util.ObjectDescriberTest$InnerArray@%s>\"",
			Integer.toHexString(object.arr.hashCode()),
			Integer.toHexString(object.hashCode()) );
		
		assertEquals(
				expected, 
				new ObjectDescriber().toString("object", object));
	}
	
	@Test
	public void testCollection() throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		
		List<String> list = Arrays.asList("aaa", "bbb", "ccc");
		
		String expected = "\"list\" : [" + getValue(list.get(0)) + ", " + getValue(list.get(1)) + ", " + getValue(list.get(2)) + "]"
			+ " \"<" + list.getClass().getName() + "@" + Integer.toHexString(list.hashCode()) + ">\"";
		
		assertEquals(
				expected, 
				new ObjectDescriber().toString("list", list));
	}
	
	@Test
	public void testMap() throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		
		Map<Object, Object> map = new HashMap<Object, Object>();

		Integer key1 = 1;
		String value1 = "壱";
		String key2 = "abc";
		String value2 = "ABC";
		
		map.put(key1, value1);
		map.put(key2, value2);
		
		String expected = String.format( 
			"\"map\" : {\"1\" \"<java.lang.Integer@%s>\" : \"壱\" \"<java.lang.String@%s>\", \"abc\" \"<java.lang.String@%s>\" : \"ABC\" \"<java.lang.String@%s>\"} \"<java.util.HashMap@%s>\"",
			Integer.toHexString(key1.hashCode()),
			Integer.toHexString(value1.hashCode()),
			Integer.toHexString(key2.hashCode()),
			Integer.toHexString(value2.hashCode()),
			Integer.toHexString(map.hashCode())
		);
		
		assertEquals(
				expected,
				new ObjectDescriber().toString("map", map));
	}
	
	@Test
	public void testObject() throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		
		Foo foo = new Foo();
		
		String expected1 = String.format(
				"\"foo\" : {\"bar\" : {\"baz\" : {\"value3\" : [\"ccc\" \"<java.lang.String@%s>\", \"ddd\" \"<java.lang.String@%s>\", \"eee\" \"<java.lang.String@%s>\"] \"<java.util.Arrays$ArrayList@%s>\", \"value4\" : \"0\" \"<java.lang.Integer@0>\", \"value5\" : null, \"foo\" : null} \"<net.mikaboshi.util.ObjectDescriberTest$Baz@%s>\", \"value2\" : [\"aaa\" \"<java.lang.String@%s>\", \"bbb\" \"<java.lang.String@%s>\"]} \"<net.mikaboshi.util.ObjectDescriberTest$Bar@%s>\", \"value1\" : \"VALUE1\" \"<java.lang.String@%s>\"} \"<net.mikaboshi.util.ObjectDescriberTest$Foo@%s>\"",
				Integer.toHexString(foo.bar.baz.value3.get(0).hashCode()),
				Integer.toHexString(foo.bar.baz.value3.get(1).hashCode()),
				Integer.toHexString(foo.bar.baz.value3.get(2).hashCode()),
				Integer.toHexString(foo.bar.baz.value3.hashCode()),
				Integer.toHexString(foo.bar.baz.hashCode()),
				Integer.toHexString(foo.bar.value2[0].hashCode()),
				Integer.toHexString(foo.bar.value2[1].hashCode()),
				Integer.toHexString(foo.bar.hashCode()),
				Integer.toHexString(foo.value1.hashCode()),
				Integer.toHexString(foo.hashCode())
		);
		
		assertEquals(
				expected1, 
				new ObjectDescriber(Mode.FIELD_RECURSIVE).toString("foo", foo));
		
		String expected2 = String.format(
				"\"foo\" : {\"bar\" : {\"baz\" : {\"foo\" : null, \"value3\" : [\"ccc\" \"<java.lang.String@%s>\", \"ddd\" \"<java.lang.String@%s>\", \"eee\" \"<java.lang.String@%s>\"] \"<java.util.Arrays$ArrayList@%s>\", \"value4\" : \"0\" \"<java.lang.Integer@0>\", \"value5\" : null} \"<net.mikaboshi.util.ObjectDescriberTest$Baz@%s>\", \"value2\" : [\"aaa\" \"<java.lang.String@%s>\", \"bbb\" \"<java.lang.String@%s>\"]} \"<net.mikaboshi.util.ObjectDescriberTest$Bar@%s>\", \"value1\" : \"VALUE1\" \"<java.lang.String@%s>\"} \"<net.mikaboshi.util.ObjectDescriberTest$Foo@%s>\"",
				Integer.toHexString(foo.bar.baz.value3.get(0).hashCode()),
				Integer.toHexString(foo.bar.baz.value3.get(1).hashCode()),
				Integer.toHexString(foo.bar.baz.value3.get(2).hashCode()),
				Integer.toHexString(foo.bar.baz.value3.hashCode()),
				Integer.toHexString(foo.bar.baz.hashCode()),
				Integer.toHexString(foo.bar.value2[0].hashCode()),
				Integer.toHexString(foo.bar.value2[1].hashCode()),
				Integer.toHexString(foo.bar.hashCode()),
				Integer.toHexString(foo.value1.hashCode()),
				Integer.toHexString(foo.hashCode())
		);
		
		assertEquals(
				expected2, 
				new ObjectDescriber(Mode.ACCESSOR_RECURSIVE).toString("foo", foo));
		
		
		String expected3 = String.format(
				"\"foo\" : {\"bar\" : \"<net.mikaboshi.util.ObjectDescriberTest$Bar@%s>\", \"value1\" : \"VALUE1\" \"<java.lang.String@%s>\"} \"<net.mikaboshi.util.ObjectDescriberTest$Foo@%s>\"",
				Integer.toHexString(foo.bar.hashCode()),
				Integer.toHexString(foo.value1.hashCode()),
				Integer.toHexString(foo.hashCode())
		);
		
		assertEquals(
				expected3, 
				new ObjectDescriber(Mode.FIELD).toString("foo", foo));
		
		String expected4 = String.format(
				"\"foo\" : {\"bar\" : \"<net.mikaboshi.util.ObjectDescriberTest$Bar@%s>\", \"value1\" : \"VALUE1\" \"<java.lang.String@%s>\"} \"<net.mikaboshi.util.ObjectDescriberTest$Foo@%s>\"",
				Integer.toHexString(foo.bar.hashCode()),
				Integer.toHexString(foo.value1.hashCode()),
				Integer.toHexString(foo.hashCode())
		);
		
		assertEquals(
				expected4, 
				new ObjectDescriber(Mode.ACCESSOR).toString("foo", foo));
		
	}
	
	@Test
	public void testCircularReference() throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		
		Foo foo = new Foo();
		foo.bar.baz.foo = foo;
		foo.bar.baz.value3.set(0, foo.bar.value2[0]);
		
		String expected = String.format(
				"\"foo\" : {\"bar\" : {\"baz\" : {\"value3\" : [\"aaa\" \"<java.lang.String@%s>\", \"ddd\" \"<java.lang.String@%s>\", \"eee\" \"<java.lang.String@%s>\"] \"<java.util.Arrays$ArrayList@%s>\", \"value4\" : \"0\" \"<java.lang.Integer@0>\", \"value5\" : null, \"foo\" : (Circular reference) \"<net.mikaboshi.util.ObjectDescriberTest$Foo@%s>\"} \"<net.mikaboshi.util.ObjectDescriberTest$Baz@%s>\", \"value2\" : [\"aaa\" \"<java.lang.String@%s>\", \"bbb\" \"<java.lang.String@%s>\"]} \"<net.mikaboshi.util.ObjectDescriberTest$Bar@%s>\", \"value1\" : \"VALUE1\" \"<java.lang.String@%s>\"} \"<net.mikaboshi.util.ObjectDescriberTest$Foo@%s>\"",
				Integer.toHexString(foo.bar.baz.value3.get(0).hashCode()),
				Integer.toHexString(foo.bar.baz.value3.get(1).hashCode()),
				Integer.toHexString(foo.bar.baz.value3.get(2).hashCode()),
				Integer.toHexString(foo.bar.baz.value3.hashCode()),
				Integer.toHexString(foo.hashCode()),
				Integer.toHexString(foo.bar.baz.hashCode()),
				Integer.toHexString(foo.bar.value2[0].hashCode()),
				Integer.toHexString(foo.bar.value2[1].hashCode()),
				Integer.toHexString(foo.bar.hashCode()),
				Integer.toHexString(foo.value1.hashCode()),
				Integer.toHexString(foo.hashCode())
		);
		
		
		assertEquals(
				expected, 
				new ObjectDescriber(Mode.FIELD_RECURSIVE).toString("foo", foo));
		
	}
	
	@SuppressWarnings("unused")
	private static class Foo {
		
		private Bar bar = new Bar();
		
		private String value1 = "VALUE1";
		
		public Bar getBar() {
			return bar;
		}
		
		public String getValue1() {
			return value1;
		}
	}
	
	@SuppressWarnings("unused")
	private static class Bar {
		
		private Baz baz = new Baz();
		
		private String[] value2 = new String[] {"aaa", "bbb"};
		
		public Baz getBaz() {
			return baz;
		}
		
		public String[] getValue2() {
			return value2;
		}
	}
	
	@SuppressWarnings("unused")
	private static class Baz {
		
		private List<String> value3 = Arrays.asList("ccc", "ddd", "eee");
		
		private Integer value4 = 0;
		
		private String value5 = null;
		
		private Foo foo;
		
		public Foo getFoo() {
			return foo;
		}
		
		public List<String> getValue3() {
			return value3;
		}
		
		public Integer getValue4() {
			return value4;
		}
		
		public String getValue5() {
			return value5;
		}
	}
	
	private static class InnerArray {
		
		private String[] arr = new String[] {"AAA", "BBB", "CCC"};
	}
	
	private String getValue(Object object) {
		if (object == null) {
			return "null";
		} else {
			return "\"" + object.toString() + "\" \"<" + object.getClass().getName() + "@" + Integer.toHexString(object.hashCode()) + ">\"";
		}
	}
}
