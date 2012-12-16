package net.mikaboshi.util;

import net.mikaboshi.validator.SimpleValidator;

/**
 * サロゲートペア文字を扱うためのユーティリティクラス。
 * 
 * @author Takuma Umezawa
 * @since 1.1.5
 */
public final class CodePointUtils {

	private CodePointUtils() {}
	
	/**
	 * コードポイントの配列を返す。
	 * @param str
	 * @return
	 * @throws NullPointerException strがnullの場合
	 */
	public static int[] toCodePointArray(String str) {
		
		SimpleValidator.validateNotNull(str, "str");
		
		int length = str.codePointCount(0, str.length());
		
		int[] codePointArray = new int[length];
		
		for (int i = 0; i < length; i++) {
			codePointArray[i] = str.codePointAt(i);
		}
		
		return codePointArray;
	}
	
	/**
	 * １つのコードポイントから文字列を作成する。
	 * @param codePoint
	 * @return
	 */
	public static String toString(int codePoint) {
		return new String(new int[] {codePoint}, 0, 1);
	}
	
}
