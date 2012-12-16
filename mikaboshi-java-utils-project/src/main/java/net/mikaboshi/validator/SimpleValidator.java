package net.mikaboshi.validator;

import java.io.File;
import java.lang.reflect.Constructor;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.mikaboshi.util.ResourceBundleWrapper;

import org.apache.commons.lang.StringUtils;

/**
 * <p>
 * 値のバリデーション（検証）を行うクラス。
 * </p><p>
 * 失敗した場合は、例外をスローする。
 * </p><p>
 * 例外のメッセージは、実行環境のロケールに合わせて設定される。
 * （日本語とデフォルトの英語のみ）
 * </p><p>
 * 各 validate* メソッドにおいて、例外クラスを指定しなかった場合（nullの場合）、
 * {@link ValidatorException} がデフォルト例外クラスとして使用される。
 * </p><p>
 * 各 validate* メソッドにおいて、指定する例外クラスは、Stringを１つ引数に持つ
 * コンストラクタがなければならない。
 * </p>
 * 
 * @author Takuma Umezawa
 */
public final class SimpleValidator {
	private SimpleValidator() {}
	
	/**
	 * バリデーションメッセージが定義されたリソースバンドルプロパティファイル。
	 */
	private static final String BUNDLE_BASE_NAME =
		"net.mikaboshi.validator.messages";
	
	private static final ResourceBundleWrapper bundle =
			new ResourceBundleWrapper(ResourceBundle.getBundle(BUNDLE_BASE_NAME));
	
	/**
	 * リソースバンドルから例外メッセージのテンプレートを取得し、
	 * パラメータを埋め込んだ文字列を返す。
	 */
	static String get(String key, Object ... arguments) {
		return bundle.format(key, arguments);
	}
	
	/**
	 * 指定された例外クラスの新しいインスタンスを生成し、スローする。
	 */
	static <T extends Throwable> void throwException(
			String message,
			Class<T> exceptionClass)
			throws T, ValidatorError {

		if (exceptionClass == null) {
			throw new ValidatorException(message);
		}
		
		T result = null;
		
		try {
			Constructor<T> constuctor =
					exceptionClass.getConstructor(String.class);
			result = exceptionClass.cast(constuctor.newInstance(message));
			
		} catch (Throwable e) {
			// リフレクション例外等
			throw new ValidatorError(e);
		}

		throw result;
	}
	
	/**
	 * Objectがnullの場合は、指定された例外クラスをスローする。
	 * 
	 * @param <T>
	 * @param object　チェック対象のオブジェクト
	 * @param name objectの名前
	 * @param exceptionClass　objectがnullの場合にスローされる例外クラス
	 * @throws T 
	 * @throws ValidatorError 例外オブジェクトの生成に失敗した場合にスローされる。
	 */
	public static <T extends Throwable> void validateNotNull(
			Object object,
			String name,
			Class<T> exceptionClass)
			throws T, ValidatorError {
		
		if (object != null) {
			return;
		}
		
		throwException(get("not_null", name), exceptionClass);
	}
	
	/**
	 * Objectがnullの場合は、NullPointerExceptionをスローする。
	 * 
	 * @param object　チェック対象のオブジェクト
	 * @param name objectの名前
	 * @throws NullPointerException Objectがnullの場合
	 * @since 1.1.5
	 */
	public static void validateNotNull(
			Object object,
			String name)
			throws NullPointerException {
		
		if (object != null) {
			return;
		}
		
		validateNotNull(object, name, NullPointerException.class);
	}
	
	/**
	 * データが１つだけ指定されているかチェックする。
	 * 
	 * @param <T>
	 * @param objects　検証対象のデータセット。キー：データ項目名、値：データ
	 * @param exceptionClass
	 * @throws T
	 * @throws ValidatorError
	 */
	public static <T extends Throwable> void validateNotNullJust1(
			Map<String, Object> objects,
			Class<T> exceptionClass) 
			throws T, ValidatorError {
		
		int count = 0;
		for (String key : objects.keySet()) {
			Object value = objects.get(key);
			if (value != null) {
				count++;
			}
		}

		if (count == 1) {
			return;
		}
		
		String message = get("not_null_just_1",
				StringUtils.join(objects.keySet(), ','));
		
		throwException(message, exceptionClass);
	}
	
	/**
	 * strに不正な文字が含まれていないかチェックする。
	 * 
	 * @param <T>
	 * @param str
	 * @param name
	 * @param invalidChars
	 * @param exceptionClass
	 * @throws T
	 * @throws ValidatorError
	 */
	public static <T extends Throwable> 
		void validateNotContainsInvalidCharactor(
			String str,
			String name,
			char[] invalidChars,
			Class<T> exceptionClass)
			throws T, ValidatorError {
		
		for (char c1 : str.toCharArray()) {
			for (char c2 : invalidChars) {
				if (c1 == c2) {
					throwException(get(
						"not_contains_invalid_charactor", name, c1),
						exceptionClass);
				}
			}
		}
	}
	
