package net.mikaboshi.ant;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;

import net.mikaboshi.jdbc.ArrayToDbImporter;
import net.mikaboshi.jdbc.DbUtils;
import net.mikaboshi.util.MkStringUtils;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;


/**
 * <p>
 * Excelファイルの内容をDBにインポートするAntタスク。
 * </p><p>
 * JakartaPOIを使用する。
 * </p><p>
 * Excelのフォーマットは、以下の通り。
 * <ul>
 * 	<li>シート名をテーブル名と見なす</li>
 * 	<li>データは1行目、1列目から開始する</li>
 * 	<li>existsHeader属性を指定したとき、1行目はカラム名とする</li>
 * 	<li>existsHeader属性を指定しないとき、カラムの順番はテーブルのメタ情報から取得する</li>
 * 	<li>1行目から順に走査して、最初に空行がきたところで終了する</li>
 * </ul>
 * </p><p>
 * このクラスは同期化されない。
 * </p>
 * 
 * @author Takuma Umezawa
 */
public class Xls2DbTask extends File2DbTask {

	private final M17NTaskLogger logger;
	
	/**
	 * 本タスクのプロパティをデフォルトに設定する。
	 */
	public Xls2DbTask() {
		super();
		
		this.logger = new M17NTaskLogger(
				this, AntConstants.LOG_MESSAGE_BASE_NAME);
	}
	
	private Pattern includeTableNamePattern;
	
	private String[] includeTableNames;
	
	/**
	 * <p>
	 * 読み込み対象とするシート名（＝テーブル名）を指定する。
	 * </p><p>
	 * 正規表現で指定する場合は、始めと終わりの文字を「/」にする。
	 * 正規表現で無い場合は、カンマ区切りで複数の文字列を指定できる。
	 * </p><p>
	 * 指定しなかった場合、ignoreで指定されていない全てのシートを対象とする。
	 * （つまり、matchで一致し、ignoreにも一致する場合は対象としない。）
	 * </p>
	 * @param regex
	 */
	public void setMatch(String arg) {
		if (arg == null) {
			return;
		}
		
		if (arg.length() > 1 && arg.startsWith("/") && arg.endsWith("/")) {
			this.includeTableNamePattern =
				Pattern.compile(arg.substring(1, arg.length() - 1));
			return;
		}
		
		this.includeTableNames = StringUtils.split(arg, ',');
		MkStringUtils.trimAll(this.includeTableNames);
	}
	
	private Pattern excludeTableNamePattern;
	
	private String[] excludeTableNames;
	
	/**
	 * <p>
	 * 読み込み対象としないシート名（＝テーブル名）を指定する。
	 * </p><p>
	 * 正規表現で指定する場合は、始めと終わりの文字を「/」にする。
	 * 正規表現で無い場合は、カンマ区切りで複数の文字列を指定できる。
	 * </p><p>
	 * 指定しなかった場合、matchで指定した全てのシートを対象とする。
	 * </p>
	 * @param regex
	 */
	public void setIgnore(String arg) {
		if (arg == null) {
			return;
		}
		
		if (arg.length() > 1 && arg.startsWith("/") && arg.endsWith("/")) {
			this.excludeTableNamePattern =
				Pattern.compile(arg.substring(1, arg.length() - 1));
			return;
		}
		
		this.excludeTableNames = StringUtils.split(arg, ',');
		MkStringUtils.trimAll(this.excludeTableNames);
	}
	
	/**
	 * 読み込み対象としないシート名を指定する。
	 * @param regex
	 */
	public void sheetNameIgnore(String regex) {
		this.excludeTableNamePattern = Pattern.compile(regex);
	}
	
	/**
	 * Excelファイルの文字コードは指定できないため、この属性が設定された場合は
	 * 例外がスローされる。
	 * @throws UnsupportedOperationException 常にスローされる
	 */
	@Override
	public void setCharset(String charset) {
		throw new UnsupportedOperationException("Xls2DbTask#setCharset(String) is not supported");
	}
	
	/* (非 Javadoc)
	 * @see net.mikaboshi.ant.File2DbTask#executeFile(java.io.File)
	 */
	@Override
	protected void executeFile(File file) throws IOException, SQLException {
	
		InputStream input = null;
		
		try {
			input = new BufferedInputStream(FileUtils.openInputStream(file));
			HSSFWorkbook workbook = new HSSFWorkbook(new POIFSFileSystem(input));
			
			int sheetNum = workbook.getNumberOfSheets();

			for (int i = 0; i < sheetNum; i++) {
				this.sheetName = workbook.getSheetName(i);
				executeSheet(workbook.getSheetAt(i), this.sheetName);
			}
			
		} catch (IOException e) {
			this.logger.error(e,
					"error.read_file",
					file.getAbsolutePath());
			
		} catch (SQLException e) {
			this.logger.error(e,
					"error.transport_from_file_to_db",
					file.getAbsolutePath());
			
		} finally {
			IOUtils.closeQuietly(input);
		}
	}
	
	private String sheetName;
	private int rowIndex;
	private short columnIndex;
	
