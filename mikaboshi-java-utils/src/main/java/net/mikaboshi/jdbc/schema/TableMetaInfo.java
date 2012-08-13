package net.mikaboshi.jdbc.schema;

/**
 * テーブルのメタ情報を種別。
 * 
 * @see SchemaUtils#getAllTables(java.sql.Connection, String, String, String, String[])

 * @author Takuma Umezawa
 */
public enum TableMetaInfo {
	CATEGORY,
	SCHEMA,
	NAME,
	TYPE
}
