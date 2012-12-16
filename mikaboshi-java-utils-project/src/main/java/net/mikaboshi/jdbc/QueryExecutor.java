package net.mikaboshi.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Map;

import net.mikaboshi.validator.SimpleValidator;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * クエリSQLを実行する。
 * ResultSetの処理は、{@link ResultSetHandler}に委譲する。
 * 
 * @author Takuma Umezawa
 *
 */
public class QueryExecutor {

	private Connection conn;
	private ResultSetHandler resultSetHandler;
	
	private static Log logger = LogFactory.getLog(QueryExecutor.class);
	
	/**
	 * 
	 * @param connection DBコネクション
	 * @param resultSetHandler クエリ結果を処理するオブジェクト
	 */
	public QueryExecutor(
			Connection connection, 
			ResultSetHandler resultSetHandler) {
		
		SimpleValidator.validateNotNull(
				connection, "connection", NullPointerException.class);
		SimpleValidator.validateNotNull(
				resultSetHandler, "resultSetHandler", NullPointerException.class);
		
		this.conn = connection;
		this.resultSetHandler = resultSetHandler;
	}
	
	/**
	 * 指定されたSQLを実行する。
	 * 
	 * @param argSql
	 * @throws SQLException
	 */
	public void execute(String argSql) throws SQLException {
		
		String sql = StringUtils.chomp(argSql.trim(), ";");
		
		logger.debug("execute: " + sql);
		
		Statement stmt = null;
		ResultSet rs = null;
		
		try {
			stmt = this.conn.createStatement();
			rs = stmt.executeQuery(sql);
			
			this.resultSetHandler.before(rs.getMetaData());
			
			while (rs.next()) {
				this.resultSetHandler.handle(rs);
			}
			
			this.resultSetHandler.after();
			
		} finally {
			this.resultSetHandler.close();
			
			DbUtils.closeQuietly(rs);
			DbUtils.closeQuietly(stmt);
		}
	}
	
	/**
	 * 指定されたSQLを実行する。
	 * 
	 * @param argSql
	 * @param params PreparedStatementのパラメータ
	 * @throws SQLException
	 */
	public void execute(String argSql, Object[] params) throws SQLException {
		
		String sql = StringUtils.chomp(argSql.trim(), ";");
		
		if (logger.isDebugEnabled()) {
			logger.debug("execute: " + sql);
			logger.debug("params : <'" + StringUtils.join(params, "', '") + "'>");
		}
		
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		
		try {
			pstmt = this.conn.prepareStatement(sql);
			
			for (int i = 0; i < params.length; i++) {
				pstmt.setObject(i + 1, params[i]);
			}
			
			rs = pstmt.executeQuery();
			
			this.resultSetHandler.before(rs.getMetaData());
			
			while (rs.next()) {
				this.resultSetHandler.handle(rs);
			}
			
			this.resultSetHandler.after();
			
		} finally {
			this.resultSetHandler.close();
			
			DbUtils.closeQuietly(rs);
			DbUtils.closeQuietly(pstmt);
		}
	}
	
	/**
	 * クエリを実行してリストで返すユーティリティメソッド（パラメータあり）。
	 * @param conn
	 * @param sql SQL文（パラメータは「?」）
	 * @param params
	 * @return
	 * @throws SQLException
	 */
	public static List<Map<String, Object>> query(
			Connection conn, 
			String sql, 
			Object[] params) 
			throws SQLException {
		
		ResultSetToMapListHandler handler = new ResultSetToMapListHandler();
		QueryExecutor executor = new QueryExecutor(conn, handler);
		executor.execute(sql, params);
		return handler.getResultList();
	}

	/**
	 * クエリを実行してリストで返すユーティリティメソッド（パラメータなし）。
	 * @param conn
	 * @param sql SQL文（パラメータなし）
	 * @return
	 * @throws SQLException
	 */
	public static List<Map<String, Object>> query(
			 Connection conn, String sql) throws SQLException {
		ResultSetToMapListHandler handler = new ResultSetToMapListHandler();
		QueryExecutor executor = new QueryExecutor(conn, handler);
		executor.execute(sql);
		return handler.getResultList();
	}
}
