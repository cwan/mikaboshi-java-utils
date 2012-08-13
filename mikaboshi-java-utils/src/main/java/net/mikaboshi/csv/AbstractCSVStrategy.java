package net.mikaboshi.csv;

import java.io.PrintWriter;
import java.io.Serializable;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;

/**
 * CSVStrategyの共通メソッドを定義した抽象クラス。
 * 
 * @author Takuma Umezawa
 */
public abstract class AbstractCSVStrategy implements CSVStrategy, Serializable {

	private static final long serialVersionUID = 1L;
	
	private String lineSeparator;
	
	/* (非 Javadoc)
	 * @see net.mikaboshi.csv.CSVStrategy#getLineSeparator()
	 */
	public String getLineSeparator() {
		return this.lineSeparator != null ? this.lineSeparator : IOUtils.LINE_SEPARATOR;
	}

	/* (非 Javadoc)
	 * @see net.mikaboshi.csv.CSVStrategy#setLineSeparator(java.lang.String)
	 */
	public void setLineSeparator(String lineSeparator) {
		this.lineSeparator = lineSeparator;
	}

	/* (non-Javadoc)
	 * @see net.mikaboshi.csv.CSVStrategy#printLine(java.lang.Object[], java.io.PrintWriter)
	 */
	public void printLine(Object[] data, PrintWriter out) {
		
		for (int i = 0; i < data.length; i++) {
			if (i != 0) {
				out.print(getDelimiter());
			}
			
			out.print(escape(data[i]));
		}
		
		out.print(getLineSeparator());
	}
	
	private String nullString = StringUtils.EMPTY;
	
	/* (非 Javadoc)
	 * @see net.mikaboshi.csv.CSVStrategy#setNullString(java.lang.String)
	 */
	public void setNullString(String nullString) {
		this.nullString = nullString;
	}
	
	/* (non-Javadoc)
	 * @see net.mikaboshi.csv.CSVStrategy#nullValue()
	 */
	public String getNullString() {
		return this.nullString;
	}
	
	private String delimiter = ",";
	
	/* (非 Javadoc)
	 * @see net.mikaboshi.csv.CSVStrategy#setDelimiter(java.lang.String)
	 */
	public void setDelimiter(String delimiter) {
		this.delimiter = delimiter;
	}
	
	/* (非 Javadoc)
	 * @see net.mikaboshi.csv.CSVStrategy#getDelimiter()
	 */
	public String getDelimiter() {
		return this.delimiter;
	}

}
