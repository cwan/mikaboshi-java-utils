package net.mikaboshi.ant;

import java.util.ResourceBundle;

import net.mikaboshi.util.ResourceBundleWrapper;
import net.mikaboshi.validator.SimpleValidator;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.LogLevel;

/**
 * <p>
 * AntのTaskログを、多言語対応フォーマットで出力する。
 * </p><p>
 * フォーマットは、コンストラクタで指定する {@code java.util.ResourceBundle}
 * から読み込む。
 * </p><p>
 * パラメータの埋め込み方式は、{@code java.text.MessageFormat} に従う。
 * </p>
 * 
 * @author Takuma Umezawa
 * @since 0.1.2
 */
public class M17NTaskLogger {

	private final Task task;
	
	private final ResourceBundleWrapper bundle;
	
	/**
	 * ログにタスク名として出力されるタスクと、ログメッセージテンプレートが定義された
	 * リソースバンドルのベース名を指定する。
	 * @param task ログにタスク名として出力されるタスク
	 * @param messageBaseName ログメッセージテンプレートが定義されたリソースバンドルのベース名
	 * @throws NullPointerException taskまたはmessageBaseNameがnullの場合
	 */
	public M17NTaskLogger(Task task, String messageBaseName) {
		SimpleValidator.validateNotNull(
				task,
				"task",
				NullPointerException.class);
		
		this.task = task;
		
		ResourceBundle rb = ResourceBundle.getBundle(messageBaseName);
		bundle = new ResourceBundleWrapper(rb);
	}
	
	/**
	 * リソースバンドルから、フォーマットしたメッセージ文字列を取得する。
	 * @param key　リソースバンドルのキー 
	 * @param args メッセージパラメータ
	 * @return
	 */
	public String getString(String key, Object ... args) {
		return this.bundle.format(key, args);
	}
	
	/**
	 * リソースバンドルから、フォーマットしたメッセージ文字列を設定したBuildExceptionをスローする。
	 * @param key
	 * @param args
	 * @throws BuildException
	 */
	public void throwBuildException(String key, Object ... args)
			throws BuildException {
		throw new BuildException(getString(key, args));
	}
	
	/**
	 * リソースバンドルから、フォーマットしたメッセージ文字列と、例外オブジェクトを
	 * 設定したBuildExceptionをスローする。
	 * @param t
	 * @param key
	 * @param args
	 * @throws BuildException
	 */
	public void throwBuildException(
			Throwable t, String key, Object ... args) throws BuildException {
		throw new BuildException(getString(key, args), t);
	}

	/**
	 * DEBUGレベルのログを出力する。
	 * @param key
	 * @param args
	 */
	public void debug(String key, Object ... args) {
		String message = getString(key, args);
		this.task.log(message, LogLevel.DEBUG.getLevel());
	}
	
	/**
	 * VORBOSEレベルのログを出力する。
	 * @param key
	 * @param args
	 */
	public void verbose(String key, Object ... args) {
		String message = getString(key, args);
		this.task.log(message, LogLevel.VERBOSE.getLevel());
	}
	
	/**
	 * INFOレベルのログを出力する。
	 * @param key
	 * @param args
	 */
	public void info(String key, Object ... args) {
		String message = getString(key, args);
		this.task.log(message, LogLevel.INFO.getLevel());
	}
	
	/**
	 * WARNレベルのログを出力する。
	 * @param key
	 * @param args
	 */
	public void warn(String key, Object ... args) {
		String message = getString(key, args);
		this.task.log(message, LogLevel.WARN.getLevel());
	}
	
	/**
	 * WARNレベルのログと、例外スタックトレースを出力する。
	 * @param t
	 * @param key
	 * @param args
	 */
	public void warn(Throwable t, String key, Object ... args) {
		String message =
			getString(key, args) +
			IOUtils.LINE_SEPARATOR +
			ExceptionUtils.getStackTrace(t);
		this.task.log(message, LogLevel.WARN.getLevel());
	}
	
	/**
	 * ERRORレベルのログを出力する。
	 * @param key
	 * @param args
	 */
	public void error(String key, Object ... args) {
		String message = getString(key, args);
		this.task.log(message, LogLevel.ERR.getLevel());
	}
	
	/**
	 * ERRORレベルのログと、例外スタックトレースを出力する。
	 * @param t
	 * @param key
	 * @param args
	 */
	public void error(Throwable t, String key, Object ... args) {
		String message =
			getString(key, args) +
			IOUtils.LINE_SEPARATOR +
			ExceptionUtils.getStackTrace(t);
		this.task.log(message, LogLevel.ERR.getLevel());
	}
}
