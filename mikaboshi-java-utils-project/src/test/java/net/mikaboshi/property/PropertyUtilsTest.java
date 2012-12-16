package net.mikaboshi.property;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import net.mikaboshi.property.Property.Mode;

import org.junit.Test;

public class PropertyUtilsTest {

	/**
	 * getPropertyNameMap
	 * Propertyアノテーションの付いたメソッドが１つも無い。
	 */
	@Test
	public void testGetPropertyNameMapNoProperty() {
		
		class Clazz {
			@SuppressWarnings("unused")
			public void m1() {
			}
		}
		
		Map<Method, String> map = 
			PropertyUtils.getPropertyNameMap(Clazz.class);
		assertEquals(0, map.size());
	}
	
	
	/**
	 * getPropertyNameMap
	 * Propertyアノテーションの付いたメソッドが１つ。（aliasあり）
	 */
	@Test
	public void testGetPropertyNameMap1PropertyWithAlias() 
		throws SecurityException, NoSuchMethodException {
		
		class Clazz {
			@SuppressWarnings("unused")
			@Property(alias = "prop1")
			public void m1(String arg) {
			}
		}
		
		Map<Method, String> map = 
			PropertyUtils.getPropertyNameMap(Clazz.class);
		assertEquals(1, map.size());
		assertEquals("prop1", 
				map.get(Clazz.class.getMethod("m1", String.class)));
	}
	
	/**
	 * getPropertyNameMap
	 * Propertyアノテーションの付いたメソッドが1つ。（aliasなし）
	 */
	@Test
	public void testGetPropertyNameMap1PropertyWithoutAlias() 
		throws SecurityException, NoSuchMethodException {
		
		class Clazz {
			@SuppressWarnings("unused")
			@Property
			public void m1(String arg) {
			}
		}
		
		Map<Method, String> map = 
			PropertyUtils.getPropertyNameMap(Clazz.class);
		assertEquals(1, map.size());
		assertEquals("m1", 
				map.get(Clazz.class.getMethod("m1", String.class)));
	}
	
	/**
	 * getPropertyNameMap
	 * setで始まるメソッド。
	 */
	@Test
	public void testGetPropertyNameMapMethodNameStartsWithSet() 
		throws SecurityException, NoSuchMethodException {
		
		@SuppressWarnings("unused")
		class Clazz {
			
			@Property
			public void setAaaBbb(String arg) {
			}
			
			@Property
			public void set(String arg) {
			}
			
			@Property
			public void set1(String arg) {
			}
		}
		
		Map<Method, String> map = 
			PropertyUtils.getPropertyNameMap(Clazz.class);
		assertEquals(3, map.size());
		assertEquals("aaaBbb", 
				map.get(Clazz.class.getMethod("setAaaBbb", String.class)));
		assertEquals("set", 
				map.get(Clazz.class.getMethod("set", String.class)));
		assertEquals("1", 
				map.get(Clazz.class.getMethod("set1", String.class)));
	}

	/**
	 * getPropertyNameMap
	 * 引数なし。
	 */
	@Test(expected = IllegalArgumentException.class)
	public void testGetPropertyNameMapNoParameter() 
		throws SecurityException, NoSuchMethodException {
		
		class Clazz {
			@SuppressWarnings("unused")
			@Property
			public void m1() {
			}
		}
		
		@SuppressWarnings("unused")
		Map<Method, String> map = 
			PropertyUtils.getPropertyNameMap(Clazz.class);
	}
	
