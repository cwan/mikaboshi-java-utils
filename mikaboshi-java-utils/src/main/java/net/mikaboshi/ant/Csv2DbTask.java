package net.mikaboshi.ant;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.sql.SQLException;

import net.mikaboshi.csv.CSVStrategy;
import net.mikaboshi.csv.StandardCSVStrategy;
import net.mikaboshi.jdbc.ArrayToDbImporter;
import net.mikaboshi.jdbc.DbUtils;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.tools.ant.BuildException;


/**
 * <p>
 * CSV ファイルの内容を RDB にインポートする Ant タスクを実装する。
 * </p><p>
 * このクラスは同期化されない。
 * </p>
 * 
 * @author Takuma Umezawa
 */
public class Csv2DbTask extends File2DbTask {

	private final M17NTaskLogger logger;
	
	/**
	 * 本タスクのプロパティをデフォルトに設定する。
	 */
	public Csv2DbTask() {
		super();
		
		this.logger = new M17NTaskLogger(
				this, AntConstants.LOG_MESSAGE_BASE_NAME);
	}
	
	private String tableName;
	
	/**
	 * <p>
	 * インポート先のテーブル名を設定する。
	 * </p><p>
	 * file プロパティで CSV ファイルを指定した場合は、この tableName
	 * プロパティでインポート先のテーブルを指定できる。
	 * </p><p>
	 * dir または fileset と、tableName の両方を指定してはならない。
	 * </p>
	 * 
	 * @param tableName インポート先のテーブル名
	 */
	public void setTableName(String tableName) {
		this.tableName = tableName;
	}
	
	/**
	 * <p>
	 * インポート先のテーブル名を取得する。
	 * </p></p>
	 * 複数のファイルをインポートする場合、最初に Ant のプロパティに設定されていた値ではなく、
	 * 次のファイルの内容をインポートする先のテーブル名を返す。
	 * </p>
	 * 
	 * @return インポート先のテーブル名
	 */
	protected String getTableName() {
		return this.tableName;
	}

	private CSVStrategy csvStrategy;
	
	/**
	 * <p>
	 * CSV　ファイルの読み込み方式を決定する　CSVStrategy　クラスを指定する。
	 * 省略した場合は、{@link StandardCSVStrategy} が適用される。
	 * </p>
	 * 
	 * @param className CSVStrategy の実装クラス名（FQCN）
	 * @throws BuildException CSVStrategy の実装クラスのインスタンス生成に失敗した場合
	 */
	public void setCSVStrategy(String className) throws BuildException {
		try {
			this.csvStrategy =
				(CSVStrategy) Class.forName(className).newInstance();
		} catch (Exception e) {
			this.logger.throwBuildException(e,
					"error.create_csvstrategy",
					className);
		}
	}
	
	/**
	 * インポート元のファイルをパースする CSVStrategy の実装クラスのインスタンスを返す。
	 * @return CSVStrategy の実装クラスのインスタンス
	 */
	protected CSVStrategy getCSVStrategy() {
		if (this.csvStrategy == null) {
			this.csvStrategy = new StandardCSVStrategy();
		}
		
		return this.csvStrategy;
	}

	/**
	 * 各ファイルの読み込み前の処理として、インポート先のテーブルを決定する。
	 * @param file インポートするファイル
	 * @throws IllegalArgumentException テーブル名が不正な場合
	 */
	@Override
	protected void doBeforeEach(File file) {
		// テーブル名の決定
		if (getFile() == null || this.tableName == null) {
			this.tableName = FilenameUtils.getBaseName(file.getName());
		}
		
		DbUtils.validateTableName(this.tableName);
	}
	
	/* (非 Javadoc)
	 * @see net.mikaboshi.ant.File2DbTask#executeFile(java.io.File)
	 */
	@Override
	protected void executeFile(File file) 
			throws IOException, SQLException {
		
		truncateIfRequred(getTableName(), getCurrentConnection());
		
		InputStream is = null;
		Reader reader = null;
		
		// INSERTまたはUPDATEに成功した件数
		int insertOrUpdateRowCount = 0;
		
		// 現在読み込み中の行（論理行）
		int lineCount = 0;
		
		ArrayToDbImporter arrayToDbImporter =
			new ArrayToDbImporter(getCurrentConnection());
		arrayToDbImporter.setSchemaName(getSchema());
		arrayToDbImporter.setTableName(getTableName());
		arrayToDbImporter.setReplace(isReplace());
		arrayToDbImporter.setNullString(getNullString());
		arrayToDbImporter.setCaseSensitive(isCaseSensitive());
		
		if (!isExistsHeader()) {
			// CSVの1行目がカラム名ではない場合、テーブル定義のカラム順でImporterを初期化
			arrayToDbImporter.initialize();
		}
		
		try {
			is = FileUtils.openInputStream(file);
			reader = new BufferedReader(new InputStreamReader(is, getCharset()));
			
			for (String[] lines : getCSVStrategy().csvLines(reader)) {
				lineCount++;
				
				try {
					insertOrUpdateRowCount +=
							executeRow(arrayToDbImporter, lines, lineCount);
					
				} catch (SQLException e) {
					if (isHaltOnError()) {
						throw e;
					}
					
					this.logger.warn(e,
							"continue_on_error.logical_line",
							lineCount);
					
					// PostgreSQLの場合、ロールバックが必要
					DbUtils.rollbackQuietly(getCurrentConnection());
				}
			}
			
		} finally {
			this.logger.debug("lines.execute", lineCount);
			this.logger.info("lines.insert_update", insertOrUpdateRowCount);
			
			if (arrayToDbImporter != null) {
				arrayToDbImporter.close();
			}
			
			IOUtils.closeQuietly(reader);
			IOUtils.closeQuietly(is);
		}
	}
	
	/**
	 * １行のインポートを行う。
	 * 
	 * @param arrayToDbImporter
	 * @param rowData
	 * @param lineCount
	 * @return
	 * @throws SQLException
	 */
	private int executeRow(
			ArrayToDbImporter arrayToDbImporter,
			String[] rowData,
			int lineCount)
		throws SQLException {
		
		if (lineCount == 1 && isExistsHeader()) {
			// CSVの1行目がカラム名の場合、カラムの順序を指定してImporterを初期化
			arrayToDbImporter.setColumnNames(rowData);
			arrayToDbImporter.initialize();
			return 0;
		}
		
		return arrayToDbImporter.execute(rowData);
	}
}
