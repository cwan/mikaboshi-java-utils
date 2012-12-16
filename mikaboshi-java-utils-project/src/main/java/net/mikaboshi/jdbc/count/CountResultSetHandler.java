package net.mikaboshi.jdbc.count;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

import net.mikaboshi.jdbc.ResultSetHandler;

/**
 * クエリ結果の１列目を整数として返す。
 * 
 * @author Takuma Umezawa
 *
 */
public class CountResultSetHandler implements ResultSetHandler {

	private Integer count = null;
	
	public CountResultSetHandler() {
	}
	
	/**
	 * このクラスでは何も行わない。
	 */
	public void after() {
	}

	/**
	 * このクラスでは何も行わない。
	 */
	public void before(final ResultSetMetaData meta) {
	}

	/**
	 * このクラスでは何も行わない。
	 */
	public void close() {
	}

	/* (非 Javadoc)
	 * @see net.mikaboshi.jdbc.ResultSetHandler#handle(java.sql.ResultSet)
	 */
	public void handle(ResultSet rs) throws SQLException {
		this.count = new Integer(rs.getInt(1));
	}
	
	/**
	 * {@link #handle(ResultSet)} の実行によって評価された件数を取得する。
	 * @return
	 */
	public int getCount() {
		if (this.count == null) {
			throw new IllegalStateException("ResultSetが評価されていません");
		}
		
		return this.count.intValue();
	}

}
