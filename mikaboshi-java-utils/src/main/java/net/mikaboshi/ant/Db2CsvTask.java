package net.mikaboshi.ant;

import static net.mikaboshi.validator.SimpleValidator.validateNotNull;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Set;

import net.mikaboshi.csv.CSVStrategy;
import net.mikaboshi.csv.StandardCSVStrategy;
import net.mikaboshi.jdbc.DbUtils;
import net.mikaboshi.jdbc.QueryExecutor;
import net.mikaboshi.jdbc.ResultSetHandler;
import net.mikaboshi.jdbc.ResultSetToCSVHandler;
import net.mikaboshi.jdbc.schema.SchemaUtils;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.tools.ant.BuildException;

/**
 * <p>
 * データベースの全て（またはパターンに一致する）テーブルをCSVファイルにエクスポートするAntタスク。
 * </p><p>
 * outputDir 属性に指定したディレクトリの下に、「スキーマ名.テーブル名.csv」　というファイルが
 * 生成される。schema 属性を省略した場合は、「テーブル名.csv」となる。
 * </p><p>
 * このクラスは同期化されない。
 * </p>
 * @author Takuma Umezawa
 * @since 1.0.1
 */
public class Db2CsvTask extends Db2FileTask {

	private final M17NTaskLogger logger;
	
	/**
	 * 本タスクのプロパティをデフォルトに設定する。
	 */
	public Db2CsvTask() {
		super();
		
		this.logger = new M17NTaskLogger(
				this, AntConstants.LOG_MESSAGE_BASE_NAME);
	}
	
	private File outputDir;
	
	/**
	 * エクスポートファイルを配置するディレクトリパスを設定する。（必須）
	 * @param path
	 * @throws IOException 
	 */
	public void setOutputDir(String path) throws IOException {
		this.outputDir = new File(path);
		
		if (this.outputDir.exists() && this.outputDir.isDirectory()) {
			return;
		}
		
		FileUtils.forceMkdir(this.outputDir);
	}
	
	private String charset;

	/**
	 * エクスポートファイルの文字セットを設定する。
	 * 省略時は、システムデフォルトが適用される。
	 */	
	public void setCharset(String charset) {
		this.charset = charset;
	}
	
	/**
	 * エクスポートファイルの文字セットを取得する。
	 * @return
	 */
	protected String getCharset() {
		if (this.charset == null) {
			this.charset = Charset.defaultCharset().name();
		}
		
		return this.charset;
	}
	
	private CSVStrategy csvStrategy;
	
	/**
	 * CSVの出力形式を決めるクラス名。
	 * {@link CSVStrategy}の実装クラスのFQCNを指定する。
	 * 省略可。（デフォルトは、{@link StandardCSVStrategy}）
	 * 
	 * @param className
	 * @throws BuildException
	 */
	public void setCSVStrategy(String className) throws BuildException {
		try {
			this.csvStrategy = (CSVStrategy) Class.forName(className).newInstance();
		} catch (Exception e) {
			this.logger.throwBuildException(e,
					"error.create_csvstrategy",
					className);
		}
	}
	
	/**
	 * CSVの出力を行うCSVStrategyのインスタンスを返す。
	 * @return
	 */
	protected CSVStrategy getCSVStrategy() {
		if (this.csvStrategy == null) {
			this.csvStrategy = new StandardCSVStrategy();
		}
		
		return this.csvStrategy;
	}
	
	private PrintWriter writer;
	
	/* (非 Javadoc)
	 * @see org.apache.tools.ant.Task#execute()
	 */
	public void execute() throws BuildException {
		validateNotNull(this.outputDir, "outputDir", BuildException.class);
		
		Connection conn = null;
		
		try {
			conn = getConnection();
			
			Set<String> allTableNames = SchemaUtils.getAllTableNames(
					conn, null, getSchema(), null, null);
			
			for (String tableName : allTableNames) {
				if (!getPatterns().isMatch(tableName)) {
					continue;
				}
				
				if (StringUtils.isNotBlank(getSchema())) {
					tableName = getSchema() + "." + tableName;
				}
				
				File outputFile = new File(this.outputDir, tableName + ".csv");
				
				if (outputFile.exists() && outputFile.isFile()) {
					if (isReplace()) {
						this.logger.info("db2file.overwrite", tableName, outputFile.getAbsolutePath());
					} else {
						this.logger.info("db2file.skip", tableName, outputFile.getAbsolutePath());
						continue;
					}
				}
				
				logger.info("db2file.file_name", outputFile.getAbsolutePath());
				logger.info("db2file.table_name", tableName);
				
				this.writer = new PrintWriter(outputFile, getCharset());
				
				String sql = "select * from " + tableName;
				
				try {
					new QueryExecutor(conn, createHandler()).execute(sql);
					
				} catch (SQLException e) {
					if (isHaltOnError()) {
						throw e;
					}
					
					this.logger.warn(e,
							"db2file.continue_on_error",
							tableName);
					
					// PostgreSQLの場合、ロールバックが必要
					DbUtils.rollbackQuietly(conn);
					
				} finally {
					IOUtils.closeQuietly(this.writer);
				}
			}
			
		} catch (IOException e) {
			throw new BuildException(e);
		} catch (SQLException e) {
			throw new BuildException(e);
		} finally {
			DbUtils.closeQuietly(conn);
			IOUtils.closeQuietly(this.writer);
		}
	}

	protected ResultSetHandler createHandler() throws IOException {
		return new ResultSetToCSVHandler(
					this.writer,
					isHeaderNeeded(),
					false,
					false,
					getFormatter(),
					getCSVStrategy());
	}
}
