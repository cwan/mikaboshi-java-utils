package net.mikaboshi.property;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

public class PropertyFileLoaderTest {

	private PropertyFileLoader loader;
	
	private static final String PROP_PATH = "net/mikaboshi/property/testdata.properties";
	
	@Before
	public void setUp() throws IOException {
		this.loader = new PropertyFileLoader(
				new File("src/test/resources/" + PROP_PATH));
	}
	
	/**
	 * 設定するプロパティなし。
	 */
	@Test
	public void testNoProperty() throws IOException {
		
		@SuppressWarnings("unused")
		class Clazz {
			
			String a;
			String b;
			
			@Property
			public void setA(String arg) {
				this.a = arg;
			}
			
			public void setB(String arg) {
				this.b = arg;
			}
		}
		
		Clazz obj = new Clazz();
		
		this.loader.load(obj);
		
		assertNull(obj.a);
		assertNull(obj.b);
	}
	
	/**
	 * Stringプロパティを設定する。
	 */
	@Test
	public void testSetStringProperty() throws IOException {
		
		@SuppressWarnings("unused")
		class Clazz {
			
			String prop1;
			
			@Property
			public void setProp1(String arg) {
				this.prop1 = arg;
			}
		}
		
		Clazz obj = new Clazz();
		
		this.loader.load(obj);
		
		assertEquals("aaa", obj.prop1);
	}
	
	/**
	 * 配列プロパティを設定する。
	 */
	@Test
	public void testSetArrayProperties() throws IOException {
		
		@SuppressWarnings("unused")
		class Clazz {
			
			String[] prop;
			
			@Property(alias = "prop3")
			public void setProp(String[] arg) {
				this.prop = arg;
			}
		}
		
		Clazz obj = new Clazz();
		
		this.loader.load(obj);
		
		assertNotNull(obj.prop);
		assertEquals(3, obj.prop.length);
		assertEquals("ccc", obj.prop[0]);
		assertEquals("あああ", obj.prop[1]);
		assertEquals("", obj.prop[2]);
	}
	
	/**
	 * リストプロパティを設定する。
	 */
	@Test
	public void testSetListProperties() throws IOException {
		
		@SuppressWarnings("unused")
		class Clazz {
			
			List<String> prop;
			
			@Property(alias = "prop3")
			public void setProp(List<String> arg) {
				this.prop = arg;
			}
		}
		
		Clazz obj = new Clazz();
		
		this.loader.load(obj);
		
		assertNotNull(obj.prop);
		assertEquals(3, obj.prop.size());
		assertEquals("ccc", obj.prop.get(0));
		assertEquals("あああ", obj.prop.get(1));
		assertEquals("", obj.prop.get(2));
	}
	
	/**
	 * ファイルの読み直しを行う。
	 */
	@Test
	public void testReload() throws IOException {
		
		@SuppressWarnings("unused")
		class Clazz {
			
			String prop1;
			String prop2;
			
			@Property
			public void setProp1(String arg) {
				this.prop1 = arg;
			}
			
			@Property
			public void setProp2(String arg) {
				this.prop2 = arg;
			}
		}
		
		Clazz obj = new Clazz();
		
		this.loader.load(obj, false);
		
		assertEquals("aaa", obj.prop1);
		assertEquals("bbb", obj.prop2);
		
		obj.prop1 = null;
		obj.prop2 = null;
		
		this.loader.load(obj, true);
		
		assertEquals("aaa", obj.prop1);
		assertEquals("bbb", obj.prop2);
	}
	
	/**
	 * リソースパスからの読み込み。
	 */
	@Test
	public void testSetPropertyFromResourcePath() throws IOException {
		
		@SuppressWarnings("unused")
		class Clazz {
			
			String prop1;
			
			@Property
			public void setProp1(String arg) {
				this.prop1 = arg;
			}
		}
		
		Clazz obj = new Clazz();
		
		PropertyFileLoader l = new PropertyFileLoader(PROP_PATH);
		l.load(obj);
		
		assertEquals("aaa", obj.prop1);
	}
	
	/**
	 * リソースパスからの読み込みで、ファイルが無い。
	 */
	@Test(expected = IOException.class)
	public void testSetPropertyFromResourcePathNoFile() throws IOException {
		
		new PropertyFileLoader(
				"net/mikaboshi/no_such_file.properties");
	}
	
	/**
	 * リソースパスからの読み込みで、パスがブランク。
	 */
	@Test(expected = IllegalArgumentException.class)
	public void testSetPropertyFromResourcePathBlank() throws IOException {
		
		new PropertyFileLoader("");
	}
}
