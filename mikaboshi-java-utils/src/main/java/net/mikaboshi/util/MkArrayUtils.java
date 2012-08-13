package net.mikaboshi.util;

import org.apache.commons.lang.ArrayUtils;

/**
 * 配列に関するユーティリティクラス。
 * @author Takuma Umezawa
 * @since 0.1.2
 */
public final class MkArrayUtils {
	private MkArrayUtils() {}
	
	/**
	 * 第一引数の配列の末尾に第二引数を追加した配列を返す。
	 * 第一引数、第二引数ともにnullの場合、nullを返す。
	 * 第二引数がnullの場合、第一引数をそのまま返す。
	 * 第一引数がnullの場合、第二引数のみを要素にした配列を返す。
	 * 
	 * @param <T>
	 * @param args
	 * @param arg
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static <T> T[] merge(T[] args, T arg) {
		if (args == null && arg == null) {
			return null;
		}
		
		if (arg == null) {
			return args;
		}
		
		return (T[]) ArrayUtils.add(args, arg);
	}
	
	/**
	 * 引数がnullまたは要素数が0ならばtrueを返す。
	 * @param <T>
	 * @param array
	 * @return
	 * @since 1.1.5
	 */
	public static <T> boolean isNullOrEmpty(T[] array) {
		return array == null || array.length == 0;
	}
	
	/**
	 * 引数がnullでなく、要素数が1以上ならばtrueを返す。
	 * @param <T>
	 * @param array
	 * @return
	 */
	public static <T> boolean isNotEmpty(T[] array) {
		return !isNullOrEmpty(array);
	}
}
