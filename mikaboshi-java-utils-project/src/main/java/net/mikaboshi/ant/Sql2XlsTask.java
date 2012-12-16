package net.mikaboshi.ant;

import java.io.IOException;

import net.mikaboshi.jdbc.ResultSetHandler;
import net.mikaboshi.jdbc.ResultSetToXLSHandler;

/**
 * <p>
 * SQLで与えられたクエリ結果をExcelファイルに出力するAntタスク。
 * </p><p>
 * このクラスは同期化されない。
 * </p>
 * @author Takuma Umezawa
 */
public class Sql2XlsTask extends Sql2FileTask {

	/**
	 * 本タスクのプロパティをデフォルトに設定する。
	 */
	public Sql2XlsTask() {
		super();
	}
	
	private String sheetName;
	
	/**
	 * 出力先のシート名を設定する。省略可。
	 * 
	 * @param sheetName
	 */
	public void setSheetName(String sheetName) {
		this.sheetName = sheetName;
	}
	
	private boolean append;
	
	/**
	 * 既存のExcelファイルがあった場合、シートを追加するかどうかを設定する。
	 * デフォルトは、false（追加しない）
	 * @param append trueならば、シートを追加する。falseならばファイルを上書きする。
	 */
	public void setAppend(boolean append) {
		this.append = append;
	}
	
	/**
	 * Excelファイルの文字コードは指定できないため、この属性が設定された場合は
	 * 例外がスローされる。
	 * @throws UnsupportedOperationException 常にスローされる
	 */
	@Override
	public void setCharset(String charset) {
		throw new UnsupportedOperationException("Sql2XlsTask#setCharset(String) is unsupported.");
	}
	
	private boolean replaceSheet = false;
	
	/**
	 * @since 1.1.8
	 */
	public void setReplaceSheet(boolean replaceSheet) {
		this.replaceSheet = replaceSheet;
	}

	/* (非 Javadoc)
	 * @see net.mikaboshi.ant.Sql2FileTask#createHandler()
	 */
	@Override
	protected ResultSetHandler createHandler() throws IOException {
		return new ResultSetToXLSHandler(
				getOutputFile(),
				this.append,
				isHeaderNeeded(),
				false,
				false,
				this.replaceSheet,
				getFormatter(),
				this.sheetName);
	}
}
