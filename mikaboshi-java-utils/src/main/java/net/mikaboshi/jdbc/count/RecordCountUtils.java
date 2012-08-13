package net.mikaboshi.jdbc.count;



import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.Writer;
import java.sql.Connection;
import java.sql.SQLException;

import net.mikaboshi.jdbc.DbUtils;
import net.mikaboshi.jdbc.QueryExecutor;

import org.apache.commons.lang.StringUtils;

/**
 * レコード件数に関するユーティリティクラス。
 * 
 * @author Takuma Umezawa
 */
public final class RecordCountUtils {

	private RecordCountUtils() {}
	
	/**
	 * 指定されたテーブルの全レコード件数を取得する。
	 * 
	 * @param conn
	 * @param tableName
	 * @return
	 * @throws SQLException
	 */
	public static int getAllRecordCount(
			Connection conn, String tableName) throws SQLException {
		
		DbUtils.validateTableName(tableName);
		
		final CountResultSetHandler handler = new CountResultSetHandler();
		final QueryExecutor executor = new QueryExecutor(conn, handler);
		executor.execute("select count(*) from " + tableName);
		return handler.getCount();
	}
	
	/**
	 * SELECT COUNT(*) ～ のSQLを実行し、結果を出力する。
	 * 
	 * @param input
	 * @param output
	 * @param conn
	 * @throws IOException
	 * @throws SQLException
	 */
	public static void createRecord(
			Reader input, 
			Writer output, 
			Connection conn) 
			throws IOException, SQLException {
		
		BufferedReader reader;
		
		if (input instanceof BufferedReader) {
			reader = (BufferedReader) input;
		} else {
			reader = new BufferedReader(input);
		}

		PrintWriter writer;
		
		if (output instanceof PrintWriter) {
			writer = (PrintWriter) output;
		} else {
			writer = new PrintWriter(output);
		}
		
		CountResultSetHandler countResultSetHandler 
				= new CountResultSetHandler();
		QueryExecutor countExecutor 
				= new QueryExecutor(conn, countResultSetHandler);
		
		while (true) {
			String line = reader.readLine();
			
			if (line == null) {
				break;
			}
			
			if (StringUtils.isBlank(line)) {
				// 空行の場合は、空行を出力する
				writer.println("");
				continue;
			}
			
			countExecutor.execute(line);
			
			writer.println(String.valueOf(countResultSetHandler.getCount()));
		}
	}
}
