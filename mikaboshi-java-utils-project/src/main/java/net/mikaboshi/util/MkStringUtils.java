package net.mikaboshi.util;

import net.mikaboshi.validator.SimpleValidator;

/**
 * 文字列に関するユーティリティクラス。
 * @author Takuma Umezawa
 * @since 0.1.2
 */
public final class MkStringUtils {

	private MkStringUtils() {
	}
	
	/**
	 * 文字列を指定文字数ずつに分割する。
	 * 
	 * @param str 分割対象の文字列
	 * @param size 文字数 (1以上)
	 * @return 分割した文字列の配列
	 * @throws NullPointerException strがnullの場合
	 * @throws IllegalArgumentException sizeが1未満の場合
	 */
	public static String[] slice(String str, int size) {
		SimpleValidator.validateNotNull(str, "str", 
				NullPointerException.class);
		SimpleValidator.validatePositive(size, "size", 
				IllegalArgumentException.class);
		
		if (str.length() == 0) {
			return new String[] {""};
		}
		
		final int arraySize = (str.length() - 1) / size + 1;
		
		final String[] result = new String[arraySize];
		
		for (int i = 0; i < arraySize; i++) {
			int beginIndex = size * i;
			int endIndex = size * (i + 1);
			
			if (endIndex > str.length()) {
				result[i] = str.substring(beginIndex);
			} else {
				result[i] = str.substring(beginIndex, endIndex);
			}
		}
		
		return result;
	}
	
	/**
	 * String配列の要素をすべてtrimする。
	 * @param array
	 * @throws NullPointerException arrayがnullの場合
	 */
	public static void trimAll(String[] array) {
		SimpleValidator.validateNotNull(array, "array", 
				NullPointerException.class);
		
		for (int i = 0; i < array.length; i++) {
			array[i] = array[i].trim();
		}
	}
	
	/**
	 * 末尾のnum文字を取り除く
	 * @param str
	 * @param num
	 * @return
	 * @throws NullPointerException strがnullの場合
	 */
	public static String cutTail(String str, int num) {
		SimpleValidator.validateNotNull(str, "str", 
				NullPointerException.class);
		
		if (str.length() < num) {
			return "";
		}
		
		return str.substring(0, str.length() - num);
	}
	

	private static final char[] HTML_ESCAPE_BEFORE_CHARS = {
										'&',
										'<',
										'>',
										'"',
										'\'' };
	
	private static final char[][] HTML_ESCAPE_AFTER_STRINGS = {
										"&amp;".toCharArray(),
										"&lt;".toCharArray(),
										"&gt;".toCharArray(),
										"&quot;".toCharArray(),
										"&#39;".toCharArray() };
	
	private static final int HTML_ESCAPE_CHAR_LENGTH = HTML_ESCAPE_BEFORE_CHARS.length;
	
	/**
	 * <p>
	 * HTMLの特殊文字をエスケープする。
	 * </p>
	 * <p>
	 * 正規表現で5回置換するよりは4倍ぐらい速い。
	 * </p>
	 * <table border="1">
	 * 	<tr><th>置換前</th><th>置換後</th></tr>
	 *  <tr><td>&amp;</td><td>&amp;amp;</td><tr>
	 *  <tr><td>&lt;</td><td>&amp;lt;</td><tr>
	 *  <tr><td>&gt;</td><td>&amp;gt;</td><tr>
	 *  <tr><td>&quot;</td><td>&amp;quot;</td><tr>
	 *  <tr><td>&#39;</td><td>&amp;#39;</td><tr>
	 * </table>
	 * 
	 * @param string
	 * @return エスケープ後の文字列。引数がnullの場合はnullを返す。
	 * @since 1.1.7
	 */
	public static String escapeHtml(String string) {
		
		if (string == null) {
			return null;
		}

		// エスケープ対象文字はサロゲートペアには含まれないので、考慮は不要（多分）

		StringBuilder buf = new StringBuilder( (int)((double) string.length() * 1.5D) );
		
		for (char c : string.toCharArray()) {
			
			boolean found = false;
			
			for (int i = 0; i < HTML_ESCAPE_CHAR_LENGTH; i++) {
				if (c == HTML_ESCAPE_BEFORE_CHARS[i]) {
					buf.append(HTML_ESCAPE_AFTER_STRINGS[i]);
					found = true;
					break;
				}
			}
			
			if (!found) {
				buf.append(c);
			}
		}
		
		return buf.toString();
	}
	
}
