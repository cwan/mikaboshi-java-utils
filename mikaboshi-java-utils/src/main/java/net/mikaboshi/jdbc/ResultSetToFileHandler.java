package net.mikaboshi.jdbc;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import net.mikaboshi.validator.SimpleValidator;

/**
 * ResultSetからファイルに出力する抽象クラス。
 * 
 * @author Takuma Umezawa
 *
 */
public abstract class ResultSetToFileHandler implements ResultSetHandler {

	/** 列名を出力するかどうか */
	private boolean outputColumnName;
	
	/** 列のメタ情報を出力するかどうか */
	private boolean outputMetaInfo;
	
	/** 行番号を出力するかどうか */
	private boolean outputRowNumber;
	
	private ResultDataFormatter formatter;
	
	/**
	 * 出力内容を指定するコンストラクタ。
	 * 
	 * @param outputColumnName 列名を出力するかどうか
	 * @param outputMetaInfo 列のメタ情報を出力するかどうか
	 * @param outputRowNumber 行番号を出力するかどうか
	 * @param formatter 値の文字列整形オブジェクト
	 */
	public ResultSetToFileHandler(
			boolean outputColumnName,
			boolean outputMetaInfo,
			boolean outputRowNumber,
			ResultDataFormatter formatter) {
		
		SimpleValidator.validateNotNull(
				formatter, "formatter", NullPointerException.class);
		
		this.outputColumnName = outputColumnName;
		this.outputMetaInfo = outputMetaInfo;
		this.outputRowNumber = outputRowNumber;
		this.formatter = formatter;
	}

	private int rowCount;

	/**
	 * カラムのメタ情報を出力する。
	 * 
	 * TODO メタ情報出力値のM17N
	 */
	public void before(ResultSetMetaData meta) throws SQLException {
		this.rowCount = 0;
		
		int columnCount = meta.getColumnCount();
		
		// 列名
		if (this.outputColumnName) {
			List<String> line = new ArrayList<String>();
			
			if (this.outputRowNumber) {
				line.add("列名");
			}
			
			for (int i = 1; i <= columnCount; i++) {
				line.add(meta.getColumnName(i));
			}
			
			println(line);
		}
		
		if (!this.outputMetaInfo) {
			return;
		}
		
		// 列のタイプ
		{
			List<String> line = new ArrayList<String>();
			
			if (this.outputRowNumber) {
				line.add("タイプ(桁)");
			}
			
			for (int i = 1; i <= columnCount; i++) {
				StringBuilder sb = new StringBuilder();
				sb.append(meta.getColumnTypeName(i));
				sb.append("(");
				sb.append(meta.getColumnDisplaySize(i));

				int scale = meta.getScale(i);
				
				if (scale != 0) {
					sb.append(".");
					sb.append(scale);
				}
				
				sb.append(")");
				
				line.add(sb.toString());
			}
			
			println(line);
		}

		// NULL可否
		{
			List<String> line = new ArrayList<String>();
			
			if (this.outputRowNumber) {
				line.add("NULL可否");
			}
			
			for (int i = 1; i <= columnCount; i++) {
				int nullable = meta.isNullable(i);

				if (nullable == ResultSetMetaData.columnNoNulls) {
					line.add("NOT_NULL");
				} else if (nullable == ResultSetMetaData.columnNullable) {
					line.add("NULLABLE");
				} else {
					line.add("UNKNOWN");
				}
			}
			
			println(line);
		}
	}
	
	/**
	 * このクラスでは何も行わない。
	 */
	public void after() throws SQLException {
	}
	
	/**
	 * ResultSetの内容を出力する。
	 */
	public void handle(ResultSet rs) throws SQLException {
		this.rowCount++;
		
		List<String> line = new ArrayList<String>();
		
		if (this.outputRowNumber) {
			line.add(String.valueOf(this.rowCount));
		}
		
		ResultSetMetaData meta = rs.getMetaData();
		int columnCount = meta.getColumnCount();
		
		for (int i = 1; i <= columnCount; i++) {
			line.add(this.formatter.format(rs, i, meta.getColumnType(i)));
		}
		
		println(line);
	}
	
	/**
	 * １行分のデータをファイルに出力する
	 * @param line
	 */
	abstract protected void println(List<String> line); 

}
