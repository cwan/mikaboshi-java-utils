package net.mikaboshi.jdbc;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.SQLException;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFDataFormat;
import org.apache.poi.hssf.usermodel.HSSFRichTextString;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;

/**
 * ResultSetからExcelファイルを出力する。
 * 
 * @author Takuma Umezawa
 *
 */
public class ResultSetToXLSHandler extends ResultSetToFileHandler {
	
	private File output;
	
	private HSSFWorkbook workbook;
	
	private HSSFSheet sheet;
	
	private String sheetName;
	
	private HSSFCellStyle style;

	/**
	 * 出力内容を指定するコンストラクタ。
	 * replaceSheet = false
	 * 
	 * @param outputPath 出力先
	 * @param append 既存のファイルが存在する場合、シートを追加するかどうか
	 * @param outputColumnName 列名を出力するかどうか
	 * @param outputMetaInfo 列のメタ情報を出力するかどうか
	 * @param outputRowNumber 行番号を出力するかどうか
	 * @param formatter 値の文字列整形オブジェクト
	 * @param sheetName シート名（nullならばデフォルト）
	 * @throws IOException 
	 */
	public ResultSetToXLSHandler(
			File output,
			boolean append,
			boolean outputColumnName,
			boolean outputMetaInfo,
			boolean outputRowNumber,
			ResultDataFormatter formatter,
			String sheetName) throws IOException {
		
		this(output,
			append,
			outputColumnName,
			outputMetaInfo,
			outputRowNumber,
			false,
			formatter,
			sheetName);
	}
	
	
	/**
	 * 出力内容を指定するコンストラクタ。
	 * 
	 * @param outputPath 出力先
	 * @param append 既存のファイルが存在する場合、シートを追加するかどうか
	 * @param outputColumnName 列名を出力するかどうか
	 * @param outputMetaInfo 列のメタ情報を出力するかどうか
	 * @param outputRowNumber 行番号を出力するかどうか
	 * @param replaceSheet
	 * @param formatter 値の文字列整形オブジェクト
	 * @param sheetName シート名（nullならばデフォルト）
	 * @throws IOException 
	 * @since 1.1.8
	 */
	public ResultSetToXLSHandler(
			File output,
			boolean append,
			boolean outputColumnName,
			boolean outputMetaInfo,
			boolean outputRowNumber,
			boolean replaceSheet,
			ResultDataFormatter formatter,
			String sheetName) throws IOException {
		
		super(outputColumnName, outputMetaInfo, outputRowNumber, formatter);
		
		if (output.isDirectory()) {
			throw new IllegalArgumentException("outputはディレクトリ不可");
		}
		
		if (append && output.exists()) {
			InputStream input = null;
			
			try {
				input = new BufferedInputStream(FileUtils.openInputStream(output));
				this.workbook = new HSSFWorkbook(new POIFSFileSystem(input));
				
			} finally {
				if (input != null) {
					input.close();
				}
			}
		} else {
			this.workbook = new HSSFWorkbook();
		}
		
		this.output = output;
		this.sheetName = sheetName;
		
		this.style = this.workbook.createCellStyle();
		this.style.setDataFormat(HSSFDataFormat.getBuiltinFormat("text"));
		
		if (this.sheetName != null) {
			
			for (int i = 0; i < this.workbook.getNumberOfSheets(); i++) {
				
				if (this.workbook.getSheetName(i).equals(this.sheetName)) {
					if (replaceSheet) {
						this.workbook.removeSheetAt(i);
						break;
					} else {
						throw new IOException("[" + this.sheetName + "]シートは既に存在します");
					}
				}
			}
			
			this.sheet = this.workbook.createSheet(this.sheetName);
		} else {
			this.sheet = this.workbook.createSheet();
		}
	}
	
	/**
	 * Excelファイルへの書き出しを行い、ファイルを閉じる。
	 */
	public void close() throws SQLException {
		OutputStream os = null;
		
		try {
			os = new BufferedOutputStream(FileUtils.openOutputStream(this.output));
			this.workbook.write(os);
			
		} catch (IOException e) {
			// TODO ジェネリック型を使ってうまくできないものか・・・
			throw new RuntimeException("Excel出力失敗 <" + this.output.getAbsolutePath() + ">", e);
		} finally {
			IOUtils.closeQuietly(os);
		}
	}
	
	private int rowNumOfSheet = 0;
	
	/* (非 Javadoc)
	 * @see net.mikaboshi.jdbc.ResultSetToFileHandler#println(java.util.List)
	 */
	@Override
	protected void println(List<String> line) {
		
		HSSFRow row = this.sheet.createRow(this.rowNumOfSheet++);
		
		for (short iCol = 0; iCol < line.size(); iCol++) {
			HSSFCell cell = row.createCell(iCol);
			cell.setCellValue(new HSSFRichTextString(line.get(iCol)));
			cell.setCellStyle(this.style);
		}
	}
}