	/**
	 * getPropertyNameMap
	 * 様々な型。
	 */
	@Test
	public void testGetPropertyNameMapAllType() 
		throws SecurityException, NoSuchMethodException {
		
		@SuppressWarnings("unused")
		class Clazz {
			@Property
			public void setString(String arg) {}
			@Property
			public void setBoolean(Boolean arg) {}
			@Property
			public void setInteger(Integer arg) {}
			@Property
			public void setLong(Long arg) {}
			@Property
			public void setDouble(Double arg) {}
			@Property
			public void setStrings(String[] arg) {}
			@Property
			public void setBooleans(Boolean[] arg) {}
			@Property
			public void setIntegers(Integer[] arg) {}
			@Property
			public void setLongs(Long[] arg) {}
			@Property
			public void setDoubles(Double[] arg) {}
			@Property
			public void setList(List<String> arg) {}
		}
		
		Map<Method, String> map = 
			PropertyUtils.getPropertyNameMap(Clazz.class);
		assertEquals(11, map.size());
		assertEquals("string", 
				map.get(Clazz.class.getMethod("setString", String.class)));
		assertEquals("boolean", 
				map.get(Clazz.class.getMethod("setBoolean", Boolean.class)));
		assertEquals("integer", 
				map.get(Clazz.class.getMethod("setInteger", Integer.class)));
		assertEquals("long", 
				map.get(Clazz.class.getMethod("setLong", Long.class)));
		assertEquals("double", 
				map.get(Clazz.class.getMethod("setDouble", Double.class)));
		assertEquals("strings", 
				map.get(Clazz.class.getMethod("setStrings", String[].class)));
		assertEquals("booleans", 
				map.get(Clazz.class.getMethod("setBooleans", Boolean[].class)));
		assertEquals("integers", 
				map.get(Clazz.class.getMethod("setIntegers", Integer[].class)));
		assertEquals("longs", 
				map.get(Clazz.class.getMethod("setLongs", Long[].class)));
		assertEquals("doubles", 
				map.get(Clazz.class.getMethod("setDoubles", Double[].class)));
		assertEquals("list", 
				map.get(Clazz.class.getMethod("setList", List.class)));
	}
	
	/**
	 * getPropertyNameMap
	 * 引数の型が不正。
	 */
	@Test(expected = IllegalArgumentException.class)
	public void testGetPropertyNameMapIllegalParameterType() 
		throws SecurityException, NoSuchMethodException {
		
		class Clazz {
			@SuppressWarnings("unused")
			@Property
			public void m1(Float arg) {
			}
		}
		
		Map<Method, String> map = 
			PropertyUtils.getPropertyNameMap(Clazz.class);
		assertEquals(1, map.size());
		assertEquals("m1", 
				map.get(Clazz.class.getMethod("m1", String.class)));
	}
	
	/**
	 * getArrayValues
	 * 配列プロパティが1つもない。
	 */
	@Test
	public void testGetArrayValuesNoProperty() {
		// テストデータ
		Properties properties = new Properties();
		properties.put("key1", "value1");
		properties.put("key2", "value2");
		
		List<String> list = 
			PropertyUtils.getArrayValues(properties, "key");
		
		assertEquals(0, list.size());
	}
	
	/**
	 * getArrayValues
	 * 配列プロパティが1つ。
	 */
	@Test
	public void testGetArrayValues1Property() {
		// テストデータ
		Properties properties = new Properties();
		properties.put("key1", "value1");
		properties.put("key[0]", "valueX");
		properties.put("key2", "value2");
		
		List<String> list = 
			PropertyUtils.getArrayValues(properties, "key");
		
		assertEquals(1, list.size());
		assertEquals("valueX", list.get(0));
	}
	
	/**
	 * getArrayValues
	 * 配列プロパティが3つ。
	 */
	@Test
	public void testGetArrayValues3Properties() {
		// テストデータ
		Properties properties = new Properties();
		properties.put("key1", "value1");
		properties.put("key[0]", "valueX1");
		properties.put("key2", "value2");
		properties.put("key[2]", "valueX3");
		properties.put("key[1]", "valueX2");
		properties.put("key[4]", "valueX4");	// これは読み込まれない
		
		List<String> list = 
			PropertyUtils.getArrayValues(properties, "key");
		
		assertEquals(3, list.size());
		assertEquals("valueX1", list.get(0));
		assertEquals("valueX2", list.get(1));
		assertEquals("valueX3", list.get(2));
	}
	
