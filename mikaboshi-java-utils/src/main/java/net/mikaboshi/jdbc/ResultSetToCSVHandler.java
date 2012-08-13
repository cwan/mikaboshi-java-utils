package net.mikaboshi.jdbc;

import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.List;

import net.mikaboshi.csv.CSVStrategy;
import net.mikaboshi.validator.SimpleValidator;

/**
 * ResultSet から CSV を出力する。
 * 
 * @author Takuma Umezawa
 *
 */
public class ResultSetToCSVHandler extends ResultSetToFileHandler {

	/** CSVの出力先 */
	private PrintWriter writer;
	
	private CSVStrategy csvStrategy;
	
	/**
	 * 出力内容を指定するコンストラクタ。
	 * 
	 * @param writer CSVの出力先
	 * @param outputColumnName 列名を出力するかどうか
	 * @param outputMetaInfo 列のメタ情報を出力するかどうか
	 * @param outputRowNumber 行番号を出力するかどうか
	 * @param formatter 値の文字列整形オブジェクト
	 * @param csvStrategy CSVの整形オブジェクト
	 */
	public ResultSetToCSVHandler(
			PrintWriter writer,
			boolean outputColumnName,
			boolean outputMetaInfo,
			boolean outputRowNumber,
			ResultDataFormatter formatter,
			CSVStrategy csvStrategy) {

		super(outputColumnName, outputMetaInfo, outputRowNumber, formatter);
		
		SimpleValidator.validateNotNull(
				writer, "writer", NullPointerException.class);
		SimpleValidator.validateNotNull(
				csvStrategy, "csvStrategy", NullPointerException.class);
		
		this.writer = writer;
		this.csvStrategy = csvStrategy;
	}
	
	/**
	 * コンストラクタで指定された writer をフラッシュする。（close は行わない）
	 */
	public void close() throws SQLException {
		this.writer.flush();
	}

	/**
	 * コンストラクタで指定された CSVStrategy を使って1行をCSVに出力する。
	 */
	@Override
	protected void println(List<String> line) {
		this.csvStrategy.printLine(line.toArray(), this.writer);
	}

}
