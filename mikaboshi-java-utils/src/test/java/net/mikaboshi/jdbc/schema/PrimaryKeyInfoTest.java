package net.mikaboshi.jdbc.schema;

import static junit.framework.Assert.*;

import net.mikaboshi.jdbc.schema.PrimaryKeyInfo;

import org.junit.Test;

public class PrimaryKeyInfoTest {

	@Test
	public void testEqualsAllNotNullTrue() {
		PrimaryKeyInfo pk1 = new PrimaryKeyInfo();
		pk1.setTableCat("CAT");
		pk1.setTableSchem("SCH");
		pk1.setTableName("NAM");
		
		PrimaryKeyInfo pk2 = new PrimaryKeyInfo();
		pk2.setTableCat("CAT");
		pk2.setTableSchem("SCH");
		pk2.setTableName("NAM");
		
		assertTrue(pk1.equals(pk2));
	}
	
	@Test
	public void testEqualsAllNotNullFalse() {
		PrimaryKeyInfo pk1 = new PrimaryKeyInfo();
		pk1.setTableCat("CAT");
		pk1.setTableSchem("SCH");
		pk1.setTableName("NAM1");
		
		PrimaryKeyInfo pk2 = new PrimaryKeyInfo();
		pk2.setTableCat("CAT");
		pk2.setTableSchem("SCH");
		pk2.setTableName("NAM2");
		
		assertFalse(pk1.equals(pk2));
	}
	
	@Test
	public void testEqualsNullTrue() {
		PrimaryKeyInfo pk1 = new PrimaryKeyInfo();
		pk1.setTableName("NAM");
		
		PrimaryKeyInfo pk2 = new PrimaryKeyInfo();
		pk2.setTableName("NAM");
		
		assertTrue(pk1.equals(pk2));
	}
	
	@Test
	public void testEqualsNullFalse() {
		PrimaryKeyInfo pk1 = new PrimaryKeyInfo();
		pk1.setTableName("NAM");
		
		PrimaryKeyInfo pk2 = new PrimaryKeyInfo();
		pk2.setTableName("NAM2");
		
		assertFalse(pk1.equals(pk2));
	}
	
	@Test
	public void testEqualsNullAndNotNullFalse() {
		PrimaryKeyInfo pk1 = new PrimaryKeyInfo();
		pk1.setTableCat("CAT");
		pk1.setTableSchem("SCH");
		pk1.setTableName("NAM");
		
		PrimaryKeyInfo pk2 = new PrimaryKeyInfo();
		pk2.setTableSchem("SCH");
		pk2.setTableName("NAM");
		
		assertFalse(pk1.equals(pk2));
	}
	
	@Test
	public void testEqualsNotNullAndNullFalse() {
		PrimaryKeyInfo pk1 = new PrimaryKeyInfo();
		pk1.setTableName("NAM");
		
		PrimaryKeyInfo pk2 = new PrimaryKeyInfo();
		pk2.setTableSchem("SCH");
		pk2.setTableName("NAM");
		
		assertFalse(pk1.equals(pk2));
	}
	
	@Test
	public void testGetColumnNames() {
		PrimaryKeyInfo pk = new PrimaryKeyInfo();
		pk.addColumnName(2, "COL2");
		pk.addColumnName(1, "COL1");
		pk.addColumnName(3, "COL3");
		pk.addColumnName(1, "COL1");
		
		String[] columnNames = pk.getColumnNames();
		
		assertEquals(4, columnNames.length);
		assertEquals("COL1", columnNames[0]);
		assertEquals("COL1", columnNames[1]);
		assertEquals("COL2", columnNames[2]);
		assertEquals("COL3", columnNames[3]);
	}
}