	/**
	 * getPropertyByType
	 * type = String
	 */
	@Test
	public void testGetPropertyByTypeString() {
		Properties properties = new Properties();
		properties.put("a", "A");
		
		Object value = PropertyUtils.getPropertyByType(
				properties, "a", String.class, null);
		
		assertTrue(value instanceof String);
		assertEquals("A", value);
	}
	
	/**
	 * getPropertyByType
	 * type = Boolean
	 */
	@Test
	public void testGetPropertyByTypeBoolean() {
		Properties properties = new Properties();
		properties.put("a", "true");
		properties.put("b", "x");
		
		Object value1 = PropertyUtils.getPropertyByType(
				properties, "a", Boolean.class, null);
		
		assertTrue(value1 instanceof Boolean);
		assertEquals(Boolean.TRUE, value1);
		
		Object value2 = PropertyUtils.getPropertyByType(
				properties, "b", Boolean.class, null);
		
		assertNull(value2);
	}
	
	/**
	 * getPropertyByType
	 * type = Integer
	 */
	@Test
	public void testGetPropertyByTypeInteger() {
		Properties properties = new Properties();
		properties.put("a", "10");
		properties.put("b", "x");
		
		Object value1 = PropertyUtils.getPropertyByType(
				properties, "a", Integer.class, null);
		
		assertTrue(value1 instanceof Integer);
		assertEquals(10, value1);
		
		Object value2 = PropertyUtils.getPropertyByType(
				properties, "b", Integer.class, null);
		
		assertNull(value2);
	}
	
	/**
	 * getPropertyByType
	 * type = Long
	 */
	@Test
	public void testGetPropertyByTypeLong() {
		Properties properties = new Properties();
		properties.put("a", "11");
		properties.put("b", "x");
		
		Object value1 = PropertyUtils.getPropertyByType(
				properties, "a", Long.class, null);
		
		assertTrue(value1 instanceof Long);
		assertEquals(11L, value1);
		
		Object value2 = PropertyUtils.getPropertyByType(
				properties, "b", Long.class, null);
		
		assertNull(value2);
	}
	
	/**
	 * getPropertyByType
	 * type = Double
	 */
	@Test
	public void testGetPropertyByTypeDouble() {
		Properties properties = new Properties();
		properties.put("a", "12.3");
		properties.put("b", "x");
		
		Object value1 = PropertyUtils.getPropertyByType(
				properties, "a", Double.class, null);
		
		assertTrue(value1 instanceof Double);
		assertEquals(0, Double.compare(12.3d, (Double) value1));
		
		Object value2 = PropertyUtils.getPropertyByType(
				properties, "b", Double.class, null);
		
		assertNull(value2);
	}
	
	/**
	 * getPropertyByType
	 * type = Object
	 */
	@Test
	public void testGetPropertyByTypeObject() {
		Properties properties = new Properties();
		properties.put("a", "12.3");
		properties.put("b", "x");
		
		Object value1 = PropertyUtils.getPropertyByType(
				properties, "a", Object.class, null);
		
		assertEquals("12.3", value1);
		
		Object value2 = PropertyUtils.getPropertyByType(
				properties, "b", Object.class, null);
		
		assertEquals("x", value2);
	}
	
	/**
	 * getPropertyByType
	 * type = String[]
	 */
	@Test
	public void testGetPropertyByTypeStringArray() {
		Properties properties = new Properties();
		properties.put("a[0]", "A");
		properties.put("a[1]", "B");
		
		String[] value = PropertyUtils.getPropertyByType(
				properties, "a", String[].class, null);
		
		assertEquals(2, value.length);
		assertEquals("A", value[0]);
		assertEquals("B", value[1]);
	}
	
