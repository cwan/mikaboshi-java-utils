package net.mikaboshi.jdbc.schema;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.mikaboshi.jdbc.DbUtils;
import net.mikaboshi.validator.SimpleValidator;
import net.mikaboshi.validator.ValidatorException;

import org.apache.commons.lang.ArrayUtils;

/**
 * DBスキーマに関するユーティリティクラス。
 * @author Takuma Umezawa
 *
 */
public final class SchemaUtils {
	
	private SchemaUtils() {}
	
	/**
	 * 全てのテーブルのメタ情報のセットを返す。
	 * 
	 * @see java.sql.DatabaseMetaData#getTables(String, String, String, String[])
	 * 
	 * @param conn
	 * @param catalog カタログ。nullの場合は全て。
	 * @param schemaPattern スキーマ（%、_でワイルドカード）。nullの場合は全て。
	 * @param tableNamePattern テーブル名のパターン（%、_でワイルドカード）。nullの場合は全て。
	 * @param types	 テーブルの型。nullの場合は全て。
	 * 
	 * @return テーブル情報のセット。
	 * 			各テーブル情報は、マップに格納される。	マップのキーは、{@link TableMetaInfo}。
	 * @throws SQLException 
	 */
	public static Set<Map<TableMetaInfo, String>> getAllTables (
			Connection conn,
			String catalog,
			String schemaPattern,
			String tableNamePattern,
			String[] types)
			throws SQLException {
		
		Set<Map<TableMetaInfo, String>> result =
				new HashSet<Map<TableMetaInfo, String>>();
		
		DatabaseMetaData dbMeta = conn.getMetaData();
		
		ResultSet rs = null;
		
		try {
			rs = dbMeta.getTables
					(catalog, schemaPattern, tableNamePattern, types);
			
			while (rs.next()) {
				Map<TableMetaInfo, String> map = 
					new HashMap<TableMetaInfo, String>();
				
				map.put(TableMetaInfo.CATEGORY, rs.getString("TABLE_CAT"));
				map.put(TableMetaInfo.SCHEMA, rs.getString("TABLE_SCHEM"));
				map.put(TableMetaInfo.NAME, rs.getString("TABLE_NAME"));
				map.put(TableMetaInfo.TYPE, rs.getString("TABLE_TYPE"));
				
				result.add(map);
			}
			
			return result;
		} finally {
			DbUtils.closeQuietly(rs);
		}
	}
	
	/**
	 * 全テーブル名のセットを取得する。
	 * 
	 * @param conn
	 * @param catalog カタログ。nullの場合は全て。
	 * @param schemaPattern スキーマ（%、_でワイルドカード）。nullの場合は全て。
	 * @param tableNamePattern テーブル名のパターン（%、_でワイルドカード）。nullの場合は全て。
	 * @param types	 テーブルの型。nullの場合は全て。
	 * @return
	 * @throws SQLException
	 */
	public static Set<String> getAllTableNames(
			Connection conn,
			String catalog,
			String schemaPattern,
			String tableNamePattern,
			String[] types)
			throws SQLException {
		
		Set<Map<TableMetaInfo, String>> allInfo =
			getAllTables(conn, catalog, schemaPattern, tableNamePattern, types);
		
		Set<String> result = new HashSet<String>(allInfo.size());
		
		for (Map<TableMetaInfo, String> elem : allInfo) {
			result.add(elem.get(TableMetaInfo.NAME));
		}

		return result;
	}
	
	/**
	 * カラムの情報を取得する。
	 * テーブル名などは、大文字でないと取得できないDBがある。
	 * 
	 * @param dbMeta
	 * @param catalog
	 * @param schemaPattern
	 * @param tableNamePattern
	 * @param columnNamePattern
	 * @return
	 * @throws SQLException
	 */
	public static List<ColumnInfo> getColumnInfo(
			DatabaseMetaData dbMeta,
			String catalog,
			String schemaPattern,
			String tableNamePattern,
			String columnNamePattern) throws SQLException {
		
		
		List<ColumnInfo> result = new ArrayList<ColumnInfo>();
		
		ResultSet rs = null;
		
		try {
			rs = dbMeta.getColumns(catalog, schemaPattern, tableNamePattern, columnNamePattern);
			
			while (rs != null && rs.next()) {
				ColumnInfo info = new ColumnInfo();
				
				info.setTableCat(rs.getString("TABLE_CAT"));
				info.setTableSchem(rs.getString("TABLE_SCHEM"));
				info.setTableName(rs.getString("TABLE_NAME"));
				info.setColumnName(rs.getString("COLUMN_NAME"));
				info.setDataType(rs.getShort("DATA_TYPE"));
				info.setTypeName(rs.getString("TYPE_NAME"));
				info.setColumnSize(rs.getInt("COLUMN_SIZE"));
				info.setDecimalDigits(rs.getInt("DECIMAL_DIGITS"));
				info.setNumPrecRadix(rs.getInt("NUM_PREC_RADIX"));
				info.setRemarks(rs.getString("REMARKS"));
				info.setColumnDef(rs.getString("COLUMN_DEF"));
				info.setCharOctetLength(rs.getInt("CHAR_OCTET_LENGTH"));
				info.setOrdinalPosition(rs.getInt("ORDINAL_POSITION"));
				
				final String isNullable = rs.getString("IS_NULLABLE");

				if ("NO".equals(isNullable)) {
					info.setNullable(new Boolean(false));
				} else if ("YES".equals(isNullable)) {
					info.setNullable(new Boolean(true));
				} else {
					// NULL（不明）の場合がある
					info.setNullable(null);
				}
				
				if (info.getDataType() == Types.REF) {
					info.setScopeCatlog(rs.getString("SCOPE_CATLOG"));
					info.setScopeSchema(rs.getString("SCOPE_SCHEMA"));
					info.setScopeTable("SCOPE_TABLE");
				}
				
				if (info.getDataType() == Types.DISTINCT || info.getDataType() == Types.REF) {
					info.setSourceDataType(rs.getShort("SOURCE_DATA_TYPE"));
				}
				
				result.add(info);
			}
			
		} finally {
			DbUtils.closeQuietly(rs);
		}
		
		// TABLE_CAT, TABLE_SCHEM, TABLE_NAME ORDINAL_POSITIONの昇順でソート
		Collections.sort(result);
		
		return result;
	}
	

