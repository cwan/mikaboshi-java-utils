package net.mikaboshi.util;

import java.lang.reflect.InvocationTargetException;

import org.apache.commons.beanutils.MethodUtils;

/**
 * リフレクションに関するユーティリティクラス。
 * @author Takuma Umezawa
 *
 */
public final class ReflectionUtils {
	
	private ReflectionUtils() {}

	/**
	 * リフレクションによるメソッドの実行を行う。
	 * リフレクション例外が発生した場合は、実行時例外である {@link UnsupportedOperationException}
	 * がスローされる。
	 * 
	 * @param object　レシーバオブジェクト
	 * @param methodName メソッド名
	 * @param args メソッドの引数
	 * @return メソッドの戻り値
	 * @throws UnsupportedOperationException
	 * 				リフレクションによるメソッドの実行に失敗した場合
	 */
	public static Object invoke(
			Object object, 
			String methodName, 
			Object[] args) {
		try {
			return MethodUtils.invokeMethod(object, methodName, args);
		} catch (NoSuchMethodException e) {
			throw new UnsupportedOperationException(e);
		} catch (IllegalAccessException e) {
			throw new UnsupportedOperationException(e);
		} catch (InvocationTargetException e) {
			throw new UnsupportedOperationException(e);
		}
	}
	
	/**
	 * リフレクションによるメソッドの実行を行う。
	 * メソッドの引数が１つの場合のショートカット。
	 * @see {@link #invoke(Object, String, Object[])}
	 */
	public static Object invoke(Object object, String methodName, Object arg) {
		return invoke(object, methodName, new Object[] {arg});
	}
	
	/**
	 * リフレクションによるメソッドの実行を行う。
	 * メソッドの引数がない場合のショートカット。
	 * @see {@link #invoke(Object, String, Object[])}
	 */
	public static Object invoke(Object object, String methodName) {
		return invoke(object, methodName, null);
	}
}
