package net.mikaboshi.jdbc;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

import net.mikaboshi.validator.SimpleValidator;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * <p>
 * Readerから読み込んだ文字列に記述されたDML/DDL文
 * (INSERT/UPDATE/DELETE/CREATE等)を実行する。
 * </p><p>
 * 入力仕様：
 * <ul>
 *   <li>複数のDML/DDL文を記述できる。</li>
 *   <li>文の最後は、指定した区切り文字で終わらなければならない。</li>
 *   <li>区切り文字の後に改行せずに次の文を記述することはできない。</li>
 * </ul>
 * </p>
 * 
 * @author Takuma Umezawa
 */
public class DmlExecutor {

	private static Log logger = LogFactory.getLog(DmlExecutor.class);
	
	/** DML文の区切り文字 */
	private final String delimiter;
	
	/** DBコネクション */
	private final Connection conn;
	
	/** SQLException発生時に中断するならばtrue */
	private final boolean haltOnError;
	
	/**
	 * 
	 * @param connection　DBコネクション
	 * @param delimiter　DML文の区切り文字
	 * @param haltOnError　SQLException　発生時に中断するならば　true
	 */
	public DmlExecutor(
			Connection connection,
			String delimiter,
			boolean haltOnError) {
		
		SimpleValidator.validateNotNull(
				connection, "connection", NullPointerException.class);
		
		this.conn = connection;
		this.delimiter = delimiter;
		this.haltOnError = haltOnError;
	}

	/**
	 * Reader からの入力を最初から最後まで読み込み、
	 * 含まれる全てのDML文を実行する。
	 * このメソッドでは、Readerのcloseを行わない。
	 * 
	 * @param reader DML文の取得元。
	 * @throws SQLException
	 * @throws IOException 
	 */
	public void execute(Reader reader) 
			throws SQLException, IOException {
		
		BufferedReader bufReader;
		
		if (reader instanceof BufferedReader) {
			bufReader = (BufferedReader) reader;
		} else {
			bufReader = new BufferedReader(reader);
		}
		
		int lineCount = 0;
		int statementCount = 0;
		int successCount = 0;
		int errorCount = 0;
		int updateCount = 0;
		
		try {
			StringBuilder dmlBuffer = new StringBuilder();
			
			while (true) {
				String line = bufReader.readLine();
				
				if (line == null) {
					break;
				}
				
				lineCount++;
				
				line = line.trim();
				
				if (line.length() == 0) {
					continue ;
				}
				
				dmlBuffer.append(line);
				
				if (!line.endsWith(this.delimiter)) {
					dmlBuffer.append(" ");
					continue;
				}
				
				String dml = dmlBuffer.toString();
				dmlBuffer = new StringBuilder();
				
				dml = StringUtils.chomp(dml, this.delimiter);
				statementCount++;
				
				try {
					updateCount += execute(this.conn, dml);
					successCount++;
					
				} catch (SQLException e) {
					if (this.haltOnError) {
						logger.fatal("エラーが発生しました。処理を中断します。(" + lineCount + "行目)", e);
						throw e;
					}
					
					logger.warn("エラーが発生しましたが、処理を継続します。(" + lineCount + "行目)", e);
					errorCount++;
				}
			}
			
			if (dmlBuffer.toString().length() != 0) {
				logger.warn("末尾に不正な文字列があります: [" + dmlBuffer + "]");
			}

		} finally {
			logger.debug(String.format(
					"result: <read: %d lines, execute: %d statements (success:%d/failure:%d), affected: %d rows>",
					new Integer(lineCount),
					new Integer(statementCount),
					new Integer(successCount),
					new Integer(errorCount),
					new Integer(updateCount)));
		}
	}
	
	/**
	 * １つのDMLステートメントを実行する。
	 * 
	 * @param conn
	 * @param dml
	 * @return
	 * @throws SQLException
	 */
	public static int execute(
			Connection conn, 
			String dml) 
			throws SQLException {
		
		Statement stmt = null;
		
		try {
			stmt = conn.createStatement();
			
			long start = System.currentTimeMillis();
			
			int count = stmt.executeUpdate(dml);
			
			long time = System.currentTimeMillis() - start;
			
			if (logger.isTraceEnabled()) {
				logger.trace(dml + ";    " + count + " rows affected (" + time + " ms)");
			}

			return count;
			
		} finally {
			DbUtils.closeQuietly(stmt);
		}		
	}
	
	/**
	 * PreparedStatementにパラメータを設定して実行する。
	 * このメソッドでは、PreparedStatementのcloseを行わない。
	 * 
	 * @param pstmt
	 * @param params
	 * @return
	 * @throws SQLException
	 */
	public static int execute(
			PreparedStatement pstmt, 
			Object[] params) 
			throws SQLException {
		
		pstmt.clearParameters();
		
		if (logger.isDebugEnabled()) {
			logger.debug("PreparedStatement instance: " + pstmt);
			logger.debug("parameters : <'" + StringUtils.join(params, "', '") + "'>");
		}
		
		int i = 0;
		for (Object param : params) {
			pstmt.setObject(++i, param);
		}
		
		return pstmt.executeUpdate();
	}
	
	/**
	 * 文字列のDML文の「?」にパラメータを設定して実行する。
	 * 
	 * @param conn
	 * @param dml
	 * @param params
	 * @return
	 * @throws SQLException
	 */
	public static int execute(
			Connection conn, 
			String dml, 
			Object[] params) 
			throws SQLException {
		
		logger.debug("execute: " + dml);
		
		PreparedStatement pstmt = null;
		
		try {
			pstmt = conn.prepareStatement(dml);
			return execute(pstmt, params);
			
		} finally {
			DbUtils.closeQuietly(pstmt);
		}
	}
}
