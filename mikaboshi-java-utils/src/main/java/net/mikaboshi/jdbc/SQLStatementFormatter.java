package net.mikaboshi.jdbc;

import java.sql.ResultSet;
import java.sql.SQLException;

import net.mikaboshi.jdbc.schema.DataTypeUtils;

/**
 * 
 * SQL表記文字列に変換するする。 
 * 例えば、CHAR型ならばシングルクォートで囲った文字列を返す。
 * 
 * @author Takuma Umezawa
 * 
 */
public class SQLStatementFormatter implements ResultDataFormatter {

	/**
	 * @param rs
	 * @param columnIndex　ResultSetの何列目か (>=1)
	 * @param columnType　java.sql.Typesの値
	 * @return
	 * @throws SQLException
	 */
	public String format(ResultSet rs, int columnIndex, int columnType)
			throws SQLException {
		
		return DataTypeUtils.formatSqlStatementRiteral(
				rs.getObject(columnIndex));
	}

}
