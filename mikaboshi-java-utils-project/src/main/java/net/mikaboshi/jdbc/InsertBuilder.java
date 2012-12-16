package net.mikaboshi.jdbc;

import java.io.PrintWriter;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

import net.mikaboshi.validator.SimpleValidator;

import org.apache.commons.lang.StringUtils;

/**
 * ResultSetから、INSERT文字列を生成する。
 * 
 * @author Takuma Umezawa
 *
 */
public class InsertBuilder implements ResultSetHandler {

	/** INSERT文の出力先 */
	private final PrintWriter writer;
	
	/** INSERTするテーブル名 */
	private final String tableName;
	
	/** INSERT文にカラム名を出力するならばtrue */
	private boolean writeColumnName;
	
	/**
	 * 
	 * @param writer INSERT文の出力先。このクラスではcloseを行わない。
	 * @param tableName INSERTするテーブル名
	 * @param isWriteColumnName INSERT文にカラム名を出力するならばtrue
	 */
	public InsertBuilder(
			PrintWriter writer, 
			String tableName, 
			boolean isWriteColumnName) {
		DbUtils.validateTableName(tableName);
		
		this.writer = writer;
		this.tableName = tableName;
		this.writeColumnName = isWriteColumnName;
	}
	
	/**
	 * INSERT文にカラム名を出力するかどうかを取得する。
	 * @return カラム名を出力する場合はtrueを返す。
	 */
	public boolean isWriteColumnName() {
		return this.writeColumnName;
	}

	/**
	 * このクラスでは何も行わない。
	 */
	public void after() {
	}

	private String[] columnNames;
	private int[] columnTypes;
	
	/**
	 * メタデータをチェックし、カラムの情報を取得する。
	 */
	public void before(ResultSetMetaData meta) throws SQLException {
		int size = meta.getColumnCount();
		this.columnNames = new String[size];
		this.columnTypes = new int[size];
		
		for (int i = 0; i < size; i++) {
			this.columnNames[i] = meta.getColumnName(i + 1);
			this.columnTypes[i] = meta.getColumnType(i + 1);
		}
	}

	/**
	 * このクラスでは何も行わない。
	 */
	public void close() {
	}

	private final ResultDataFormatter formatter =
			new SQLStatementFormatter();
	
	/**
	 * ResultSetの１行から１つのINSERT文を生成して書き出す。
	 */
	public void handle(ResultSet rs) throws SQLException {
		
		SimpleValidator.validateNotNull(
				this.writer, "writer", IllegalStateException.class);
		SimpleValidator.validateNotNull(
				this.columnTypes, "columnTypes", IllegalStateException.class);
		
		StringBuilder sb = new StringBuilder();
		sb.append("insert into ");
		sb.append(this.tableName);
		
		if (this.writeColumnName) {
			sb.append(" (");
			sb.append(StringUtils.join(this.columnNames, ", "));
			sb.append(")");
		}
		
		sb.append(" values (");
		
		for (int i = 0; i < this.columnTypes.length; i++) {
			if (i != 0) {
				sb.append(", ");
			}
			sb.append(this.formatter.format(rs, i + 1, this.columnTypes[i]));
		}
		
		sb.append(") ;");

		this.writer.println(sb.toString());
	}

}
