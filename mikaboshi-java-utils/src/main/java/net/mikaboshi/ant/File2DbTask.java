package net.mikaboshi.ant;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.mikaboshi.jdbc.DbUtils;
import net.mikaboshi.jdbc.DmlExecutor;
import net.mikaboshi.validator.SimpleValidator;

import org.apache.commons.lang.StringUtils;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.taskdefs.JDBCTask;
import org.apache.tools.ant.types.FileSet;

/**
 * <p>
 * ファイルを読み込み、DBに対して操作を行うAntタスクの抽象クラス。
 * </p><p>
 * 読み込むファイルは、file属性、dir属性、fileset要素のどれか1つで
 * 指定する。（複数指定した場合はエラーとする）
 * <p>
 * 
 * @author Takuma Umezawa
 */
public abstract class File2DbTask extends JDBCTask {

	private final M17NTaskLogger logger;
	
	/**
	 * 本タスクのプロパティをデフォルトに設定する。
	 */
	public File2DbTask() {
		super();
		
		this.logger = new M17NTaskLogger(
				this, AntConstants.LOG_MESSAGE_BASE_NAME);
	}
	
	private File file;
	
	/**
	 * 読み込み対象のファイルパスを設定する。
	 * @param path
	 */
	public void setFile(String path) {
		this.file = new File(path);
	}
	
	/**
	 * 読み込み対象のファイルパスを取得する。
	 * @return
	 */
	protected File getFile() {
		return this.file;
	}

	private File dir;
	
	/**
	 * 読み込み対象のファイルがあるディレクトリのパスを設定する。
	 * 直下にある全てのファイルが対象となる。
	 */
	public void setDir(String path) {
		this.dir = new File(path);
	}
	
	/**
	 * 読み込み対象のファイルがあるディレクトリのパスを取得する。
	 * @return
	 */
	protected File getDir() {
		return this.dir;
	}

	private List<FileSet> filesets;
	
	/**
	 * 読み込み対象のファイルセットを追加する。
	 */
	public void addFileset(FileSet fileset) {
		if (this.filesets == null) {
			this.filesets = new ArrayList<FileSet>();
		}
		
		this.filesets.add(fileset);
	}
	
	/**
	 * 読み込み対象のファイルセットのリストを取得する。
	 * @return
	 */
	protected List<FileSet> getFileSets() {
		return this.filesets;
	}
	
	private String charset;

	/**
	 * 読み込み対象のファイルの文字セットを設定する。
	 * 省略時は、システムデフォルトが適用される。
	 */	
	public void setCharset(String charset) {
		this.charset = charset;
	}
	
	/**
	 * 読み込み対象のファイルの文字セットを取得する。
	 * @return
	 */
	protected String getCharset() {
		if (this.charset == null) {
			this.charset = Charset.defaultCharset().name();
		}
		
		return this.charset;
	}
	
	private String nullString = StringUtils.EMPTY;
	
	/**
	 * DBカラムにnullを設定する場合の文字列を指定する。（省略可）
	 * 省略した場合、ファイルから読み込んだ項目が空文字の場合は空文字をカラムに設定する。
	 * 
	 * @param nullString
	 */
	public void setNullString(String nullString) {
		this.nullString = nullString;
	}
	
	/**
	 * DBカラムにnullを設定する場合の文字列を取得する。
	 * @return
	 */
	protected String getNullString() {
		return this.nullString;
	}
	
	/**
	 * file属性、dir属性、fileset要素を解析し、読み込み対象の全ファイルを返す。
	 * 
	 * @return
	 * @throws BuildException
	 */
	protected Set<File> getFiles() throws BuildException {
		Map<String, Object> fileParamMap = new HashMap<String, Object>();
		fileParamMap.put("file", this.file);
		fileParamMap.put("dir", this.dir);
		fileParamMap.put("fileset", this.filesets);
		
		SimpleValidator.validateNotNullJust1(fileParamMap, BuildException.class);
		
		if (this.file != null && (!this.file.exists() || !this.file.isFile())) {
			this.logger.throwBuildException(
					"error.file_not_exist",
					this.file.getAbsolutePath());
		}
		
		if (this.dir != null && (!this.dir.exists() || !this.dir.isDirectory())) {
			this.logger.throwBuildException(
					"error.dir_not_exist",
					this.dir.getAbsolutePath());
		}
		
		return TaskUtils.getFileSet(this.file, this.dir, this.filesets, false);
	}
	