	/**
	 * getPropertyByType
	 * type = Boolean[]
	 */
	@Test
	public void testGetPropertyByTypeBooleanArray() {
		Properties properties = new Properties();
		properties.put("a[0]", "true");
		properties.put("a[1]", "B");
		properties.put("a[2]", "false");
		
		Boolean[] value = PropertyUtils.getPropertyByType(
				properties, "a", Boolean[].class, null);
		
		assertEquals(3, value.length);
		assertEquals(Boolean.TRUE, value[0]);
		assertEquals(null, value[1]);
		assertEquals(Boolean.FALSE, value[2]);
	}
	
	/**
	 * getPropertyByType
	 * type = Integer[]
	 */
	@Test
	public void testGetPropertyByTypeIntegerArray() {
		Properties properties = new Properties();
		properties.put("a[0]", "10");
		properties.put("a[1]", "B");
		properties.put("a[2]", "11");
		
		Integer[] value = PropertyUtils.getPropertyByType(
				properties, "a", Integer[].class, null);
		
		assertEquals(3, value.length);
		assertEquals(10, value[0].intValue());
		assertEquals(null, value[1]);
		assertEquals(11, value[2].intValue());
	}
	
	/**
	 * getPropertyByType
	 * type = Long[]
	 */
	@Test
	public void testGetPropertyByTypeLongArray() {
		Properties properties = new Properties();
		properties.put("a[0]", "10");
		properties.put("a[1]", "B");
		properties.put("a[2]", "11");
		
		Long[] value = PropertyUtils.getPropertyByType(
				properties, "a", Long[].class, null);
		
		assertEquals(3, value.length);
		assertEquals(10L, value[0].intValue());
		assertEquals(null, value[1]);
		assertEquals(11L, value[2].intValue());
	}
	
	/**
	 * getPropertyByType
	 * type = Double[]
	 */
	@Test
	public void testGetPropertyByTypeDoubleArray() {
		Properties properties = new Properties();
		properties.put("a[0]", "10");
		properties.put("a[1]", "B");
		properties.put("a[2]", "11.1");
		
		Double[] value = PropertyUtils.getPropertyByType(
				properties, "a", Double[].class, null);
		
		assertEquals(3, value.length);
		assertEquals(0, Double.compare(10.0d, value[0]));
		assertEquals(null, value[1]);
		assertEquals(0, Double.compare(11.1d, value[2]));
	}
	
	/**
	 * getPropertyByType
	 * type = Object[]
	 */
	@Test
	public void testGetPropertyByTypeObjectArray() {
		Properties properties = new Properties();
		properties.put("a[0]", "A");
		properties.put("a[1]", "B");
		
		Object[] value = PropertyUtils.getPropertyByType(
				properties, "a", Object[].class, null);
		
		assertEquals(2, value.length);
		assertEquals("A", value[0]);
		assertEquals("B", value[1]);
	}
	
	/**
	 * getPropertyByType
	 * type = Float[]
	 */
	@Test(expected = IllegalArgumentException.class)
	public void testGetPropertyByTypeIllegalArray() {
		Properties properties = new Properties();
		properties.put("a[0]", "10");
		properties.put("a[1]", "11.1");
		
		PropertyUtils.getPropertyByType(
				properties, "a", Float[].class, null);
	}
	
	/**
	 * getPropertyByType
	 * type = List<String>
	 */
	@Test
	public void testGetPropertyByTypeStringList() {
		Properties properties = new Properties();
		properties.put("a[0]", "A");
		properties.put("a[1]", "B");
		
		@SuppressWarnings("unchecked")
		List<String> list = PropertyUtils.getPropertyByType(
				properties, "a", List.class, String.class);
		
		assertEquals(2, list.size());
		assertEquals("A", list.get(0));
		assertEquals("B", list.get(1));
	}
	
