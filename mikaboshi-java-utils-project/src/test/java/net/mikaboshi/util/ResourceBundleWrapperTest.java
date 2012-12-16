package net.mikaboshi.util;

import static org.junit.Assert.*;

import java.util.Properties;
import java.util.ResourceBundle;

import org.junit.BeforeClass;
import org.junit.Test;

public class ResourceBundleWrapperTest {

	private static ResourceBundleWrapper bundle;
	
	@BeforeClass
	public static void setUpClass() {
		bundle = new ResourceBundleWrapper(
				ResourceBundle.getBundle("net.mikaboshi.util.test"));
	}
	
	@Test
	public void testGetStringByNullKey() {
		assertNull(bundle.getString(null));
	}
	
	@Test
	public void testGetStringByAbsentKey() {
		assertNull(bundle.getString("no_such_key"));
	}
	
	@Test
	public void testGetStringByPresentKey() {
		assertEquals("A", bundle.getString("a"));
		assertEquals("B3", bundle.getString("b"));
		assertEquals("あ", bundle.getString("c"));
	}
	
	@Test
	public void testGetStringGetDefault() {
		String def = "default value";
		assertEquals(def, bundle.getString(null, def));
	}
	
	@Test
	public void testFormatSuccess() {
		assertEquals("hello, world.", bundle.format("fmt", "world"));
	}
	
	@Test
	public void testFormatFailure() {
		assertEquals("hello, {0}.", bundle.format("fmt"));
	}
	
	@Test
	public void testFormatByAbsentKey() {
		assertEquals("no_such_key", bundle.format("no_such_key"));
	}
	
	@Test
	public void testToProperties() {
		Properties prop = bundle.toProperties();
		
		assertEquals(4, prop.size());
		assertEquals("A", prop.getProperty("a"));
		assertEquals("B3", prop.getProperty("b"));
		assertEquals("あ", prop.getProperty("c"));
		assertEquals("hello, {0}.", prop.getProperty("fmt"));
		assertNull(prop.getProperty("no_such_key"));
	}
}