	private boolean haltOnError = true;
	
	/**
	 * <p>
	 * エラー（SQLException）発生時に、ロールバックして処理を中断するかどうかを指定する。
	 * </p><p>
	 * 中断する場合はtrue。
	 * 次の論理行から続ける場合はfalse。
	 * （省略化。デフォルトはtrue。）
	 * </p><p>
	 * 対象ファイルが複数あって、trueが指定された場合、残りのファイルは
	 * 読み込まれない。
	 * </p><p>
	 * autoCommit=trueかつhaltOnError=trueが指定された場合、
	 * ロールバックは行われず、読み込まれた行は有効となり、エラーが発生した時点で中断される。
	 * </p>
	 * @param haltOnError
	 */
	public void setHaltonerror(boolean haltOnError) {
		this.haltOnError = haltOnError;
	}
	
	/**
	 * エラー（SQLException）発生時に、ロールバックして処理を中断するかどうかを取得する。
	 * @return
	 */
	protected boolean isHaltOnError() {
		return this.haltOnError;
	}
	
	private String schema;
	
	/**
	 * 操作対象のスキーマを指定する。（省略可）
	 * 
	 * @param schema
	 */
	public void setSchema(String schema) {
		this.schema = schema;
	}
	
	/**
	 * 操作対象のスキーマを取得する。
	 * 無指定の場合はnullが返る。
	 * @return
	 */
	protected String getSchema() {
		return this.schema;
	}
	
	private boolean truncate = false;
	
	/**
	 * 最初にテーブルの全レコードを削除してからインポートする場合はtrue。
	 * （省略可。デフォルトはfalse）
	 * 
	 * @param truncate
	 */
	public void setTruncate(boolean truncate) {
		this.truncate = truncate;
	}
	
	private boolean replace = false;
	
	/**
	 * <p>
	 * 主キーが一致するレコードがある場合にUPDATEを行うかどうかを設定する。
	 * </p><p>
	 * 主キーが一致するレコードがある場合に、INSERTではなくUPDATEを実行する場合は
	 * trueを設定する。（省略可。デフォルトはfalse）
	 * </p><p>
	 * falseの場合、主キーが一致するレコードがある場合は、
	 * 例外（SQLException）がスローされる。
	 * </p>
	 * @param replace
	 */
	public void setReplace(boolean replace) {
		this.replace = replace;
	}
	
	/**
	 * 主キーが一致するレコードがある場合にUPDATEを行うかどうかを取得する。
	 * @return
	 */
	protected boolean isReplace() {
		return this.replace;
	}
	
	private boolean existsHeader = false;
	
	/**
	 * <p>
	 * CSVファイルの1行目がカラム名かどうかを設定する。
	 *　1行目がカラム名ならばtrue （省略可。デフォルトはfalse）
	 * </p><p>
	 * 1行目がカラム名ではない場合は、列の順番でINSERTする。
	 * </p>
	 * @param existsHeader
	 */
	public void setExistsHeader(boolean existsHeader) {
		this.existsHeader = existsHeader;
	}
	
	/**
	 * CSVファイルの1行目がカラム名かどうかを取得する。
	 * @return
	 */
	protected boolean isExistsHeader() {
		return this.existsHeader;
	}
	
	private Connection currentConnection;
	
	/**
	 * 現在使用しているDBコネクションを設定する。
	 * @param conn
	 */
	protected void setCurrentConnection(Connection conn) {
		this.currentConnection = conn;
	}
	
	/**
	 * 現在使用しているDBコネクションを取得する。
	 * @return
	 */
	protected Connection getCurrentConnection() {
		return this.currentConnection;
	}
	