	/**
	 * getPropertyByType
	 * type = List<Boolean>
	 */
	@Test
	public void testGetPropertyByTypeBooleanList() {
		Properties properties = new Properties();
		properties.put("a[0]", "A");
		properties.put("a[1]", "True");
		
		@SuppressWarnings("unchecked")
		List<Boolean> list = PropertyUtils.getPropertyByType(
				properties, "a", List.class, Boolean.class);
		
		assertEquals(2, list.size());
		assertNull(list.get(0));
		assertEquals(Boolean.TRUE, list.get(1));
	}
	
	/**
	 * getPropertyByType
	 * type = List<Integer>
	 */
	@Test
	public void testGetPropertyByTypeIntegerList() {
		Properties properties = new Properties();
		properties.put("a[0]", "99");
		properties.put("a[1]", "True");
		
		@SuppressWarnings("unchecked")
		List<Integer> list = PropertyUtils.getPropertyByType(
				properties, "a", List.class, Integer.class);
		
		assertEquals(2, list.size());
		assertEquals(99, list.get(0).intValue());
		assertNull(list.get(1));
	}
	
	/**
	 * getPropertyByType
	 * type = List<Long>
	 */
	@Test
	public void testGetPropertyByTypeLongList() {
		Properties properties = new Properties();
		properties.put("a[0]", "990");
		properties.put("a[1]", "True");
		
		@SuppressWarnings("unchecked")
		List<Long> list = PropertyUtils.getPropertyByType(
				properties, "a", List.class, Long.class);
		
		assertEquals(2, list.size());
		assertEquals(990L, list.get(0).intValue());
		assertNull(list.get(1));
	}
	
	/**
	 * getPropertyByType
	 * type = List<Double>
	 */
	@Test
	public void testGetPropertyByTypeDoubleList() {
		Properties properties = new Properties();
		properties.put("a[0]", "99");
		properties.put("a[1]", "True");
		
		@SuppressWarnings("unchecked")
		List<Double> list = PropertyUtils.getPropertyByType(
				properties, "a", List.class, Double.class);
		
		assertEquals(2, list.size());
		assertEquals(0, Double.compare(99.0d, list.get(0)));
		assertNull(list.get(1));
	}
	
	/**
	 * getPropertyByType
	 * type = List<Object>
	 */
	@Test
	public void testGetPropertyByTypeObjectList() {
		Properties properties = new Properties();
		properties.put("a[0]", "99");
		properties.put("a[1]", "True");
		
		@SuppressWarnings("unchecked")
		List<Object> list = PropertyUtils.getPropertyByType(
				properties, "a", List.class, Object.class);
		
		assertEquals(2, list.size());
		assertEquals("99", list.get(0));
		assertEquals("True", list.get(1));
	}
	
	/**
	 * getPropertyByType
	 * type = List<BigDecimal>
	 */
	@Test(expected = IllegalArgumentException.class)
	public void testGetPropertyByTypeIllegalList() {
		Properties properties = new Properties();
		properties.put("a[0]", "99");
		properties.put("a[1]", "True");
		
		PropertyUtils.getPropertyByType(
				properties, "a", List.class, BigDecimal.class);
	}
	
	/**
	 * load
	 * 引数の型が Boolean
	 * @throws InvocationTargetException 
	 * @throws IllegalAccessException 
	 */
	@Test
	public void testLoadBoolean() 
		throws IllegalAccessException, InvocationTargetException {
		
		@SuppressWarnings("unused")
		class Clazz {
			Boolean b1;
			Boolean b2;
			
			@Property
			public void setB1(Boolean b) {
				this.b1 = b;
			}
			@Property
			public void setB2(Boolean b) {
				this.b2 = b;
			}
		}
		
		Properties properties = new Properties();
		properties.put("b1", "true");
		properties.put("b2", "hoge");
		
		Clazz obj = new Clazz();
		
		PropertyUtils.load(obj, properties);
		assertEquals(Boolean.TRUE, obj.b1);
		assertNull(obj.b2);
	}
	
