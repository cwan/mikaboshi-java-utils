package net.mikaboshi.jdbc;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import net.mikaboshi.jdbc.ArrayToDbImporter;

import org.junit.Test;

@SuppressWarnings("boxing")
public class ArrayToDbImporterTest extends HSQLDBTestCase {
	
	/**
	 * 1行INSERT
	 * @throws SQLException
	 */
	@Test
	public void testInsert1() throws SQLException {
		String tableName = "SAMPLE_TAB1";
		
		int before = getRecordCount(tableName);
		
		ArrayToDbImporter importer =
			new ArrayToDbImporter(getConnection());
		importer.setTableName(tableName);
		importer.initialize();
		
		importer.execute(new String[] {"100", "X"});
		
		int after = getRecordCount(tableName);
		
		assertEquals(before + 1, after);
		
		importer.close();
	}
	
	/**
	 * 1行INSERT。カラム名指定。
	 * @throws SQLException
	 */
	@Test
	public void testInsert1WithColumnNames() throws SQLException {
		String tableName = "SAMPLE_TAB1";
		
		int before = getRecordCount(tableName);
		
		ArrayToDbImporter importer =
			new ArrayToDbImporter(getConnection());
		importer.setTableName(tableName);
		importer.setColumnNames(new String[] {"ID", "NAME"});
		importer.initialize();
		
		importer.execute(new String[] {"100", "X"});
		
		int after = getRecordCount(tableName);
		
		assertEquals(before + 1, after);
		
		importer.close();
	}
	
	/**
	 * 1行INSERT。カラム名指定（順番が違う）
	 * @throws SQLException
	 */
	@Test
	public void testInsert1WithColumnNamesRev() throws SQLException {
		String tableName = "SAMPLE_TAB1";
		
		int before = getRecordCount(tableName);
		
		ArrayToDbImporter importer =
			new ArrayToDbImporter(getConnection());
		importer.setTableName(tableName);
		importer.setColumnNames(new String[] {"NAME", "ID"});
		importer.initialize();
		
		importer.execute(new String[] {"X", "100"});
		
		int after = getRecordCount(tableName);
		
		assertEquals(before + 1, after);
		
		importer.close();
	}
	
	/**
	 * 1行INSERT。大文字/小文字を区別。
	 * @throws SQLException
	 */
	@Test
	public void testInsert1CaseSensitive() throws SQLException {
		String tableName = "SAMPLE_TAB1";
		
		int before = getRecordCount(tableName);
		
		ArrayToDbImporter importer =
			new ArrayToDbImporter(getConnection());
		importer.setTableName(tableName);
		importer.setColumnNames(new String[] {"ID", "NAME"});
		importer.setCaseSensitive(true);
		importer.initialize();
		
		importer.execute(new String[] {"100", "X"});
		
		int after = getRecordCount(tableName);
		
		assertEquals(before + 1, after);
		
		importer.close();
	}
	
	/**
	 * 1行INSERT。大文字/小文字を区別（テーブル名不一致）。
	 * @throws SQLException
	 */
	@Test(expected = SQLException.class)
	public void testInsert1CaseSensitiveTableNameUnmatch() throws SQLException {
		String tableName = "sample_tab1";
		
		int before = getRecordCount(tableName);
		
		ArrayToDbImporter importer =
			new ArrayToDbImporter(getConnection());
		importer.setTableName(tableName);
		importer.setColumnNames(new String[] {"ID", "NAME"});
		importer.setCaseSensitive(true);
		importer.initialize();
		
		importer.execute(new String[] {"100", "X"});
		
		int after = getRecordCount(tableName);
		
		assertEquals(before + 1, after);
		
		importer.close();
	}
	
	/**
	 * 1行INSERT。大文字/小文字を区別（カラム名不一致）。
	 * @throws SQLException
	 */
	@Test(expected = SQLException.class)
	public void testInsert1CaseSensitiveColumnNameUnmatch() throws SQLException {
		String tableName = "SAMPLE_TAB1";
		
		int before = getRecordCount(tableName);
		
		ArrayToDbImporter importer =
			new ArrayToDbImporter(getConnection());
		importer.setTableName(tableName);
		importer.setColumnNames(new String[] {"id", "name"});
		importer.setCaseSensitive(true);
		importer.initialize();
		
		importer.execute(new String[] {"100", "X"});
		
		int after = getRecordCount(tableName);
		
		assertEquals(before + 1, after);
		
		importer.close();
	}
	
	/**
	 * 2行INSERT
	 * @throws SQLException
	 */
	@Test
	public void testInsert2() throws SQLException {
		String tableName = "SAMPLE_TAB1";
		
		int before = getRecordCount(tableName);

		ArrayToDbImporter importer =
			new ArrayToDbImporter(getConnection());
		importer.setTableName(tableName);
		importer.initialize();
		
		importer.execute(new String[] {"100", "X"});
		importer.execute(new String[] {"101", "Y"});
		
		int after = getRecordCount(tableName);
		
		assertEquals(before + 2, after);
		
		importer.close();
	}
	