	/**
	 * テーブルの主キーを取得する。
	 * @param dbMeta
	 * @param catalog
	 * @param schema
	 * @param table
	 * @return
	 * @throws SQLException
	 */
	public static Set<PrimaryKeyInfo> getPrimaryKeys(
			DatabaseMetaData dbMeta,
			String catalog,
			String schema,
			String table) throws SQLException {
		
		ResultSet rs = null;
		
		try {
			rs = dbMeta.getPrimaryKeys(catalog, schema, table);
			
			final Map<String, PrimaryKeyInfo> map = new HashMap<String, PrimaryKeyInfo>();
			
			while (rs.next()) {
				String tableCat = rs.getString("TABLE_CAT");
				String tableSchem = rs.getString("TABLE_SCHEM");
				String tableName = rs.getString("TABLE_NAME");
				String columnName = rs.getString("COLUMN_NAME");
				int keySeq = rs.getShort("KEY_SEQ");
				String pkName = rs.getString("PK_NAME");
				
				final String key = "" + tableCat + ":" + tableSchem + ":" + tableName;
				
				PrimaryKeyInfo pk = map.get(key);
				
				if (pk == null) {
					pk = new PrimaryKeyInfo();
					pk.setTableCat(tableCat);
					pk.setTableSchem(tableSchem);
					pk.setTableName(tableName);
					pk.setPkName(pkName);
					map.put(key, pk);
				}
				
				pk.addColumnName(keySeq, columnName);
			}
			
			return new HashSet<PrimaryKeyInfo>(map.values());
			
		} finally {
			DbUtils.closeQuietly(rs);
		}
	}
	
	/**
	 * テーブルが存在するか判定する。
	 * 
	 * @param dbMeta
	 * @param catalog
	 * @param schemaPattern
	 * @param tableNamePattern
	 * @return
	 * @throws SQLException
	 */
	public static boolean existsTable(
		DatabaseMetaData dbMeta,
		String catalog,
		String schemaPattern,
		String tableNamePattern) throws SQLException {
		
		List<ColumnInfo> columnInfoList = 
			getColumnInfo(dbMeta, catalog, schemaPattern, tableNamePattern, null);
		
		if (columnInfoList == null || columnInfoList.isEmpty()) {
			return false;
		} else {
			return true;
		}
	}

	/**
	 * 主キー情報込みで、１つのテーブルのカラム情報を取得する。
	 * 
	 * @param dbMeta
	 * @param catalog
	 * 			カタログ名。nullの場合は指定無し。
	 * @param schemaName
	 * 			スキーマ名。パターン不可。nullの場合は指定無し。
	 * 			tableNameと合わせて、テーブルが特定できなければならない。
	 * @param tableName
	 * 			テーブル名。パターン不可。null不可。
	 * 			schemaNameと合わせて、テーブルが特定できなければならない。		
	 * @return
	 * @throws SQLException
	 */
	public static List<ColumnInfo> getColumnInfoWithPK(
			DatabaseMetaData dbMeta,
			String catalog,
			String schemaName,
			String tableName) throws SQLException {
		
		SimpleValidator.validateNotNull(tableName, "テーブル名", NullPointerException.class);
		
		Set<PrimaryKeyInfo> pkInfoSet = getPrimaryKeys(dbMeta, catalog, schemaName, tableName);
		
		if (pkInfoSet.size() != 1) {
			throw new ValidatorException("テーブルの主キーが特定できません。");
		}
		
		String[] pkColumnNames = null;

		for (PrimaryKeyInfo pkInfo : pkInfoSet) {
			pkColumnNames = pkInfo.getColumnNames();
		}
		
		List<ColumnInfo> columnInfoList = 
			getColumnInfo(dbMeta, catalog, schemaName, tableName, null);
		
		int pkOrder = 0;
		
		for (ColumnInfo columnInfo : columnInfoList) {
			if (ArrayUtils.contains(pkColumnNames, columnInfo.getColumnName())) {
				columnInfo.setPrimaryKeyOrder(++pkOrder);
			}
		}
		
		return columnInfoList;
	}
	
	
}
