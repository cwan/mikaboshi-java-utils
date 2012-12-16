package net.mikaboshi.jdbc;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * ResultSetの１列目を順次文字列リストに格納する。
 * 
 * @author Takuma Umezawa
 *
 */
public class SingleColumnToListResultSetHandler implements ResultSetHandler {

	private List<String> result = new ArrayList<String>();
	
	public SingleColumnToListResultSetHandler() {
	}

	/**
	 * このクラスでは何も行わない。
	 */
	public void after() throws SQLException {
	}

	/**
	 * このクラスでは何も行わない。
	 */
	public void before(ResultSetMetaData meta) throws SQLException {
	}

	/**
	 * このクラスでは何も行わない。
	 */
	public void close() throws SQLException {
	}

	/* (非 Javadoc)
	 * @see net.mikaboshi.jdbc.ResultSetHandler#handle(java.sql.ResultSet)
	 */
	public void handle(ResultSet rs) throws SQLException {
		Object value = rs.getObject(1);
		if (value != null) {
			this.result.add(value.toString());
		} else {
			this.result.add(null);
		}
	}
	
	/**
	 * {@link #handle(ResultSet)} の実行によってデータが格納されたリストを取得する。
	 * @return
	 */
	public List<String> getList() {
		return this.result;
	}

}
