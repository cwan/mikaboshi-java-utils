package net.mikaboshi.jdbc;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.map.CaseInsensitiveMap;

/**
 * <p>
 * ResultSetから、1行 = 1 Mapオブジェクトのリストを作成する。
 * Mapのキーはカラム名、値はカラムの値。
 * </p><p>
 * クエリ結果の行数が多い場合は、OutOfMemoryErrorが発生しうる。
 * </p><p>
 * Mapの実装は、{@code org.apache.commons.collections.map.CaseInsensitiveMap}
 * を使用しているため、カラム名の大文字/小文字の区別はない。
 * </p>
 * @author Takuma Umezawa
 *
 */
public class ResultSetToMapListHandler implements ResultSetHandler {

	/**
	 * 結果行数の上限を無限（Integer.MAX_VALUE）にするコンストラクタ。
	 */
	public ResultSetToMapListHandler() {
		this(Integer.MAX_VALUE);
	}
	
	private final int maxRowCount;
	
	/**
	 * 結果行数の上限を設定する。
	 * この上限より多い行数が検索されても破棄する。
	 *
	 * @param maxRowCount
	 */
	public ResultSetToMapListHandler(int maxRowCount) {
		this.maxRowCount = maxRowCount;
	}
	
	/**
	 * このクラスでは何も行わない。
	 */
	public void after() throws SQLException {
	}
	
	private List<Map<String, Object>> resultList;
	
	public List<Map<String, Object>> getResultList() {
		return this.resultList;
	}
	
	private String[] columnNames;
	
	public String[] getColumnNames() {
		return this.columnNames;
	}
	
	private int[] columnTypes;
	
	public int[] getColumnTypes() {
		return this.columnTypes;
	}
	
	private int rowCount;
	
	/**
	 * 前処理として、カラムの名前、型を取得する。
	 */
	public void before(ResultSetMetaData meta) throws SQLException {
		this.rowCount = 0;
		this.resultList	= new ArrayList<Map<String, Object>>();
		
		final int columnCount = meta.getColumnCount();
		
		this.columnNames = new String[columnCount];
		this.columnTypes = new int[columnCount];
		
		for (int i = 0; i < columnCount; i++) {
			this.columnNames[i] = meta.getColumnName(i + 1);
			this.columnTypes[i] = meta.getColumnType(i + 1);
		}
	}

	/**
	 * このクラスでは何も行わない。
	 */
	public void close() throws SQLException {
	}

	/**
	 * ResultSetから、Mapを生成する。
	 */
	@SuppressWarnings("unchecked")
	public void handle(ResultSet rs) throws SQLException {
		this.rowCount++;
		
		if (this.rowCount > this.maxRowCount) {
			return;
		}
		
		Map<String, Object> rowData = new CaseInsensitiveMap();
		
		for (int i = 0; i < this.columnNames.length; i++) {
			rowData.put(
					this.columnNames[i], 
					rs.getObject(i + 1));
		}
		
		this.resultList.add(rowData);
	}

}