	/**
	 * PKの一意制約違反
	 * @throws SQLException
	 */
	@Test(expected = SQLException.class)
	public void testInsertPKConflict() throws SQLException {
		String tableName = "SAMPLE_TAB1";
		
		ArrayToDbImporter importer =
			new ArrayToDbImporter(getConnection());
		importer.setTableName(tableName);
		importer.initialize();
		
		try {
			importer.execute(new String[] {"1", "X"});
		} finally {
			importer.close();
		}
	}
	
	/**
	 * 1行Update
	 * @throws SQLException
	 */
	@Test
	public void testUpdate1() throws SQLException {
		String tableName = "SAMPLE_TAB1";
		
		int before = getRecordCount(tableName);
		
		ArrayToDbImporter importer =
			new ArrayToDbImporter(getConnection());
		importer.setTableName(tableName);
		importer.setReplace(true);
		importer.initialize();
		
		importer.execute(new String[] {"1", "X"});
		
		List<Map<String, Object>> resultList = getAllRecords(tableName);
		
		assertEquals(before, resultList.size());
		
		for (Map<String, Object> rowData : resultList) {
			if (rowData.get("id").equals(1)) {
				assertEquals("X",  rowData.get("name"));
			}
		}
		
		importer.close();
	}
	
	/**
	 * 2行Update
	 * @throws SQLException
	 */
	@Test
	public void testUpdate2() throws SQLException {
		String tableName = "SAMPLE_TAB1";
		
		int before = getRecordCount(tableName);
		
		ArrayToDbImporter importer =
			new ArrayToDbImporter(getConnection());
		importer.setTableName(tableName);
		importer.setReplace(true);
		importer.initialize();
		
		importer.execute(new String[] {"1", "X"});
		importer.execute(new String[] {"2", "Y"});
		
		List<Map<String, Object>> resultList = getAllRecords(tableName);
		
		assertEquals(before, resultList.size());
		
		for (Map<String, Object> rowData : resultList) {
			if (rowData.get("id").equals(1)) {
				assertEquals("X",  rowData.get("name"));
			}
			if (rowData.get("id").equals(2)) {
				assertEquals("Y",  rowData.get("name"));
			}
		}
		
		importer.close();
	}
	
	/**
	 * InsertとUpdateを交互に
	 * @throws SQLException
	 */
	@Test
	public void testInsertAndUpdate() throws SQLException {
		String tableName = "SAMPLE_TAB1";
		
		int before = getRecordCount(tableName);
		
		ArrayToDbImporter importer =
			new ArrayToDbImporter(getConnection());
		importer.setTableName(tableName);
		importer.setReplace(true);
		importer.initialize();
		
		importer.execute(new String[] {"100", "A"});
		importer.execute(new String[] {"1", "B"});
		importer.execute(new String[] {"101", "C"});
		importer.execute(new String[] {"2", "D"});
		importer.execute(new String[] {"1", "E"});
		
		List<Map<String, Object>> resultList = getAllRecords(tableName);
		
		assertEquals(before + 2, resultList.size());
		
		for (Map<String, Object> rowData : resultList) {
			int id = (Integer) rowData.get("id");
			String name = (String) rowData.get("name");
			
			switch (id) {
			case 1:
				assertEquals("E", name);
				break;
			case 2:
				assertEquals("D", name);
				break;
			case 100:
				assertEquals("A", name);
				break;
			case 101:
				assertEquals("C", name);
				break;
			default:
				
			}
		}
		
		importer.close();
	}
	
	/**
	 * null値を指定してInsert
	 * @throws SQLException
	 */
	@Test
	public void testInsertNullValue() throws SQLException {
		String tableName = "EMP";
		
		int before = getRecordCount(tableName);
		
		ArrayToDbImporter importer =
			new ArrayToDbImporter(getConnection());
		importer.setTableName(tableName);
		importer.setNullString("null");
		importer.initialize();
		
		importer.execute(new String[] {"9999", "null", "null", "null", "null", "null", "null", "null"});
		
		List<Map<String, Object>> resultList = getAllRecords(tableName);
		
		assertEquals(before + 1, resultList.size());
		
		for (Map<String, Object> rowData : resultList) {
			if (!rowData.get("EMPNO").equals(9999)) {
				continue;
			}
			
			assertNull(rowData.get("ENAME"));
			assertNull(rowData.get("JOB"));
			assertNull(rowData.get("MGR"));
			assertNull(rowData.get("HIREDATE"));
			assertNull(rowData.get("SAL"));
			assertNull(rowData.get("COMM"));
			assertNull(rowData.get("DEPTNO"));
		}
	}

}
