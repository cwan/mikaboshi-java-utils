package net.mikaboshi.jdbc.schema;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.sql.SQLException;
import java.util.List;
import java.util.Set;

import net.mikaboshi.jdbc.HSQLDBTestCase;

import org.junit.Test;

@SuppressWarnings("boxing")
public class SchemaUtilsTest extends HSQLDBTestCase {

	@Test
	public void testGetAllTableNames() throws SQLException {
		
		Set<String> tableNames = 
			SchemaUtils.getAllTableNames(getConnection(), null, "", "%E%", null);
		
		for (String tableName : tableNames) {
			System.out.println(tableName);
		}
		
	}
	
	@Test
	public void testGetColumnInfo() throws SQLException {
		
		List<ColumnInfo> infoList =
			SchemaUtils.getColumnInfo(
					getConnection().getMetaData(),
					null,
					"",
					"EMP",
					null);
		
		System.out.println(infoList.size());
		
		for (ColumnInfo info : infoList) {
			System.out.println(info);
		}

	}
	
	@Test
	public void testGetPrimaryKeys() throws Exception {
		Set<PrimaryKeyInfo> infoSet =
			SchemaUtils.getPrimaryKeys(
					getConnection().getMetaData(),
					null,
					null,
					"SAMPLE_TAB2");
		
		assertEquals(1, infoSet.size());
		
		for (PrimaryKeyInfo info : infoSet) {
			System.out.println(info.getPkName());
			System.out.println(info.getTableName());
			
			String[] columnNames = info.getColumnNames();
			assertEquals(2, columnNames.length);
			assertTrue("id1".equalsIgnoreCase(columnNames[0]));
			assertTrue("id2".equalsIgnoreCase(columnNames[1]));
		}			
	}
	
	@Test
	public void testGetColumnInfoWithPK_SimgleKey() throws SQLException {
		List<ColumnInfo> infoList = SchemaUtils.getColumnInfoWithPK(
				getConnection().getMetaData(),
				null,
				null,
				"SAMPLE_TAB1");
		
		int count = 0;
		for (ColumnInfo info : infoList) {
			if (info.getPrimaryKeyOrder() > 0) {
				count++;
				assertTrue("id".equalsIgnoreCase(info.getColumnName()));
			}
		}
		
		assertEquals(1, count);
	}
	
	@Test
	public void testGetColumnInfoWithPK_MultiKey() throws SQLException {
		List<ColumnInfo> infoList = SchemaUtils.getColumnInfoWithPK(
				getConnection().getMetaData(),
				null,
				null,
				"SAMPLE_TAB2");
		
		int count = 0;
		for (ColumnInfo info : infoList) {
			if (info.getPrimaryKeyOrder() == 1) {
				count++;
				assertTrue("id1".equalsIgnoreCase(info.getColumnName()));
			} else 	if (info.getPrimaryKeyOrder() == 2) {
				count++;
				assertTrue("id2".equalsIgnoreCase(info.getColumnName()));
			}
		}
		
		assertEquals(2, count);
	}
	
	@Test
	public void testExistsTableTrue() throws SQLException {
		// データベースによって、大文字だったり小文字だったりする
		
		boolean result1 = SchemaUtils.existsTable(
				getConnection().getMetaData(),
				null, null, "sample_tab1");
		
		boolean result2 = SchemaUtils.existsTable(
				getConnection().getMetaData(),
				null, null, "SAMPLE_TAB2");
		
		assertTrue(result1 || result2);
	}
	
	@Test
	public void testExistsTableFalse() throws SQLException {
		boolean result = SchemaUtils.existsTable(
				getConnection().getMetaData(),
				null, null, "no_such_table");
		
		assertFalse(result);
	}
}
