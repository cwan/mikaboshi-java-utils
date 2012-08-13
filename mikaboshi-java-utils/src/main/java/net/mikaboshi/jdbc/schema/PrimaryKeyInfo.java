package net.mikaboshi.jdbc.schema;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang.builder.EqualsBuilder;

/**
 * １つのテーブルの主キー情報。
 * @author Takuma Umezawa
 *
 */
public class PrimaryKeyInfo {
	
	public PrimaryKeyInfo() {}

	private String tableCat;
	
	private String tableSchem;
	
	private String tableName;
	
	private List<Column> columnNameList = new ArrayList<Column>();
	
	private String pkName;

	public String getTableCat() {
		return this.tableCat;
	}

	public void setTableCat(final String tableCat) {
		this.tableCat = tableCat;
	}

	public String getTableSchem() {
		return this.tableSchem;
	}

	public void setTableSchem(final String tableSchem) {
		this.tableSchem = tableSchem;
	}

	public String getTableName() {
		return this.tableName;
	}

	public void setTableName(final String tableName) {
		this.tableName = tableName;
	}

	public String[] getColumnNames() {
		// keySeqでソートする
		Collections.sort(this.columnNameList);
		
		final String[] columnNames = new String[this.columnNameList.size()];
		
		for (int i = 0; i < columnNames.length; i++) {
			columnNames[i] = this.columnNameList.get(i).name;
		}
		
		return columnNames;
	}
	
	public void addColumnName(final int keySeq, final String columnName) {
		this.columnNameList.add(new Column(keySeq, columnName));
	}

	public String getPkName() {
		return this.pkName;
	}

	public void setPkName(final String pkName) {
		this.pkName = pkName;
	}
	
	/**
	 * tableCat, tableSchem, tableNameが等しいときtrue。
	 * tableCatが両方ともnullの場合は、tableSchemとtableNameで比較する。
	 * tableSchemも両方nullの場合は、tableNameだけで比較する。
	 */
	@Override
	public boolean equals(Object o) {
		if (!(o instanceof PrimaryKeyInfo)) {
			return false;
		}
		
		PrimaryKeyInfo rhs = (PrimaryKeyInfo) o;
		
		return new EqualsBuilder()
			.append(this.tableCat, rhs.tableCat)
			.append(this.tableSchem, rhs.tableSchem)
			.append(this.tableName, rhs.tableName)
			.isEquals();
	}
	
	static class Column implements Comparable<Column> {
		int seq;
		String name;
		
		public Column(int seq, String name) {
			this.seq = seq;
			this.name = name;
		}
		
		public int compareTo(Column o) {
			if (o == null) {
				throw new NullPointerException("nullは比較できません");
			}
			
			if (this.seq < o.seq) {
				return -1;
			} else if (this.seq > o.seq) {
				return 1;
			} else {
				return 0;
			}
		}
	}
}