	/**
	 * 文字列データに値が指定されているかチェックする。
	 * null、空文字、半角スペース、タブ・改行等の空白文字ならば例外をスローする。
	 * 
	 * @param <T>
	 * @param str
	 * @param name
	 * @param exceptionClass
	 * @throws T
	 * @throws ValidatorError
	 */
	public static <T extends Throwable> void validateNotBlank(
			String str,
			String name,
			Class<T> exceptionClass) 
			throws T, ValidatorError {
		
		if (StringUtils.isBlank(str)) {
			throwException(get("not_blank", name), exceptionClass);
		}
	}
	
	/**
	 * 文字列データに値が指定されているかチェックする。
	 * nullまたは長さ=0ならば例外をスローする。
	 * 
	 * @param <T>
	 * @param str
	 * @param name
	 * @param exceptionClass
	 * @throws T
	 * @throws ValidatorError
	 */
	public static <T extends Throwable> void validateNotNullNorLength0(
			String str,
			String name,
			Class<T> exceptionClass) 
			throws T, ValidatorError {
		
		if (str == null || str.length() == 0) {
			throwException(get("not_null_nor_length0", name), exceptionClass);
		}
	}
	
	/**
	 * 整数値が0以上であるかチェックする。
	 * 
	 * @param <T>
	 * @param value
	 * @param name
	 * @param exceptionClass
	 * @throws T
	 * @throws ValidatorError
	 */
	public static <T extends Throwable> void validatePositiveOrZero(
			int value,
			String name,
			Class<T> exceptionClass) 
			throws T, ValidatorError {
		
		if (value < 0) {
			throwException(get("positive_or_zero", name), exceptionClass);
		}
	}
	
	/**
	 * 整数値が1以上であるかチェックする。
	 * 
	 * @param <T>
	 * @param value
	 * @param name
	 * @param exceptionClass
	 * @throws T
	 * @throws ValidatorError
	 */
	public static <T extends Throwable> void validatePositive(
			int value,
			String name,
			Class<T> exceptionClass) 
			throws T, ValidatorError {
		
		if (value < 1) {
			throwException(get("positive", name), exceptionClass);
		}
	}
	
	/** 正規表現パターンオブジェクトのキャッシュ */
	private static Map<String, Pattern> patternMap =
			new HashMap<String, Pattern>();
	
	/**
	 * 文字列が正規表現にマッチするかチェックする。
	 * 
	 * @param <T>
	 * @param string
	 * @param regex
	 * @param name
	 * @param exceptionClass
	 * @throws T
	 * @throws ValidatorError
	 */
	public static synchronized <T extends Throwable> void validatePattern(
			String string,
			String regex,
			String name,
			Class<T> exceptionClass) 
			throws T, ValidatorError {
		
		Pattern pattern = patternMap.get(string);
		
		if (pattern == null) {
			pattern = Pattern.compile(regex);
			patternMap.put(regex, pattern);
		}
		
		final Matcher matcher = pattern.matcher(string);
		
		if (!matcher.matches()) {
			throwException(get("pattern", name, string),
					exceptionClass);
		}
	}
	
	/**
	 * コレクションがnullではなく、要素が1つ以上あることを確認する。
	 * 
	 * @param <T>
	 * @param collection
	 * @param name
	 * @param exceptionClass
	 * @throws T
	 * @throws ValidatorError
	 */
	public static <T extends Throwable> void validateNotNullNorEmpty(
			Collection<?> collection,
			String name,
			Class<T> exceptionClass) 
			throws T, ValidatorError {
		
		if (collection == null || collection.isEmpty()) {
			throwException(get("not_null_nor_empty", name), exceptionClass);
		}
	}
	
	/**
	 * ファイルまたはディレクトリが存在することを確認する。
	 * 
	 * @param <T>
	 * @param file　チェック対象のファイル
	 * @param exceptionClass　fileが存在しないの場合にスローされる例外クラス
	 * @throws T 
	 * @throws ValidatorError 例外オブジェクトの生成に失敗した場合にスローされる
	 */
	public static <T extends Throwable> void validateFileExists(
			File file,
			Class<T> exceptionClass) 
			throws T, ValidatorError {
		
		if (file.exists()) {
			return;
		}
		
		throwException(
				get("file_absent", file.getAbsolutePath()), 
				exceptionClass);
	}
	
	/**
	 * 指定されたFileがファイルで（ディレクトリではなく）、読み込み可能であることを確認する。
	 * 
	 * @param <T>
	 * @param file　チェック対象のファイル
	 * @param exceptionClass　fileがファイルでないか、読み込み不可能な場合にスローされる例外クラス
	 * @throws T 
	 * @throws ValidatorError 例外オブジェクトの生成に失敗した場合にスローされる
	 * @throws NullPointerException fileまたはexceptionClassの場合にスローされる
	 */
	public static <T extends Throwable> void validateFileCanRead(
			File file,
			Class<T> exceptionClass) 
			throws T, ValidatorError {
		
		if (file.isFile() && file.canRead()) {
			return;
		}
		
		throwException(
				get("file_cannot_be_read", file.getAbsolutePath()), 
				exceptionClass);
	}

}
