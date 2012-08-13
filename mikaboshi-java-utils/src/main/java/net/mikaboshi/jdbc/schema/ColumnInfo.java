package net.mikaboshi.jdbc.schema;

import java.io.Serializable;
import java.sql.DatabaseMetaData;

import org.apache.commons.lang.builder.CompareToBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

/**
 * 列の情報
 * 
 * @see DatabaseMetaData#getColumns(String, String, String, String)
 * @author Administrator
 *
 */
public class ColumnInfo implements Serializable, Comparable<ColumnInfo>{

	private static final long serialVersionUID = 1L;

	public ColumnInfo() {}
	
	private String tableCat;
	
	private String tableSchem;
	
	private String tableName;
	
	private String columnName;
	
	private short dataType;
	
	private String typeName;
	
	private int columnSize;
	
	private int decimalDigits;
	
	private int numPrecRadix;
	
	private Boolean nullable;
	
	private String remarks;
	
	private String columnDef;
	
	private int charOctetLength;
	
	private int ordinalPosition;
	
	private String scopeCatlog;
	
	private String scopeSchema;
	
	private String scopeTable;
	
	private short sourceDataType;
	
	private int primaryKeyOrder = 0;

	public int getPrimaryKeyOrder() {
		return this.primaryKeyOrder;
	}

	public void setPrimaryKeyOrder(int primaryKeyOrder) {
		this.primaryKeyOrder = primaryKeyOrder;
	}

	public int getCharOctetLength() {
		return this.charOctetLength;
	}

	public void setCharOctetLength(int charOctetLength) {
		this.charOctetLength = charOctetLength;
	}

	public String getColumnDef() {
		return this.columnDef;
	}

	public void setColumnDef(String columnDef) {
		this.columnDef = columnDef;
	}

	public String getColumnName() {
		return this.columnName;
	}

	public void setColumnName(String columnName) {
		this.columnName = columnName;
	}

	public int getColumnSize() {
		return this.columnSize;
	}

	public void setColumnSize(int columnSize) {
		this.columnSize = columnSize;
	}

	public short getDataType() {
		return this.dataType;
	}

	public void setDataType(short dataType) {
		this.dataType = dataType;
	}

	public int getDecimalDigits() {
		return this.decimalDigits;
	}

	public void setDecimalDigits(int decimalDigits) {
		this.decimalDigits = decimalDigits;
	}

	public Boolean getNullable() {
		return this.nullable;
	}

	public void setNullable(Boolean nullable) {
		this.nullable = nullable;
	}

	public int getNumPrecRadix() {
		return this.numPrecRadix;
	}

	public void setNumPrecRadix(int numPrecRadix) {
		this.numPrecRadix = numPrecRadix;
	}

	public int getOrdinalPosition() {
		return this.ordinalPosition;
	}

	public void setOrdinalPosition(int ordinalPosition) {
		this.ordinalPosition = ordinalPosition;
	}

	public String getRemarks() {
		return this.remarks;
	}

	public void setRemarks(String remarks) {
		this.remarks = remarks;
	}

	public String getScopeCatlog() {
		return this.scopeCatlog;
	}

	public void setScopeCatlog(String scopeCatalog) {
		this.scopeCatlog = scopeCatalog;
	}

	public String getScopeSchema() {
		return this.scopeSchema;
	}

	public void setScopeSchema(String scopeSchema) {
		this.scopeSchema = scopeSchema;
	}

	public String getScopeTable() {
		return this.scopeTable;
	}

	public void setScopeTable(String scopeTable) {
		this.scopeTable = scopeTable;
	}

	public short getSourceDataType() {
		return this.sourceDataType;
	}

	public void setSourceDataType(short sourceDataType) {
		this.sourceDataType = sourceDataType;
	}

	public String getTableCat() {
		return this.tableCat;
	}

	public void setTableCat(String tableCat) {
		this.tableCat = tableCat;
	}

	public String getTableName() {
		return this.tableName;
	}

	public void setTableName(String tableName) {
		this.tableName = tableName;
	}

	public String getTableSchem() {
		return this.tableSchem;
	}

	public void setTableSchem(String tableSchem) {
		this.tableSchem = tableSchem;
	}

	public String getTypeName() {
		return this.typeName;
	}

	public void setTypeName(String typeName) {
		this.typeName = typeName;
	}
	
	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}

	public int compareTo(ColumnInfo o) {
		
	    return new CompareToBuilder()
	                 .append(this.tableCat, o.tableCat)
	                 .append(this.tableSchem, o.tableSchem)
	                 .append(this.tableName, o.tableName)
	                 .append(this.ordinalPosition, o.ordinalPosition)
	                 .toComparison();
	}
	
}
