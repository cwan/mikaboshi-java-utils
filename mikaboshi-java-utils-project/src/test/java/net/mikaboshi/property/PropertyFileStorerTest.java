package net.mikaboshi.property;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import net.mikaboshi.property.Property.Mode;

import org.junit.Before;
import org.junit.Test;

public class PropertyFileStorerTest {

	private PropertyFileStorer storer;
	
	private File file = new File("src/test/resources/net/mikaboshi/property/storerTest1.properties");
	
	@Before
	public void setUp() {
		
		if (this.file.exists()) {
			this.file.delete();
		}
		
		this.storer = new PropertyFileStorer(this.file);
	}
	
	/**
	 * プロパティなし。
	 */
	@Test
	public void testStoreNoProperty() throws IOException {
		
		@SuppressWarnings("unused")
		class Clazz {
			@Property(mode = Mode.SET)
			public String getProp1() {
				return "value1";
			}
		}
		
		this.storer.store(new Clazz());
		
		assertTrue(this.file.exists());
		
		Properties prop = PropertyFileLoader.loadPropertyFile(this.file);
		assertEquals(0, prop.size());
	}
	
	/**
	 * プロパティ1つ。
	 */
	@Test
	public void testStore1Poroperty() throws IOException {
		
		@SuppressWarnings("unused")
		class Clazz {
			@Property(mode = Mode.GET)
			public String getProp1() {
				return "value1";
			}
		}
		
		this.storer.store(new Clazz());
		
		assertTrue(this.file.exists());
		
		Properties prop = PropertyFileLoader.loadPropertyFile(this.file);
		assertEquals(1, prop.size());
		assertEquals("value1", prop.get("prop1"));
	}
	
	/**
	 * プロパティ3つ。
	 */
	@Test
	public void testStore3Poroperties() throws IOException {
		
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
		
		this.storer.store(new Clazz());
		
		assertTrue(this.file.exists());
		
		Properties prop = PropertyFileLoader.loadPropertyFile(this.file);
		assertEquals(3, prop.size());
		assertEquals("value1", prop.get("prop1"));
		assertEquals("value2", prop.get("prop2"));
		assertEquals("value3", prop.get("xxx.yyy.zzz"));
	}

	/**
	 * 配列プロパティとリストプロパティ。
	 */
	@Test
	public void testStoreArrayAndListPoroperties() throws IOException {
		
		@SuppressWarnings("unused")
		class Clazz {
			@Property(mode = Mode.GET)
			public String[] getProp1() {
				return new String[] {"value1-1", "value1-2"};
			}
			
			@Property(mode = Mode.GET)
			public String[] getProp2() {
				return new String[] {};
			}
			
			@Property(mode = Mode.GET, alias = "prop3")
			public List<String> getList() {
				return Arrays.asList("value3-1", "value3-2", "value3-3");
			}
		}
		
		this.storer.store(new Clazz());
		
		assertTrue(this.file.exists());
		
		Properties prop = PropertyFileLoader.loadPropertyFile(this.file);
		assertEquals(5, prop.size());
		assertEquals("value1-1", prop.get("prop1[0]"));
		assertEquals("value1-2", prop.get("prop1[1]"));
		assertNull(prop.get("prop2"));
		assertNull(prop.get("prop2[0]"));
		assertEquals("value3-1", prop.get("prop3[0]"));
		assertEquals("value3-2", prop.get("prop3[1]"));
		assertEquals("value3-3", prop.get("prop3[2]"));
	}
	
	/**
	 * Booleanプロパティ。
	 */
	@Test
	public void testStoreBooleanPoroperty() throws IOException {
		
		@SuppressWarnings("unused")
		class Clazz {
			@Property(mode = Mode.GET)
			public Boolean getProp1() {
				return Boolean.TRUE;
			}
			
			@Property(mode = Mode.GET)
			public Boolean getProp2() {
				return Boolean.FALSE;
			}
		}
		
		this.storer.store(new Clazz());
		
		assertTrue(this.file.exists());
		
		Properties prop = PropertyFileLoader.loadPropertyFile(this.file);
		assertEquals(2, prop.size());
		assertEquals("true", prop.get("prop1"));
		assertEquals("false", prop.get("prop2"));
	}
	
	/**
	 * storeOnExitのテスト。
	 * 実行後に以下のファイルが存在し、中身が 「prop1=value1」のみであることを
	 * 目視確認すること。
	 * <br/>
	 * src/test/resources/net/mikaboshi/property/storerTest2.properties
	 */
	@Test
	public void testStoreOnExit() throws IOException {
		
		@SuppressWarnings("unused")
		class Clazz {
			@Property(mode = Mode.GET)
			public String getProp1() {
				return "value1";
			}
		}
		
		File f = new File("src/test/resources/net/mikaboshi/property/storerTest2.properties");
		
		if (f.exists()) {
			f.delete();
		}
		
		PropertyFileStorer s = new PropertyFileStorer(f);
		
		s.storeOnExit(new Clazz());
		
		assertFalse(this.file.exists());
	}
}
