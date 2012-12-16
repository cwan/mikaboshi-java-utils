package net.mikaboshi.jdbc;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

/**
 * クエリ実行結果のResultSetに対して何らかの処理をするクラスの
 * インターフェース。
 * 
 * @author Takuma Umezawa
 *
 */
public interface ResultSetHandler {
	
	/**
	 * ResultSetに対して処理を行う。
	 * クエリ結果が2件以上の場合は、１行ごとにこのメソッドが呼ばれる。
	 * 
	 * @param rs
	 * @throws SQLException
	 */
	public void handle(ResultSet rs) throws SQLException;
	
	/**
	 * SQL実行後、ResultSetのループに入る前に呼び出されるフックメソッド。
	 */
	public void before(ResultSetMetaData meta) throws SQLException;
	
	/**
	 * ResultSetのループを抜けた後に呼び出されるフックメソッド。
	 * ループ中に例外が発生した場合は呼び出されない。
	 */
	public void after() throws SQLException;
	
	/**
	 * 例外の有無にかかわらず、最後（ResultSetが閉じられる直前）に呼び出される。
	 */
	public void close() throws SQLException;
}
