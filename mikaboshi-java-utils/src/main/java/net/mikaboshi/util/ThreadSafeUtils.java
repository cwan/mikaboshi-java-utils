package net.mikaboshi.util;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.WeakHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.apache.commons.collections.map.LRUMap;

import net.mikaboshi.validator.SimpleValidator;

/**
 * スレッドセーフなユーティリティクラス。
 * @author Takuma Umezawa
 * @since 1.1.2
 */
public final class ThreadSafeUtils {
	
	@SuppressWarnings("unchecked")
	private static final Map<String, Pattern> patternCache = new LRUMap(200);
	
	private static final ThreadLocal<Map<String, SimpleDateFormat>> dateFormatLocal
		= new ThreadLocal<Map<String,SimpleDateFormat>>();
	
	private static final ThreadLocal<Map<String, DecimalFormat>> numberFormatLocal
		= new ThreadLocal<Map<String,DecimalFormat>>();
	
	public ThreadSafeUtils() {
	}
	
	/**
	 * 正規表現のチェックし、マッチ部分の配列を返す。
	 * 
	 * @see Pattern
	 * @see Matcher
	 * @param input 対象文字列
	 * @param regex 正規表現文字列
	 * @return inputがregexにマッチした場合、マッチ部分の配列を返す。マッチしない場合はnullを返す。
	 * @throws PatternSyntaxException 正規表現の文法誤り
	 */
	public static String[] match(String input, String regex) {
		
		SimpleValidator.validateNotNull(input, "input");

		Pattern pattern = getPattern(regex);
		
		Matcher matcher = pattern.matcher(input);
		
		if (matcher.matches()) {
			String[] result = new String[matcher.groupCount()];
			
			for (int i = 0; i < result.length; i++) {
				result[i] = matcher.group(i + 1);
			}
			
			return result;
			
		} else {
			return null;
		}
	}
	
	/**
	 * 正規表現のチェックを行う。
	 * 
	 * @see Pattern
	 * @see Matcher
	 * @param input 対象文字列
	 * @param regex 正規表現文字列
	 * @return inputがregexにマッチした場合、trueを返す。
	 * @throws PatternSyntaxException 正規表現の文法誤り
	 */
	public static boolean matches(String input, String regex) {
		
		SimpleValidator.validateNotNull(input, "input");
		
		return getPattern(regex).matcher(input).matches();
	}
	
	/**
	 * 正規表現のチェックを行う。
	 * 
	 * @see Pattern
	 * @see Matcher
	 * @param input 対象文字列
	 * @param regex 正規表現文字列
	 * @param partial 部分一致で比較するならばtrueを指定
	 * @param caseSensitive 大文字/小文字を区別するならばtrueを指定
	 * @return inputがregexにマッチした場合、trueを返す。
	 * @throws PatternSyntaxException 正規表現の文法誤り
	 * @since 1.1.5
	 */
	public static boolean matches(
			String input, 
			String regex, 
			boolean partial, 
			boolean caseSensitive) {
		
		SimpleValidator.validateNotNull(input, "input");
		SimpleValidator.validateNotNull(regex, "regex");
		
		String pattern = buildPatternString(regex, partial, caseSensitive); 
		
		return getPattern(pattern).matcher(input).matches();
	}
	
	/**
	 * 正規表現の文法エラーをチェックする。
	 * @param regex
	 * @param partial
	 * @param caseSensitive
	 * @return
	 * @throws PatternSyntaxException パターンが不正の場合 
	 * @since 1.1.5
	 */
	public static void checkValidPattern(
			String regex, 
			boolean partial, 
			boolean caseSensitive)
			throws PatternSyntaxException {
		
		String pattern = buildPatternString(regex, partial, caseSensitive);
		getPattern(pattern);
	}
	
	/**
	 * 正規表現の文法エラーをチェックする。
	 * @param regex
	 * @return
	 * @throws PatternSyntaxException パターンが不正の場合
	 * @since 1.1.5
	 */
	public static void checkValidPattern(String regex) throws PatternSyntaxException {
		checkValidPattern(regex, false, false);
	}
	