	private void executeSheet(HSSFSheet sheet, String tableName) throws SQLException {
		
		if (isIgnore(tableName)) {
			return;
		}
		
		DbUtils.validateTableName(tableName);
		
		truncateIfRequred(tableName, getCurrentConnection());
		
		// INSERTまたはUPDATEに成功した件数
		int insertOrUpdateRowCount = 0;
		
		this.rowIndex = -1;
		
		ArrayToDbImporter arrayToDbImporter = 
			new ArrayToDbImporter(getCurrentConnection());
		arrayToDbImporter.setSchemaName(getSchema());
		arrayToDbImporter.setTableName(tableName);
		arrayToDbImporter.setReplace(isReplace());
		arrayToDbImporter.setNullString(getNullString());
		arrayToDbImporter.setCaseSensitive(isCaseSensitive());
		
		if (!isExistsHeader()) {
			// シートの1行目がカラム名ではない場合、テーブル定義のカラム順でImporterを初期化
			arrayToDbImporter.initialize();
		}
		
		try {
			for (@SuppressWarnings("unchecked")
				 Iterator<HSSFRow> rIter = sheet.rowIterator(); rIter.hasNext();) {
				
				this.rowIndex++;
					
				try {
					int result = executeRow(arrayToDbImporter, rIter.next());
					
					if (result < 0) {
						// 空行がきたら終了
						break;
					}
					
					insertOrUpdateRowCount += result;
				
				} catch (SQLException e) {
					if (isHaltOnError()) {
						throw e;
					}
					
					this.logger.warn(e,
							"continue_on_error.physical_line",
							this.rowIndex + 1);
					
					// PostgreSQLの場合、ロールバックが必要
					DbUtils.rollbackQuietly(getCurrentConnection());
				}
					
			} // end row iterator
			
		} finally {
			this.logger.debug("lines.execute", this.rowIndex + 1);
			this.logger.info("lines.insert_update", insertOrUpdateRowCount);
			
			if (arrayToDbImporter != null) {
				arrayToDbImporter.close();
			}
		}
	}
	
	/**
	 * 引数のテーブルをインポート対象としないかどうか判定する。
	 * @param tableName
	 * @return 対象としないならばtrueを返す。
	 */
	private boolean isIgnore(String tableName) {
		if (this.excludeTableNamePattern != null) {
			if (this.excludeTableNamePattern.matcher(tableName).matches()) {
				return true;
			}
		}
		
		if (this.excludeTableNames != null) {
			for (String s : this.excludeTableNames) {
				if (s.equals(tableName)) {
					return true;
				}
			}
		}
		
		if (this.includeTableNamePattern != null) {
			if (this.includeTableNamePattern.matcher(tableName).matches()) {
				return false;
			}
		}
		
		if (this.includeTableNames != null) {
			for (String s : this.includeTableNames) {
				if (s.equals(tableName)) {
					return false;
				}
			}
		}
		
		if (this.includeTableNamePattern == null &&
				this.includeTableNames == null) {
			return false;
		} else {
			return true;
		}
	}
	
	/**
	 * 行の挿入/更新を実行する。
	 * 
	 * @param arrayToDbImporter
	 * @param row
	 * @return INSERT/UPDATEの件数。最後の行を過ぎた場合は、-1を返す。
	 * @throws SQLException
	 */
	private int executeRow(
			ArrayToDbImporter arrayToDbImporter,
			HSSFRow row) throws SQLException {
		
		if (this.rowIndex == 0 && isExistsHeader()) {
			// シートの1行目がカラム名の場合、カラムの順序を指定してImporterを初期化
			
			List<String> list = new ArrayList<String>();
			
			for (@SuppressWarnings("unchecked")
				 Iterator<HSSFCell> iter = row.cellIterator(); iter.hasNext();) {
				HSSFCell cell = iter.next();
				list.add(getCellValue(cell));
			}
			
			arrayToDbImporter.setColumnNames(
					list.toArray(new String[list.size()]));
			arrayToDbImporter.initialize();
			return 0;
		}
		
		String[] rowData = 
			getRowData(row, arrayToDbImporter.getNumberOfColumns());
		
		if (rowData == null) {
			// 空行がきたら終了
			return -1;
		}
		
		return arrayToDbImporter.execute(rowData);
	}
	
	/**
	 * １行のデータをString配列で取得する。全てのカラムが空白の場合はnullを返す。
	 * @param row
	 * @param numberOfColumns
	 * @return
	 */
	private String[] getRowData(HSSFRow row, int numberOfColumns) {
		String[] result = new String[numberOfColumns];
		Arrays.fill(result, "");
		
		boolean isAllBlank = true;
		
		for (this.columnIndex = 0; columnIndex < numberOfColumns; columnIndex++) {
			HSSFCell cell = row.getCell(columnIndex);
			
			result[columnIndex] = getCellValue(cell);
			
			if (StringUtils.isNotBlank(result[columnIndex])) {
				isAllBlank = false;
			}
		}
		
		if (isAllBlank) {
			return null;
		}

		return result;
	}
	
	private String getCellValue(HSSFCell cell) {
		if (cell == null) {
			return StringUtils.EMPTY;
		}
		
		switch (cell.getCellType()) {
			case HSSFCell.CELL_TYPE_BLANK:
				return StringUtils.EMPTY;
			
			case HSSFCell.CELL_TYPE_BOOLEAN:
				return Boolean.toString(cell.getBooleanCellValue());
				
			case HSSFCell.CELL_TYPE_FORMULA:
			case HSSFCell.CELL_TYPE_NUMERIC:
				try {
					return Double.toString(cell.getNumericCellValue());
				} catch (NumberFormatException e) {
					logger.warn(e, "error.HSSFCell.fomula_evaluation",
							this.sheetName, this.rowIndex + 1, this.columnIndex + 1);
					return StringUtils.EMPTY;
				}
			
			case HSSFCell.CELL_TYPE_STRING:
				return cell.getRichStringCellValue().getString();
				
			default:
				logger.warn("error.HSSFCell.cell_type_error", 
						this.sheetName, this.rowIndex + 1, this.columnIndex + 1);
				return StringUtils.EMPTY;
				
		}
	}

}
