package net.mikaboshi.ant;

import net.mikaboshi.util.MkArrayUtils;
import net.mikaboshi.validator.SimpleValidator;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.LogLevel;

/**
 * AntのTaskログを、可変引数でフォーマットする。
 * フォーマットの形式は、{@code java.util.Formatter} とする。 
 * 
 * @author Takuma Umezawa
 *
 */
public class TaskLogger {

	private final Task task;
	
	/**
	 * ログにタスク名として出力されるタスクを指定する。
	 * @param task
	 */
	public TaskLogger(Task task) {
		SimpleValidator.validateNotNull(
				task,
				"task",
				NullPointerException.class);
		
		this.task = task;
	}
	
	/**
	 * DEBUGレベルのログを出力する。
	 * @param format
	 * @param args
	 */
	public void debug(String format, Object ... args) {
		this.task.log(String.format(format, args), LogLevel.DEBUG.getLevel());
	}
	
	/**
	 * VORBOSEレベルのログを出力する。
	 * @param format
	 * @param args
	 */
	public void verbose(String format, Object ... args) {
		this.task.log(String.format(format, args), LogLevel.VERBOSE.getLevel());
	}
	
	/**
	 * INFOレベルのログを出力する。
	 * @param format
	 * @param args
	 */
	public void info(String format, Object ... args) {
		this.task.log(String.format(format, args), LogLevel.INFO.getLevel());
	}
	
	/**
	 * WARNレベルのログを出力する。
	 * @param format
	 * @param args
	 */
	public void warn(String format, Object ... args) {
		this.task.log(String.format(format, args), LogLevel.WARN.getLevel());
	}
	
	/**
	 * WARNレベルのログと、例外スタックトレースを出力する。
	 * @param t
	 * @param format
	 * @param args
	 */
	public void warn(Throwable t, String format, Object ... args) {
		this.task.log(
				String.format(format + "%n%s",
						MkArrayUtils.merge(args, ExceptionUtils.getStackTrace(t))),
				LogLevel.WARN.getLevel());
	}
	
	/**
	 * ERRORレベルのログを出力する。
	 * @param format
	 * @param args
	 */
	public void error(String format, Object ... args) {
		this.task.log(String.format(format, args), LogLevel.ERR.getLevel());
	}
	
	/**
	 * ERRORレベルのログと、例外スタックトレースを出力する。
	 * @param t
	 * @param format
	 * @param args
	 */
	public void error(Throwable t, String format, Object ... args) {
		this.task.log(
				String.format(format + "%n%s",
						MkArrayUtils.merge(args, ExceptionUtils.getStackTrace(t))),
				LogLevel.ERR.getLevel());
	}
}
