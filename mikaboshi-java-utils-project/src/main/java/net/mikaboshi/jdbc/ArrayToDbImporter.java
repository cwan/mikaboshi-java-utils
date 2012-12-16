package net.mikaboshi.jdbc;

import static net.mikaboshi.validator.SimpleValidator.validateNotNull;
import static net.mikaboshi.validator.SimpleValidator.validateNotNullNorEmpty;
import static net.mikaboshi.validator.SimpleValidator.validatePattern;
import static net.mikaboshi.validator.SimpleValidator.validatePositive;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.mikaboshi.jdbc.schema.ColumnInfo;
import net.mikaboshi.jdbc.schema.DataTypeUtils;
import net.mikaboshi.jdbc.schema.PrimaryKeyInfo;
import net.mikaboshi.jdbc.schema.SchemaUtils;
import net.mikaboshi.validator.ValidatorException;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * 文字列の配列で与えられたデータを、指定されたテーブルに
 * インポートする。
 * 
 * @author Takuma Umezawa
 *
 */
public class ArrayToDbImporter {
	
	private static Log logger = LogFactory.getLog(ArrayToDbImporter.class);

	private Connection connection;
	
	private String schemaName;
	
	private String tableName;
	
	private boolean replace;
	
	/**
	 * @param connection 接続済みのDBコネクション。
	 * @throws NullPointerException connectionがnullの場合
	 */
	public ArrayToDbImporter(Connection connection) {
		validateNotNull(connection, "connection", NullPointerException.class);
		this.connection = connection;
	}

	/**
	 * <p>
	 * インポート先のスキーマ名を設定する。
	 * </p><p>
	 * 未指定の場合はスキーマを限定しない。ワイルドカード不可。
	 * tableNameと合わせて、挿入先のテーブルが特定できなければならない。
	 * </p><p>
	 * スキーマ名のとして許可されるパターンは、{@code ^[a-zA-Z0-9\\._]{1,30}$}
	 * </p><p>
	 * {@link #initialize()}を実行する前に設定すること。
	 * </p>
	 * 
	 * @param schemaName
	 * @throws IllegalArgumentException スキーマ名が不正な場合
	 */
	public void setSchemaName(String schemaName) {
		if (schemaName != null) {
			validatePattern(
					schemaName,	
					"^[a-zA-Z0-9\\._]{1,30}$", 
					"schemaName",
					IllegalArgumentException.class);
		}
		
		this.schemaName = schemaName;
	}
	
	/**
	 * <p>
	 * インポート先のテーブル名を設定する。（必須）
	 * </p><p>
	 * ワイルドカード不可。
	 * schemaNameと合わせて、挿入先のテーブルが特定できなければならない。
	 * </p><p>
	 * テーブル名として許可されるパターンは、{@code ^[a-zA-Z0-9\\._]{1,30}$}
	 * </p><p>
	 * {@link #initialize()}を実行する前に設定すること。
	 * </p>
	 * @param tableName
	 * @throws NullPointerException tableNameがnullの場合
	 * @throws IllegalArgumentException tableNameが不正の場合
	 */
	public void setTableName(String tableName) {
		validateNotNull(tableName, "tableName", NullPointerException.class);
		DbUtils.validateTableName(tableName);
		
		this.tableName = tableName;
	}
	
	/**
	 * <p>
	 * 主キーが一致するレコードが存在する場合、更新するかどうかを設定する。
	 * </p><p>
	 * trueを指定した場合、主キーが一致するレコードが存在するときは
	 * INSERTではなくUPDATEを行う。
	 * falseを指定し、主キーが一致するレコードが存在する場合には
	 * SQLExceptionとなる。
	 * </p><p>
	 * 指定しなければ、falseとなる。
	 * </p><p>
	 * {@link #initialize()}を実行する前に設定すること。
	 * </p>
	 * @param replace
	 */
	public void setReplace(boolean replace) {
		this.replace = replace;
	}
	
	/**
	 * <p>
	 * INSERT文のカラムの順序を、カラム名の配列で指定する。
	 * </p><p>
	 * 指定しなかった場合は、DBのメタ情報から取得したカラム順を適用する。
	 * </p><p>
	 * {@link #initialize()}を実行する前に設定すること。
	 * </p>
	 * @param columnNames
	 */
	public void setColumnNames(String[] columnNames) {
		this.insertColumnNames = columnNames;
	}
	
