package net.mikaboshi.jdbc;

import java.sql.ResultSet;
import java.sql.SQLException;

/** 
 * ResultSetの1値を文字列に変換する。
 * 
 * @author Takuma Umezawa
 *
 */
public interface ResultDataFormatter {

	/**
	 * 引数の ResultSet の getXxx(columnIndex) メソッドで取得される値を、
	 * 文字列に変換する。
	 * このメソッドでは、 ResultSet#next() メソッドを実行することはない。
	 * 
	 * @param rs データを取得する元の ResultSet
	 * @param columnIndex 引数の　ResultSet における、取得対象カラムの番号
	 * @param columnType {@link java.sql.Types} で定義されるデータ型
	 * @return ResultSet の指定カラムを文字列に変換した値
	 * @throws SQLException
	 */
	public String format(ResultSet rs, int columnIndex, int columnType) 
		throws SQLException;
}