	/**
	 * store
	 * プロパティが1つもない。
	 */
	@Test
	public void testStoreNoProperty() 
		throws IllegalAccessException, InvocationTargetException {
		
		class Clazz {
			@SuppressWarnings("unused")
			public String m1() {
				return "";
			}
		}
		
		Properties properties = PropertyUtils.store(new Clazz());
		assertEquals(0, properties.size());
	}
	
	/**
	 * store
	 * 書き出しプロパティ1つ。
	 */
	@Test
	public void testStore1Property() 
		throws IllegalAccessException, InvocationTargetException {
		
		class Clazz {
			@SuppressWarnings("unused")
			@Property(mode = Mode.GET)
			public String getProp1() {
				return "value1";
			}
		}
		
		Properties properties = PropertyUtils.store(new Clazz());
		assertEquals(1, properties.size());
		assertEquals("value1", properties.get("prop1"));
	}
	
	/**
	 * store
	 * 書き出しプロパティ3つ。
	 */
	@Test
	public void testStore3Properties() 
		throws IllegalAccessException, InvocationTargetException {
		
		@SuppressWarnings("unused")
		class Clazz {
			
			@Property(mode = Mode.GET)
			public String getProp1() {
				return "value1";
			}
			
			@Property(mode = Mode.GET)
			public String prop2() {
				return "value2";
			}
			
			@Property(mode = Mode.GET, alias = "xxx.yyy.zzz")
			public String prop3() {
				return "value3";
			}
		}
		
		Properties properties = PropertyUtils.store(new Clazz());
		assertEquals(3, properties.size());
		assertEquals("value1", properties.get("prop1"));
		assertEquals("value2", properties.get("prop2"));
		assertEquals("value3", properties.get("xxx.yyy.zzz"));
	}
	
	/**
	 * store
	 * 戻り値が配列。
	 */
	@Test
	public void testStoreArrayProperties() 
		throws IllegalAccessException, InvocationTargetException {
		
		@SuppressWarnings("unused")
		class Clazz {
			
			@Property(mode = Mode.GET)
			public String[] getProp1() {
				return new String[] { "value1", "value2", "value3" };
			}
		}
		
		Properties properties = PropertyUtils.store(new Clazz());
		assertEquals(3, properties.size());
		assertEquals("value1", properties.get("prop1[0]"));
		assertEquals("value2", properties.get("prop1[1]"));
		assertEquals("value3", properties.get("prop1[2]"));
	}
	
	/**
	 * store
	 * 戻り値がリスト。
	 */
	@Test
	public void testStoreListProperties() 
		throws IllegalAccessException, InvocationTargetException {
		
		@SuppressWarnings("unused")
		class Clazz {
			
			@Property(mode = Mode.GET)
			public List<String> getProp1() {
				return Arrays.asList("value1", "value2", "value3");
			}
		}
		
		Properties properties = PropertyUtils.store(new Clazz());
		assertEquals(3, properties.size());
		assertEquals("value1", properties.get("prop1[0]"));
		assertEquals("value2", properties.get("prop1[1]"));
		assertEquals("value3", properties.get("prop1[2]"));
	}
	
	/**
	 * store
	 * 戻り値がBigDecimal。
	 */
	@Test
	public void testStoreOhterProperties() 
		throws IllegalAccessException, InvocationTargetException {
		
		@SuppressWarnings("unused")
		class Clazz {
			
			@Property(mode = Mode.GET)
			public List<BigDecimal> getProp1() {
				BigDecimal value2 = new BigDecimal(23.4d)
					.setScale(1, BigDecimal.ROUND_HALF_UP);
				
				return Arrays.asList(new BigDecimal(10), value2);
			}
			
			@Property(mode = Mode.GET)
			public BigDecimal getProp2() {
				return new BigDecimal(99);
			}
		}
		
		Properties properties = PropertyUtils.store(new Clazz());
		assertEquals(3, properties.size());
		assertEquals("10", properties.get("prop1[0]"));
		assertEquals("23.4", properties.get("prop1[1]"));
		assertEquals("99", properties.get("prop2"));
	}
}