	private String nullString;
	
	/**
	 * <p>
	 * カラムにnullを設定することを示す文字列を指定する。
	 * </p><p>
	 * 指定しなかった場合は、{@link #execute(String[])}の引数の
	 * 配列要素がnullの場合にnullを設定する。
	 * </p><p>
	 * {@link #initialize()}を実行する前に設定すること。
	 * </p>
	 * @param nullString
	 */
	public void setNullString(String nullString) {
		this.nullString = nullString;
	}
	
	private boolean caseSensitive = false;
	
	/**
	 * <p>
	 * テーブル名およびカラム名の大文字/小文字を厳密にするかどうか。
	 * </p><p>
	 * trueならば大文字/小文字を区別する。falseならば区別しない。
	 * デフォルトは、false（区別しない）
	 * </p>
	 * @param b
	 * @since 1.0.1
	 */
	public void setCaseSensitive(boolean b) {
		this.caseSensitive = b;
	}
	
	/**
	 * 指定されたテーブルからメタ情報を取得し、インポートの準備を行う。
	 * 
	 * @throws SQLException
	 * 		指定されたテーブルのメタ情報取得や、PreparedStatement生成時等に
	 * 		おいて、エラーが発生した場合。
	 * @throws IllegalStateException connection, tableNameが未設定の場合
	 */
	public void initialize() throws SQLException {
		
		validateNotNull(this.connection, "connection", 
				IllegalStateException.class);
		validateNotNull(this.tableName, "tableName", 
				IllegalStateException.class);
		
		adjustTableName();
		
		// INSERT/UPDATEするテーブルのカラムの情報取得
		analyzeColumnInfo();

		// INSERT文のPreparedStatement生成
		createInsertPreparedStatement();
		
		// UPDATE文のPreparedStatement生成
		if (this.replace) {
			createUpdatePreparedStatement();
		}
	}
	
	/**
	 * テーブル名の調整を行う。
	 * @throws SQLException 
	 */
	private void adjustTableName() throws SQLException {
		Set<String> allTableNames = SchemaUtils.getAllTableNames(
				this.connection, null, this.schemaName, null, null);
		
		if (allTableNames.contains(this.tableName)) {
			return;
		}
		
		if (!this.caseSensitive) {
			for (String s : allTableNames) {
				if (s.equalsIgnoreCase(this.tableName)) {
					if (logger.isDebugEnabled()) {
						logger.debug(String.format("指定されたテーブル<%s>の代わりに<%s>を適用します",
								this.tableName, s));
					}
					this.tableName = s;
					return;
				}
			}
		}
		
		throw new SQLException("Table '" + this.tableName + "' not found.");
	}
	
	/** 指定されたテーブルのカラム情報 */
	private List<ColumnInfo> columnInfoList;
	
	/** INSERT文の順序が設定されたカラム名の配列 */
	private String[] insertColumnNames;
	
	/** INSERT文の順序が設定されたカラムのデータ型（java.sql.Types）の配列 */
	private int[] insertDataTypes;
	
	/** INSERT文のPreparedStatement */
	private PreparedStatement insertStatement;
	
	/** UPDATE文のPreparedStatement */
	private PreparedStatement updateStatement;
	
	/** SELECT文のPreparedStatement */
	private PreparedStatement selectStatement;
	
	/** 対象テーブルの主キーのカラム名の配列 */
	private String[] primaryKeyColumnNames;
	
	/** 対象テーブルの主キーカラムのデータ型（java.sql.Types）の配列 */
	private int[] primaryKeyColumnTypes;
	
