package net.mikaboshi.jdbc.schema;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;

import net.mikaboshi.jdbc.DbUtils;
import net.mikaboshi.jdbc.ResultSetHandler;

/**
 * メタ情報を書き出す。
 * 
 * @author Takuma Umezawa
 *
 */
public class MetadataWriter {
	
	private Connection conn;
	private ResultSetHandler resultSetHandler;
	
	/**
	 * 
	 * @param conn
	 * @param resultSetHandler DBメタ情報を出力するハンドラ
	 */
	public MetadataWriter(Connection conn, ResultSetHandler resultSetHandler) {
		this.conn = conn;
		this.resultSetHandler = resultSetHandler;
	}
	
	/**
	 * 指定されたカタログ、スキーマ、テーブル、カラムの情報を書き出す。
	 * 
	 * @see DatabaseMetaData#getColumns(String, String, String, String)
	 * 
	 * @param catalog カタログ名。nullならば全て。
	 * @param schemaPattern スキーマ名のパターン。nullならば全て。
	 * @param tableNamePattern テーブル名のパターン。nullならば全て。
	 * @param columnNamePattern カラム名のパターン。nullならば全て。
	 * @throws SQLException
	 */
	public void doWrite(
			String catalog,
			String schemaPattern,
			String tableNamePattern,
			String columnNamePattern)
			throws SQLException {
		
		DatabaseMetaData dbMeta = this.conn.getMetaData();
		
		ResultSet rs = null;
		
		try {
			rs = dbMeta.getColumns(catalog, schemaPattern, tableNamePattern, columnNamePattern);
			
			this.resultSetHandler.before(rs.getMetaData());
			
			while (rs.next()) {
				this.resultSetHandler.handle(rs);
			}
			
			this.resultSetHandler.after();
			
		} finally {
			this.resultSetHandler.close();
			DbUtils.closeQuietly(rs);
		}
	}
}
