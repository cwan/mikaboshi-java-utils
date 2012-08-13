package net.mikaboshi.ant;

import java.io.IOException;
import java.io.PrintWriter;

import net.mikaboshi.csv.CSVStrategy;
import net.mikaboshi.csv.StandardCSVStrategy;
import net.mikaboshi.jdbc.ResultSetHandler;
import net.mikaboshi.jdbc.ResultSetToCSVHandler;

import org.apache.commons.io.IOUtils;
import org.apache.tools.ant.BuildException;

/**
 * <p>
 * SQLで与えられたクエリ結果をCSVファイルに出力するAntタスク。
 * </p><p>
 * このクラスは同期化されない。
 * </p>
 * @author Takuma Umezawa
 */
public class Sql2CsvTask extends Sql2FileTask {

	private final M17NTaskLogger logger;
	
	/**
	 * 本タスクのプロパティをデフォルトに設定する。
	 */
	public Sql2CsvTask() {
		super();
		
		this.logger = new M17NTaskLogger(
				this, AntConstants.LOG_MESSAGE_BASE_NAME);
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
	 * @see net.mikaboshi.ant.Sql2FileTask#execute()
	 */
	@Override
	public void execute() throws BuildException {
		try {
			this.writer = new PrintWriter(getOutputFile(), getCharset());
			super.execute();

		} catch (IOException e) {
			throw new BuildException(e);
		} finally {
			IOUtils.closeQuietly(this.writer);
		}
	}

	/* (非 Javadoc)
	 * @see net.mikaboshi.ant.Sql2FileTask#createHandler()
	 */
	@Override
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