	/**
	 * String配列で与えられた１行のデータをインポートする。
	 * 
	 * @param rowData 
	 * @return
	 * @throws SQLException
	 */
	public int execute(String[] rowData) throws SQLException {
		// null値の置換
		if (this.nullString != null) {
			for (int i = 0; i < rowData.length; i++) {
				if (this.nullString.equals(rowData[i])) {
					rowData[i] = null;
				}
			}
		}

		if (this.replace) {
			// replaceが指定された場合は、まず主キーでUPDATEを試みる
			
			String[] updateRowData = sortRowDataForUpdate(rowData);
			
			if (this.updateStatement == null) {
				// 主キーのみのテーブルの場合は、クエリを発行し、
				// 行が存在するならば何もしない（存在しないならばINSERTする）
				
				if (logger.isDebugEnabled()) {
					logger.debug("select keys: <'" + StringUtils.join(updateRowData, "', '") + "'>");
				}
				
				DataTypeUtils.setParameter(
						this.selectStatement, 
						updateRowData, 
						getUpdateDataTypes());
				
				ResultSet rs = null;
				try {
					rs = this.selectStatement.executeQuery();
					if (!rs.next()) {
						throw new SQLException("SELECT実行失敗");
					}
					
					int count = rs.getInt(1);
					
					if (count == 1) {
						// 同じ行が存在する
						return 0;
					}
					
				} finally {
					DbUtils.closeQuietly(rs);
				}
				
			} else {
			
				if (logger.isDebugEnabled()) {
					logger.debug("update parameters: <'" + StringUtils.join(updateRowData, "', '") + "'>");
				}
				
				DataTypeUtils.setParameter(
						this.updateStatement, 
						updateRowData, 
						getUpdateDataTypes());
				
				int count = this.updateStatement.executeUpdate();
				
				if (count == 1) {
					return 1;
				}
				
				if (count != 0) {
					throw new AssertionError("count = " + count);
				}
			}
		}

		if (logger.isDebugEnabled()) {
			logger.debug("insert parameters: <'" + StringUtils.join(rowData, "', '") + "'>");
		}
		
		// InsertのPreparedStatementパラメータセット
		DataTypeUtils.setParameter(
				this.insertStatement, 
				rowData, 
				this.insertDataTypes);
		
		return this.insertStatement.executeUpdate();
	}
	
	/**
	 * 対象テーブルのカラムの数を返す。
	 * @return
	 */
	public int getNumberOfColumns() {
		return this.insertColumnNames.length;
	}
	
	/**
	 * インポートに使用したPreparedStatementを閉じる。
	 * 必ず最後に実行すること。
	 */
	public void close() {
		DbUtils.closeQuietly(this.updateStatement);
		DbUtils.closeQuietly(this.insertStatement);
	}
	
	/**
	 * 処理対象テーブルのカラム情報を取得する。
	 * @throws SQLException
	 */
	private void analyzeColumnInfo() throws SQLException {
		
		DatabaseMetaData dbMeta = this.connection.getMetaData();
		
		// 対象テーブルのカラム情報取得
		this.columnInfoList = SchemaUtils.getColumnInfo(
				dbMeta, null, this.schemaName, this.tableName, null);
		
		// カラム名に同じものが複数ないかチェックする
		Set<String> columnNameSetForUniqueCheck =
				new HashSet<String>(this.columnInfoList.size());
		
		for (ColumnInfo info : this.columnInfoList) {
			if (columnNameSetForUniqueCheck.contains(info.getColumnName())) {
				throw new ValidatorException(
						String.format("カラム<%s>が特定できません。（スキーマ:<%s>, テーブル<%s>）", 
								info.getColumnName(), this.schemaName, this.tableName));
			}
			columnNameSetForUniqueCheck.add(info.getColumnName());
		}
		
		validateNotNullNorEmpty(
				this.columnInfoList, "カラム情報リスト", SQLException.class);
		
		if (this.replace) {
			// 主キー情報の取得
			Set<PrimaryKeyInfo> pkInfoSet = SchemaUtils.getPrimaryKeys(
					dbMeta, null, this.schemaName, this.tableName);
			
			if (pkInfoSet.size() != 1) {
				throw new ValidatorException(
						String.format("主キーが特定できません。（スキーマ:<%s>, テーブル<%s>）", 
								this.schemaName, this.tableName));
			}
			
			for (PrimaryKeyInfo pkInfo : pkInfoSet) {
				this.primaryKeyColumnNames = pkInfo.getColumnNames();
			}
			
			if (this.primaryKeyColumnNames.length == 0) {
				throw new ValidatorException(
						String.format("主キー情報が取得できません。（スキーマ:<%s>, テーブル<%s>）", 
								this.schemaName, this.tableName));
			}
			
			// 主キーのデータ型
			this.primaryKeyColumnTypes = new int[this.primaryKeyColumnNames.length];
			
			OUT_LOOP:
			for (int i = 0; i < this.primaryKeyColumnTypes.length; i++) {
				for (ColumnInfo columnInfo : this.columnInfoList) {
					if (columnInfo.getColumnName().equalsIgnoreCase(this.primaryKeyColumnNames[i])) {
						this.primaryKeyColumnTypes[i] = columnInfo.getDataType();
						continue OUT_LOOP;
					}
				}
				
				throw new ValidatorException(
						String.format("カラム<%s>のデータ型が特定できません。（スキーマ:<%s>, テーブル<%s>）", 
								this.primaryKeyColumnNames[i], this.schemaName, this.tableName));
			}
		}
	}
	
