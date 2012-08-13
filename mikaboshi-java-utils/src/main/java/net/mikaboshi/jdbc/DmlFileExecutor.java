package net.mikaboshi.jdbc;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.sql.Connection;
import java.sql.SQLException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * <p>
 * ファイルに記述されたDML/DDL文(INSERT/UPDATE/DELETE/CREATE等)を実行する。
 * </p><p>
 * 入力ファイルの仕様：
 * <ul>
 *   <li>入力ファイルには、複数のDML/DDL文を記述できる。
 *   <li>文の最後は、指定した区切り文字で終わらなければならない。
 *   <li>区切り文字の後に改行せずに次の文を記述することはできない。
 * </ul>
 * </p>
 * @author Takuma Umezawa
 */
public class DmlFileExecutor {

	private static Log logger = LogFactory.getLog(DmlFileExecutor.class);
	
	private DmlFileExecutor() {}
	
	/**
	 * DMLファイルを実行する。
	 * 
	 * @param conn 接続済みコネクション
	 * @param file　実行するDMLファイル
	 * @param charset ファイルの文字コード。nullを指定すると、システムのデフォルトが適用される。
	 * @param delimiter DML文の区切り文字
	 * @param haltOnError エラーが発生しらすぐ中断するならばtrue。
	 * 
	 * @throws SQLException
	 * 		DML文の実行でSQLExceptionが発生した場合。
	 * 		ただし、haltOnError="false" の場合は、例外を投げず次のDML文を実行する。
	 * @throws IOException
	 */
	public static void execute(
			Connection conn,
			File file,
			String charset,
			String delimiter,
			boolean haltOnError) 
			throws SQLException, IOException {
		
		DmlExecutor executor = new DmlExecutor(conn, delimiter, haltOnError);
		
		if (logger.isDebugEnabled()) {
			logger.debug("read file to execute: <" + file.getCanonicalPath() + ">");
		}
		
		InputStream is = null;
		Reader reader = null;
		
		try {
			is = FileUtils.openInputStream(file);
			
			if (charset == null) {
				reader = new InputStreamReader(is);
			} else {
				reader = new InputStreamReader(is, charset);
			}
			
			executor.execute(reader);
			
		} finally {
			IOUtils.closeQuietly(reader);
			IOUtils.closeQuietly(is);
		}
	}

}
