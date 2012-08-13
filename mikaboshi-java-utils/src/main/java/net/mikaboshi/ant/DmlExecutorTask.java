package net.mikaboshi.ant;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import net.mikaboshi.jdbc.DmlFileExecutor;
import net.mikaboshi.velocity.VelocityUtils;

import org.apache.commons.io.FileUtils;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.types.Parameter;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;

/**
 * <p>
 * ファイルに記述されたDML文を実行するAntタスク。
 * </p><p>
 * DML(UPDATE/INSERT/DELETE)の他、DDL(CREATE TABLE等)も実行可能。
 * </p><p>
 * インプットは、以下のどちらか。
 * <ul>
 *   <li>複数のDML文を記述したテキストファイル
 *   <li>DML文を生成するvelocityテンプレートファイル
 * </ul>
 * </p><p>
 * DML文の区切り文字は「;」とする。
 * </p>
 * 
 * @author Takuma Umezawa
 */
public class DmlExecutorTask extends File2DbTask {
	
	private final M17NTaskLogger logger;

	/**
	 * 本タスクのプロパティをデフォルトに設定する。
	 */
	public DmlExecutorTask() {
		super();
		
		this.logger = new M17NTaskLogger(
				this, AntConstants.LOG_MESSAGE_BASE_NAME);
	}
	
	private final static String DELIMITER = ";";
	
	private boolean velocity;
	
	/**
	 * インプットファイルがVelocityテンプレートならばtrueを指定する。
	 * （省略時はfalse）
	 * @param velocity
	 */
	public void setVelocity(boolean velocity) {
		this.velocity = velocity;
	}
	
	private String velocityPropertiesPath;
	
	/**
	 * Velocityの設定ファイルのパスを指定する。（省略時は、Velocityの標準が適用される）
	 * @param path
	 */
	public void setVelocityProperties(String path) {
		this.velocityPropertiesPath = path;
	}
	
	private boolean deleteTempFile = true;
	
	/**
	 * Velocityを使用する場合、テンプレートから生成したSQLファイルを削除するか指定する。
	 * 省略時は、true（削除する）。
	 * @param b
	 */
	public void setDeleteTempFile(boolean b) {
		this.deleteTempFile = b;
	}
	
	private List<Parameter> parameterList = new ArrayList<Parameter>();
	
	/**
	 * <p>
	 * parameterネストタグを生成する。
	 * </p><p>
	 * このタグには、VelocityContextにバインドするパラメータを設定する。
	 * nameにはパラメータの名前、valueにはパラメータの値、typeにはパラメータの型を指定する。
	 * </p><p>
	 * typeは以下のみ有効。typeを省略した場合、stringと見なす。
	 * <ul>
	 *  <li>string : 文字列
	 *  <li>integer : 整数
	 *  <li>double : 浮動小数点数
	 *  <li>date : 現在日時 （valueには{@link java.text.SimpleDateFormat}のフォーマットを指定する）
	 * </ul>
	 * </p>
	 * 
	 * @return
	 */
	public Parameter createParameter() {
		Parameter parameter = new Parameter();
		this.parameterList.add(parameter);
		return parameter;
	}
	
	/* (非 Javadoc)
	 * @see net.mikaboshi.ant.File2DbTask#executeFile(java.io.File)
	 */
	@Override
	protected void executeFile(File file) throws IOException, SQLException {
		if (this.velocity) {
			
			if (this.velocityPropertiesPath != null) {
				try {
					Velocity.init(this.velocityPropertiesPath);
				} catch (Exception e) {
					this.logger.warn(e,
							"error.invalid_velocity_properties_file",
							new File(this.velocityPropertiesPath).getAbsolutePath());
				}
			}
			
			File tempFile = null;
			
			try {
				tempFile = weave(file);
				
				DmlFileExecutor.execute(
						getCurrentConnection(),
						tempFile,
						getCharset(),
						DELIMITER,
						isHaltOnError());
			} finally {
				if (tempFile != null) {
					if (this.deleteTempFile) {
						tempFile.delete();
					} else {
						this.logger.info("file.execute_temporary",
								tempFile.getAbsolutePath());
					}
				}
			}
			
		} else {
			DmlFileExecutor.execute(
					getCurrentConnection(),
					file,
					getCharset(),
					DELIMITER,
					isHaltOnError());
		}
	}
	
	/**
	 * テンプレートファイルにパラメータを埋め込み、実ファイルに変換する。
	 * @param templateFile テンプレートファイル
	 * @return 変換したファイル
	 * @throws IOException
	 */
	protected File weave(File templateFile) throws IOException {
		
		File tempDir = new File(System.getProperty("java.io.tmpdir"));
		FileUtils.forceMkdir(tempDir);
		
		File tempFile = new File(tempDir, System.currentTimeMillis() + ".sql");
		
		try {
			VelocityUtils.weave(
					templateFile,
					tempFile,
					getCharset(),
					getCharset(),
					getContext());
		} catch (Exception e) {
			this.logger.error(e, "error.velocity_convert");
			throw new IOException(this.logger.getString("error.velocity_convert"));
		}
		
		return tempFile;
	}
	
	
	private VelocityContext context;
	
	/**
	 * parameter要素からVelocityContextを生成する。
	 * 
	 * @return
	 * @throws BuildException パラメータの型が不正な場合
	 */
	protected VelocityContext getContext() {
		if (this.context != null) {
			return this.context;
		}
		
		this.context = new VelocityContext();
		
		for (Parameter parameter : this.parameterList) {
			if (parameter.getType() == null
					|| parameter.getType().toLowerCase().endsWith("string")) {
				this.context.put(parameter.getName(), parameter.getValue());
				
			} else if (parameter.getType().toLowerCase().endsWith("integer")) {
				this.context.put(parameter.getName(),
						Integer.parseInt(parameter.getValue()));
				
			} else if (parameter.getType().toLowerCase().endsWith("double")) {
				this.context.put(parameter.getName(),
						Double.parseDouble(parameter.getValue()));
				
			} else if (parameter.getType().toLowerCase().endsWith("date")) {
				this.context.put(
						parameter.getName(),
						new SimpleDateFormat(parameter.getValue())
							.format(new Date()));
			} else {
				this.logger.throwBuildException(
						"error.unsupported_type",
						parameter.getType());
			}
		}
		
		return this.context;
	}
}