	/**
	 * INSERT文のPreparedStatementを生成する。
	 */
	private void createInsertPreparedStatement() throws SQLException {
		
		int columnSize = this.columnInfoList.size();
		
		// INSERT文のカラムの順番取得
		if (this.insertColumnNames == null) {
			// カラムの順番が無指定の場合、メタデータのORDINAL_POSITIONの順を設定
		
			this.insertColumnNames = new String[columnSize];
			this.insertDataTypes = new int[columnSize];
			
			for (int i = 0; i < columnSize; i++) {
				this.insertColumnNames[i] = this.columnInfoList.get(i).getColumnName();
				this.insertDataTypes[i] = this.columnInfoList.get(i).getDataType();
			}
		} else {
			// カラムの順番が指定されている場合
			this.insertDataTypes = new int[columnSize];
			
			OUT_LOOP:
			for (int i = 0; i < this.insertColumnNames.length; i++) {
				for (ColumnInfo columnInfo : this.columnInfoList) {
					
					if (this.caseSensitive) {
						if (columnInfo.getColumnName()
								.equals(this.insertColumnNames[i])) {
						
							this.insertDataTypes[i] = columnInfo.getDataType();
							continue OUT_LOOP;
						}
					} else {
						if (columnInfo.getColumnName()
								.equalsIgnoreCase(this.insertColumnNames[i])) {
							
							if (logger.isDebugEnabled() && 
									!this.insertColumnNames[i].equals(columnInfo.getColumnName())) {
								// 大文字/小文字が異なる場合
								logger.debug(
										String.format("テーブル<%s>: 指定されたカラム名<%s>の代わりに<%s>を適用します",
												this.tableName,
												this.insertColumnNames[i], 
												columnInfo.getColumnName()));
							}
							
							this.insertColumnNames[i] = columnInfo.getColumnName();
							this.insertDataTypes[i] = columnInfo.getDataType();
							continue OUT_LOOP;
						}
					}
				}
				
				throw new SQLException(
						String.format("テーブル<%s>: 存在しないカラム<%s>", 
								this.tableName, this.insertColumnNames[i]));
			}
		}
		
		// INSERT文PreparedStatement生成
		StringBuilder insertSql = new StringBuilder();
		insertSql.append("insert into ");
		insertSql.append(this.tableName);
		insertSql.append(" (");
		insertSql.append(StringUtils.join(this.insertColumnNames, ", "));
		insertSql.append(") values (");
		
		for (int i = 0; i < columnSize; i++) {
			if (i == 0) {
				insertSql.append("?");
			} else {
				insertSql.append(", ?");
			}
		}
		
		insertSql.append(")");
		
		if (logger.isDebugEnabled()) {
			logger.debug("create insert statement: " + insertSql);
		}
		
		this.insertStatement = 
			this.connection.prepareStatement(insertSql.toString());
	}

