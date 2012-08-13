package net.mikaboshi.csv;

import java.io.PrintWriter;
import java.io.Reader;

/**
 * <p>
 * CSVデータのエンコード、デコードを行うクラスのインターフェース。
 * </p><p>
 * このインターフェースの実装クラスには、エスケープの方法や、
 * 使用する区切り文字、不正なフォーマットだったときの処理方法が定義される。
 * </p>
 * @author Takuma Umezawa
 */
public interface CSVStrategy {

	/**
	 * 項目の区切り文字を設定する。
	 * @param delimiter 項目の区切り文字
	 */
	public void setDelimiter(String delimiter);
	
	/**
	 * 項目の区切り文字を返す。
	 * @return 項目の区切り文字
	 */
	public String getDelimiter();
	
	/**
	 * 行の区切り文字を設定する。
	 * @param lineSeparator 行の区切り文字
	 */
	public void setLineSeparator(String lineSeparator);
	
	/**
	 * 行の区切り文字を返す。
	 * @return 行の区切り文字
	 */
	public String getLineSeparator();
	
	/**
	 * 項目がnullの場合に出力する文字を設定する。
	 * @param nullString 項目がnullの場合に出力する文字
	 */
	public void setNullString(String nullString);
	
	/**
	 * 項目がnullの場合に出力する文字を返す。
	 * @return 項目がnullの場合に出力する文字
	 */
	public String getNullString();
	
	/**
	 * Readerで与えられたCSVデータを、1行ごとに読み込むIterableを返す。
	 * 
	 * @param reader
	 * @return
	 */
	public Iterable<String[]> csvLines(Reader reader);

	/**
	 * 項目をエスケープする。
	 * @param rawItem 変換前の文字列
	 * @return　出力形式に加工された項目
	 */
	public String escape(Object rawItem);
	
	/**
	 * 項目をアンエスケープする。
	 * 
	 * @param item 出力形式に加工された項目
	 * @return 変換後の文字列
	 */
	public String unescape(String item);
	
	/**
	 * １行出力する。
	 * @param data 1行のデータ項目
	 * @param out 出力先
	 */
	public void printLine(Object[] data, PrintWriter out);
}