	private boolean caseSensitive = false;
	
	/**
	 * <p>
	 * インポート先のテーブル名およびカラム名の大文字/小文字を厳密にするかどうかを設定する。
	 * </p><p>
	 * trueならば大文字/小文字を区別する。falseならば区別しない。
	 * デフォルトは、false（区別しない）
	 * </p>
	 * @param caseSensitive
	 * @since 1.0.1
	 */
	public void setCaseSensitive(boolean caseSensitive) {
		this.caseSensitive = caseSensitive;
	}
	
	/**
	 * インポート先のテーブル名およびカラム名の大文字/小文字を厳密にするかどうか。
	 * trueならば大文字/小文字を区別する。falseならば区別しない。
	 */
	protected boolean isCaseSensitive() {
		return this.caseSensitive;
	}
	
	/**
	 * ファイルを１つずつ読み込み、処理を行う。
	 */
	@Override
	public void execute() throws BuildException {
		
		Connection conn = null;
		
		Set<File> files = getFiles();
		File current = null;
		
		try {
			conn = getConnection();
			setCurrentConnection(conn);
			
			doBefore();
			
			for (File f : files) {
				current = f;
				this.logger.info("file.import_target", f.getAbsolutePath());
				
				doBeforeEach(f);
				executeFile(f);
				doBeforeEach(f);
			}
			
			doAfter();
			
		} catch (Exception e) {
			doOnError();
			DbUtils.rollbackQuietly(conn);
			
			String path = current != null ? current.getAbsolutePath() : "";
			this.logger.error(e, "error.transport_from_file_to_db", path);
			
			throw new BuildException(e);
		} finally {
			DbUtils.closeQuietly(conn);
		}
	}
	
	/**
	 * 実行前（コネクション生成直後）に呼び出されるフックメソッド。
	 * このクラスでは何も行わない。必要に応じてサブクラスでオーバーライドする。
	 */
	protected void doBefore() {
	}

	/**
	 * 実行後（コミット直前）に呼び出されるフックメソッド。
	 * エラーがあった場合は実行されない。
	 * このクラスでは何も行わない。必要に応じてサブクラスでオーバーライドする。
	 */
	protected void doAfter() {
	}
	
	/**
	 * 各ファイルの読み込み前に呼び出されるフックメソッド。
	 * このクラスでは何も行わない。必要に応じてサブクラスでオーバーライドする。
	 * @param file 
	 * @throws IOException
	 */
	protected void doBeforeEach(File file) throws IOException {
	}
	
	/**
	 * 各ファイルについて、インポート処理を行う。
	 * @param file インポート元のファイル
	 * @throws IOException ファイルの読み込みに失敗した場合
	 * @throws SQLException RDBへのインポートにおいてSQLのエラーが発生した場合
	 */
	protected abstract void executeFile(File file)
			throws IOException, SQLException;
	
	/**
	 * 各ファイルの読み込み後に呼び出されるフックメソッド。
	 * このクラスでは何も行わない。必要に応じてサブクラスでオーバーライドする。
	 */
	protected void doAfterEach(File f) throws IOException {
	}

	/**
	 * エラーがあった場合。（ロールバックする前）に呼び出されるフックメソッド。
	 * このクラスでは何も行わない。必要に応じてサブクラスでオーバーライドする。
	 */
	protected void doOnError() {
	}

	/**
	 * truncate属性が指定されているならば、テーブルの全レコードを削除する。
	 * @param table
	 * @param conn
	 * @throws SQLException
	 */
	protected void truncateIfRequred(String table, Connection conn) throws SQLException {
		if (!this.truncate) {
			return;
		}
		
		try {
			String sql = "truncate table " + table;
			DmlExecutor.execute(conn, sql);
			this.logger.debug("sql.execute", sql);
			
		} catch (SQLException e) {
			// truncateが使えないDBがある
			String sql = "delete from " + table;
			DmlExecutor.execute(conn, sql);
			this.logger.debug("sql.execute", sql);
		}
	}
	
}
