package net.mikaboshi.jdbc.schema;

import java.math.BigDecimal;
import java.sql.Blob;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;

/**
 * SQLデータ型に関する処理を行うユーティリティクラス。
 * 
 * TODO Hibernateとかのライブラリを流用できないか
 * 
 * @see java.sql.Types
 * @author Takuma Umezawa
 *
 */
public class DataTypeUtils {

// テンプレ
//	switch (type) {
//	case BIT:
//	case TINYINT:
//	case SMALLINT:
//	case INTEGER:
//	case BIGINT:
//	case FLOAT:
//	case REAL:
//	case DOUBLE:
//	case NUMERIC:
//	case DECIMAL:
//	case CHAR:
//	case VARCHAR:
//	case LONGVARCHAR:
//	case DATE:
//	case TIME:
//	case TIMESTAMP:
//	case BINARY:
//	case VARBINARY:
//	case LONGVARBINARY:
//	case NULL:
//	case OTHER:
//	case JAVA_OBJECT:
//	case DISTINCT:
//	case STRUCT:
//	case ARRAY:
//	case BLOB:
//	case CLOB:
//	case REF:
//	case DATALINK:
//	case BOOLEAN:
//	default:
//}
	
	/**
	 * SQL文のリテラル表記の文字列を取得する。
	 * 
	 * @param rs
	 * @param columnIndex
	 * @return
	 * @throws SQLException
	 */
	public static String formatSqlStatementRiteral(Object data)
			throws SQLException {
		
		if (data == null) {
			return "null";
		}
		
		// BIT
		if (data instanceof Boolean) {
			if (((Boolean) data).booleanValue()) {
				return "'1'";
			} else {
				return "'0'";
			}
		}
		
		// TINYINT
		if (data instanceof Byte) {
			return Byte.toString((Byte) data);
		}
		
		// SMALLINT
		if (data instanceof Short) {
			return Short.toString((Short) data);
		}
		
		// INTEGER
		if (data instanceof Integer) {
			return Integer.toString((Integer) data);
		}
		
		// BIGINT
		if (data instanceof Long) {
			return Long.toString((Long) data);
		}
		
		// FLOAT, REAL
		if (data instanceof Float) {
			return Float.toString((Float) data);
		}
		
		// DOUBLE, NUMERIC, DECIMAL
		if (data instanceof Double) {
			return Double.toString((Double) data);
		}
		
		// CHAR, VARCHAR, LONGVARCHAR
		if (data instanceof String) {
			return "'" + (String) data + "'";
		}
		
		// DATE
		if (data instanceof java.sql.Date) {
			// yyyy-mm-dd
			return "'" + data.toString() + "'";
		}
		
		// TIME
		if (data instanceof java.sql.Time) {
			// hh:mm:ss
			return "'" + data.toString() + "'";
		}
		
		// TIMESTAMP
		if (data instanceof java.sql.Timestamp) {
			// yyyy-mm-dd hh:mm:ss.fffffffff
			return "'" + data.toString() + "'"; 
		}
			
		// BLOBなど？
		return "'" + data.toString() + "'";
	}
	
	/**
	 * 型が数値型ならばtrueを返す
	 * @param iType
	 * @return
	 */
	public static boolean isNumericType(int iType) {
		return
			iType == Types.TINYINT ||
			iType == Types.SMALLINT ||
			iType == Types.INTEGER ||
			iType == Types.BIGINT ||
			iType == Types.FLOAT ||
			iType == Types.REAL ||
			iType == Types.DOUBLE ||
			iType == Types.NUMERIC ||
			iType == Types.DECIMAL;
	}
	
	/**
	 * PreparedStatementオブジェクトにパラメータを設定する。
	 * @param pstmt
	 * @param rowData
	 * @param types
	 * @throws SQLException
	 */
	public static void setParameter(
			final PreparedStatement pstmt,
			final Object[] rowData,
			final int[] types) throws SQLException {
		
		if (rowData.length != types.length) {
			throw new IllegalArgumentException("length of rowData and length of types are not match.");
		}
		
		pstmt.clearParameters();
		
		for (int i = 0; i < rowData.length; i++) {
			if (rowData[i] instanceof BigDecimal) {
				final BigDecimal dec = (BigDecimal) rowData[i];
				
				if (dec.scale() >= 0) {
					pstmt.setObject(i + 1, rowData[i], types[i], dec.scale());
					continue;
				}
			}
			
			if (rowData[i] instanceof Blob) {
				Blob blob = (Blob) rowData[i];
				pstmt.setBinaryStream(i + 1, blob.getBinaryStream(), (int) blob.length());
				continue;
			}
			
			if (rowData[i] instanceof String) {
				
				String data = (String) rowData[i];
				
				switch (types[i]) {
					case Types.TINYINT:
					case Types.SMALLINT:
					case Types.INTEGER:
						
						pstmt.setInt(i + 1, new BigDecimal(data).intValue());
						continue;
						
					case Types.BIGINT:
						pstmt.setLong(i + 1, new BigDecimal(data).longValue());
						continue;
						
					case Types.REAL:
						pstmt.setFloat(i + 1, new BigDecimal(data).floatValue());
						continue;
						
					case Types.FLOAT:
					case Types.DOUBLE:
						pstmt.setDouble(i + 1, new BigDecimal(data).doubleValue());
						continue;
						
					case Types.NUMERIC:
					case Types.DECIMAL:
						pstmt.setBigDecimal(i + 1, new BigDecimal(data));
						continue;
				}
			}
			
			pstmt.setObject(i + 1, rowData[i], types[i]);
		}
	}

}