	/**
	 * UPDATE文のPreparedStatementを生成する。
	 */
	private void createUpdatePreparedStatement() throws SQLException {
		
		validateNotNull(this.tableName, "tableName", NullPointerException.class);
		
		// 先にcreateInsertPreparedStatement()が実行されていなければならない
		validateNotNull(this.insertColumnNames, "insertColumnNames", NullPointerException.class);
		validateNotNull(this.insertDataTypes, "insertDataTypes", NullPointerException.class);
		validatePositive(this.insertColumnNames.length, "insertColumnNames.length", ValidatorException.class);
		
		StringBuilder updateSql = new StringBuilder();
		updateSql.append("update ");
		updateSql.append(this.tableName);
		updateSql.append(" set ");

		List<String> setParams = new ArrayList<String>();

		// 主キー以外のカラムのSET句
		for (String columnName : this.insertColumnNames) {
			if (!ArrayUtils.contains(this.primaryKeyColumnNames, columnName)) {
				setParams.add(columnName + " = ? ");
			}
		}
		
		if (setParams.isEmpty()) {
			// SET句に設定するカラムがない場合、同じ行があるか確かめるクエリを発行する
			// （主キーのみで構成されるテーブルをUPDATEしようとした場合）
			this.updateStatement = null;
			
			StringBuilder selectSql = new StringBuilder();
			selectSql.append("select count(*) from ");
			selectSql.append(this.tableName);
			selectSql.append(" where ");
			
			// WHERE句に主キーカラム
			for (int i = 0; i < this.primaryKeyColumnNames.length; i++) {
				if (i != 0) {
					selectSql.append(" and ");
				}
				
				selectSql.append(this.primaryKeyColumnNames[i]);
				selectSql.append(" = ?");
			}
			
			if (logger.isDebugEnabled()) {
				logger.debug("create select statement: " + selectSql);
			}
			
			this.selectStatement = this.connection.prepareStatement(selectSql.toString());
			return;
		}
		
		updateSql.append(StringUtils.join(setParams, ", "));
		updateSql.append("where ");
		
		// WHERE句に主キーカラム
		for (int i = 0; i < this.primaryKeyColumnNames.length; i++) {
			if (i != 0) {
				updateSql.append(" and ");
			}
			
			updateSql.append(this.primaryKeyColumnNames[i]);
			updateSql.append(" = ?");
		}
		
		if (logger.isDebugEnabled()) {
			logger.debug("create update statement: " + updateSql);
		}
		
		this.updateStatement = this.connection.prepareStatement(updateSql.toString());
	}
	
	private int[] updateColumnOrder;
	
	/**
	 * UPDATE文パラメータを設定するときのカラムの順序を取得する。
	 * 
	 * @return
	 */
	private int[] getUpdateColumnOrder() {
		if (this.updateColumnOrder != null) {
			return this.updateColumnOrder;
		}
		
		List<Integer> list1 = new ArrayList<Integer>();
		List<Integer> list2 = new ArrayList<Integer>();
		
		for (int i = 0; i < this.insertColumnNames.length; i++) {
			if (ArrayUtils.contains(this.primaryKeyColumnNames, this.insertColumnNames[i])) {
				list2.add(new Integer(i));
			} else {		
				list1.add(new Integer(i));
			}
		}
		
		list1.addAll(list2);
		
		this.updateColumnOrder = new int[list1.size()];
		 
		for (int i = 0; i < list1.size(); i++) {
			this.updateColumnOrder[i] = list1.get(i).intValue();
		}
		
		if (this.updateColumnOrder.length != this.insertDataTypes.length) {
			throw new AssertionError(
					"updateColumnOrder.length = " + updateColumnOrder.length + ", " +
					"insertDataTypes.length = " + insertDataTypes.length);
		}
		
		return this.updateColumnOrder;		
	}
	
	private int[] updateDataTypes;
	
	/**
	 * UPDATE文のPreparedStatementに設定するパラメータのデータ型を取得する。
	 * @return
	 */
	private int[] getUpdateDataTypes() {
		
		if (this.updateDataTypes != null) {
			return this.updateDataTypes;
		}
		
		int[] columnOrder = getUpdateColumnOrder();
		
		this.updateDataTypes = new int[this.insertDataTypes.length];

		for (int i = 0; i < columnOrder.length; i++) {
			this.updateDataTypes[i] = this.insertDataTypes[columnOrder[i]];
		}
		
		return this.updateDataTypes;
	}
	
	/**
	 * INSERT用のカラムデータをUPDATE用に並び替える。
	 * @param before
	 * @return
	 */
	private String[] sortRowDataForUpdate(String[] before) {
		
		int[] columnOrder = getUpdateColumnOrder();
		
		String[] after = new String[before.length];
		
		for (int i = 0; i < columnOrder.length; i++) {
			after[i] = before[columnOrder[i]];
		}
		
		return after;
	}
	
}