	/**
	 * 正規表現パターン文字列を組み立てる。
	 * @param regex ベースとなる正規表現パターン
	 * @param partial 部分一致にするかどうか
	 * @param caseSensitive 大文字/小文字を区別するかどうか
	 * @return
	 * @since 1.1.5
	 */
	public static String buildPatternString(
			String regex, 
			boolean partial, 
			boolean caseSensitive) {
		
		StringBuilder sb = new StringBuilder();
		
		if (!caseSensitive) {
			sb.append("(?i)");
		}
		
		if (partial) {
			if (!regex.startsWith("^")) {
				sb.append(".*").append(regex);
			} else {
				sb.append(regex);
			}
			
			if (!regex.endsWith("$")) {
				sb.append(".*");
			}
		} else {
			sb.append(regex);
		}
		
		return sb.toString();
	}
	
	private static Pattern getPattern(String regex) {
		
		SimpleValidator.validateNotNull(regex, "regex");
		
		Pattern pattern = null;
		
		synchronized (patternCache) {
			pattern = patternCache.get(regex);
		}
		
		if (pattern == null) {
			pattern = Pattern.compile(regex);
			
			synchronized (patternCache) {
				patternCache.put(regex, pattern);
			}
		}
		
		return pattern;
	}
	
	/**
	 * 日付のフォーマットを行う。
	 * @see SimpleDateFormat#format(Date)
	 * @param date 
	 * @param pattern
	 * @return
	 */
	public static String formatDate(Date date, String pattern) {
		
		SimpleValidator.validateNotNull(pattern, "pattern");
		
		Map<String, SimpleDateFormat> cache = dateFormatLocal.get();
		
		if (cache == null) {
			cache = new WeakHashMap<String, SimpleDateFormat>();
			dateFormatLocal.set(cache);
		}
		
		SimpleDateFormat formatter = cache.get(pattern);
		
		if (formatter == null) {
			formatter = new SimpleDateFormat(pattern);
			cache.put(pattern, formatter);
		}
		
		return formatter.format(date);
	}
	
	/**
	 * 日付のフォーマットを行う。
	 * @see SimpleDateFormat#format(Date)
	 * @param date 
	 * @param pattern
	 * @param locale
	 * @param timeZone
	 * @return
	 */
	public static String formatDate(Date date, String pattern, Locale locale, TimeZone timeZone) {
		
		SimpleValidator.validateNotNull(pattern, "pattern");
		SimpleValidator.validateNotNull(locale, "locale");
		SimpleValidator.validateNotNull(timeZone, "timeZone");
		
		Map<String, SimpleDateFormat> cache = dateFormatLocal.get();
		
		if (cache == null) {
			cache = new WeakHashMap<String, SimpleDateFormat>();
			dateFormatLocal.set(cache);
		}
		
		String key = new StringBuilder()
			.append(pattern)
			.append((char) 1)
			.append(locale.toString())
			.append((char) 1)
			.append(timeZone.getID())
			.toString();
		
		SimpleDateFormat formatter = cache.get(key);
		
		if (formatter == null) {
			formatter = new SimpleDateFormat(pattern, locale);
			formatter.setTimeZone(timeZone);
			cache.put(key, formatter);
		}
		
		return formatter.format(date);
	}
	
	/**
	 * 浮動小数点数のフォーマットを行う。
	 * 
	 * @see DecimalFormat#format(double)
	 * @param number
	 * @param pattern
	 * @return
	 */
	public static String formatNumber(double number, String pattern) {
		
		return getDecimalFormat(pattern).format(number);
	}
	
	/**
	 * 整数のフォーマットを行う。
	 * 
	 * @see DecimalFormat#format(long)
	 * @param number
	 * @param pattern
	 * @return
	 */
	public static String formatNumber(long number, String pattern) {
		
		return getDecimalFormat(pattern).format(number);
	}
	
	/**
	 * 数値のフォーマットを行う。
	 * 
	 * @see DecimalFormat#format(Object)
	 * @param number
	 * @param pattern
	 * @return
	 */
	public static String formatNumber(Object number, String pattern) {
		
		return getDecimalFormat(pattern).format(number);
	}
	
	private static DecimalFormat getDecimalFormat(String pattern) {
		
		SimpleValidator.validateNotNull(pattern, "pattern");
		
		Map<String, DecimalFormat> cache = numberFormatLocal.get();
		
		if (cache == null) {
			cache = new WeakHashMap<String, DecimalFormat>();
			numberFormatLocal.set(cache);
		}
		
		DecimalFormat formatter = cache.get(pattern);
		
		if (formatter == null) {
			formatter = new DecimalFormat(pattern);
			cache.put(pattern, formatter);
		}
		
		return formatter;
	}
}
