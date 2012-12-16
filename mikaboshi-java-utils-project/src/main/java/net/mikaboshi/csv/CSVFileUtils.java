package net.mikaboshi.csv;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Reader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;

/**
 * CSVファイルを読み書きを簡単に行うためのユーティリティクラス。
 * 
 * @author Takuma Umezawas
 */
public class CSVFileUtils {
	
	private String charset;
	
	private CSVStrategy csvStrategy;
	
	private static final String DEFAULT_CHARSET =
			Charset.defaultCharset().name();
	
	/**
	 * デフォルトコンストラクタ。
	 * CSVStrategyは、{@link StandardCSVStrategy}を使用する。
	 * 読込・書込対象のCSVファイルの文字セットは、システムデフォルトを使用する。
	 */
	public CSVFileUtils() {
		this(new StandardCSVStrategy(), DEFAULT_CHARSET);
	}
	
	/**
	 * 読込・書込対象のCSVファイルの文字セットを指定するコンストラクタ。
	 * CSVStrategyは、{@link StandardCSVStrategy}を使用する。
	 * @param charset
	 */
	public CSVFileUtils(String charset) {
		this(new StandardCSVStrategy(), charset);
	}
	
	/**
	 * CSVStategyの実装を指定するコンストラクタ。
	 * 読込・書込対象のCSVファイルの文字セットは、システムデフォルトを使用する。
	 * @param csvStrategy
	 */
	public CSVFileUtils(CSVStrategy csvStrategy) {
		this(csvStrategy, DEFAULT_CHARSET);
	}
	
	/**
	 * CSVStategyの実装と、読込・書込対象のCSVファイルの文字セットを指定するコンストラクタ。
	 * @param csvStrategy
	 * @param charset
	 */
	public CSVFileUtils(CSVStrategy csvStrategy, String charset) {
		
		if (!Charset.isSupported(charset)) {
			throw new IllegalArgumentException("不正な文字セット:" + charset);
		}
		
		this.csvStrategy = csvStrategy;
		this.charset = charset;
	}

	/**
	 * CSVファイルを読み、リスト形式で返す。
	 * 
	 * @param input
	 * @return リストの項目は１行。１行は項目のリスト。
	 * @throws IOException
	 */
	public List<List<String>>readFile(File input)
			throws IOException {
		
		List<List<String>> result = new ArrayList<List<String>>();
		
		InputStream is = null;
		Reader reader = null;

		try {
			is = FileUtils.openInputStream(input);
			reader = new BufferedReader(
					new InputStreamReader(is, this.charset));
			
			for (String[] lines : this.csvStrategy.csvLines(reader)) {
				result.add(Arrays.asList(lines));
			}
			
		} finally {
			IOUtils.closeQuietly(reader);
			IOUtils.closeQuietly(is);
		}
			
		return result;
	}
	
	/**
	 * リスト形式のデータを、CSVファイルに書き出す。
	 * 
	 * @param data リストの項目は１行。１行は項目のリスト。
	 * @param output
	 * @throws IOException
	 */
	public void writeFile(
			List<List<String>> data, File output) 
			throws IOException {

		OutputStream os = null;
		PrintWriter writer = null;
			
		int maxSize = maxSize(data);
		
		try {
			os = FileUtils.openOutputStream(output);
			writer = new PrintWriter(new OutputStreamWriter(os, this.charset));
			
			for (List<String> line : data) {
				String[] arrayData = new String[maxSize];
				line.toArray(arrayData);
				
				// 項目数を統一する
				for (int i = line.size(); i < maxSize; i++) {
					arrayData[i] = StringUtils.EMPTY;
				}
				
				this.csvStrategy.printLine(arrayData, writer);
			}
			
		} finally {
			IOUtils.closeQuietly(writer);
			IOUtils.closeQuietly(os);
		}
	}
	
	/**
	 * 中のリストの要素数の最大を返す
	 * @param list
	 * @return
	 */
	private <T> int maxSize(List<List<T>> list) {
		int max = 0;
		
		for (List<T> innerList : list) {
			if (max < innerList.size()) {
				max = innerList.size();
			}
		}
		
		return max;
	}
	
}
