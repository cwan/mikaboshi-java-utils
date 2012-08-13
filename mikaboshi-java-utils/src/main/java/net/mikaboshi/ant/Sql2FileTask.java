package net.mikaboshi.ant;

import static net.mikaboshi.validator.SimpleValidator.validateNotBlank;
import static net.mikaboshi.validator.SimpleValidator.validateNotNull;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.sql.Connection;
import java.sql.SQLException;

import net.mikaboshi.jdbc.DbUtils;
import net.mikaboshi.jdbc.QueryExecutor;
import net.mikaboshi.jdbc.ResultDataFormatter;
import net.mikaboshi.jdbc.ResultSetHandler;
import net.mikaboshi.jdbc.SimpleFormatter;

import org.apache.commons.lang.StringUtils;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.taskdefs.JDBCTask;

/**
 * <p>
 * SQLで与えられたクエリ結果をファイルに出力するAntタスク抽象クラス。
 * </p><p>
 * このクラスは同期化されない。
 * </p>
 * @author Takuma Umezawa
 */
public abstract class Sql2FileTask extends JDBCTask {

	private final M17NTaskLogger logger;
	
	/**
	 * 本タスクのプロパティをデフォルトに設定する。
	 */
	public Sql2FileTask() {
		super();
		
		this.logger = new M17NTaskLogger(
				this, AntConstants.LOG_MESSAGE_BASE_NAME);
	}
	
	private File outputFile;
	
	/**
	 * 出力するファイルのパスを設定する。（必須）
	 * @param path
	 */
	public void setOutput(String path) {
		this.outputFile = new File(path);
		
		if (this.outputFile.isDirectory()) {
			this.logger.throwBuildException(
					"error.must_be_file", "output");
		}
	}
	
	/**
	 * 出力するファイルのパスを取得する。
	 * @return
	 */
	protected File getOutputFile() {
		return this.outputFile;
	}
	
	private String sql;
	
	/**
	 * 実行するSELECT SQL文を設定する。（必須）
	 * １ステートメントのみ指定できる（全体を1ステートメントと解釈して実行する）。
	 * 末尾の「;」はあってもなくてもよい。
	 * @param text
	 */
	public void addText(String text) {
		if (text == null) {
			return;
		}
		
		this.sql = StringUtils.chomp(text.trim(), ";");
	}
	
	/**
	 * 実行するSELECT SQL文を取得する。
	 * @return
	 */
	protected String getSql() {
		return this.sql;
	}
	
	private boolean header = true;
	
	/**
	 * ファイルにカラム名を出力するかどうかを設定する。
	 * 省略可。（デフォルトはtrue：出力する）
	 * カラム名を出力する場合、SELECT文に指定された列名が使われる。
	 * 
	 * @param header
	 */
	public void setHeader(boolean header) {
		this.header = header;
	}
	
	/**
	 * ファイルにカラム名を出力するかどうかを取得する。
	 * @return
	 */
	protected boolean isHeaderNeeded() {
		return this.header;
	}
	
	private String nullString = StringUtils.EMPTY;
	
	/**
	 * nullの場合の出力文字を設定する。
	 * 省略時は、空文字が出力される。
	 * 
	 * @param nullString
	 */
	public void setNullString(String nullString) {
		this.nullString = nullString;
	}
	
	/**
	 * nullの場合の出力文字を取得する。
	 * @return
	 */
	protected String getNullString() {
		return this.nullString;
	}
	
	private String charset;

	/**
	 * 出力ファイルの文字セットを設定する。
	 * 省略時は、システムデフォルトが適用される。
	 */	
	public void setCharset(String charset) {
		this.charset = charset;
	}
	
	/**
	 * 出力ファイルの文字セットを取得する。
	 * @return
	 */
	protected String getCharset() {
		if (this.charset == null) {
			this.charset = Charset.defaultCharset().name();
		}
		
		return this.charset;
	}
	
	/**
	 * ResultSetの文字列表現を行うオブジェクトを取得する。
	 * @return
	 */
	protected ResultDataFormatter getFormatter() {
		SimpleFormatter formatter = new SimpleFormatter();
		if (getNullString() != null) {
			formatter.setNullString(getNullString());
		}
		
		return formatter;
	}
	
	/* (非 Javadoc)
	 * @see org.apache.tools.ant.Task#execute()
	 */
	@Override
	public void execute() throws BuildException {
		validateNotNull(getOutputFile(), "output", BuildException.class);
		validateNotBlank(getSql(), "SQL", BuildException.class);
		
		Connection conn = null;
		
		try {
			conn = getConnection();
			
			this.logger.info("file.output", getOutputFile().getAbsolutePath());
			
			new QueryExecutor(conn, createHandler()).execute(getSql());
			
		} catch (IOException e) {
			throw new BuildException(e);
		} catch (SQLException e) {
			throw new BuildException(e);
		} finally {
			DbUtils.closeQuietly(conn);
		}
	}
	
	/**
	 * ResultSetをファイルに書き出すHandlerインスタンスを生成する。
	 * 
	 * @return
	 * @throws IOException
	 */
	protected abstract ResultSetHandler createHandler() throws IOException;
}
