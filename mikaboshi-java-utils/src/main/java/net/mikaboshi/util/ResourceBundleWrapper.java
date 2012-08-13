package net.mikaboshi.util;

import java.text.MessageFormat;
import java.util.Enumeration;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.Properties;
import java.util.ResourceBundle;

import net.mikaboshi.validator.SimpleValidator;

import org.apache.commons.collections.map.LRUMap;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * <p>
 * ResourceBundle を簡単に使うためのラッパークラス。
 * 例外を出さずに値を取得したり、フォーマットを行ったりする。
 * </p><p>
 * コンストラクタで指定する ResourceBundle は、データをコピーするわけではないので、
 * 後から内容が変化した場合、{@link #getString(String)} 等のメソッドの結果が
 * 変わることがある。
 * </p><p>
 * 各メソッドが ResourceBundle にアクセスする場合、ResourceBundle に対して
 * 排他制御を行う。
 * </p>
 * 
 * @author Takuma Umezawa
 * @since 0.1.2
 */
public class ResourceBundleWrapper {

	private static Log logger =
			LogFactory.getLog(ResourceBundleWrapper.class);
	
	private final ResourceBundle bundle;
	
	/**
	 * 初期化済みの ResourceBundle オブジェクトを設定する。
	 * 
	 * @param resourceBunlde
	 * @throws NullPointerException resourceBunlde が null の場合
	 */
	public ResourceBundleWrapper(ResourceBundle resourceBunlde) {
		SimpleValidator.validateNotNull(
				resourceBunlde,
				"resourceBunlde",
				NullPointerException.class);
		
		this.bundle = resourceBunlde;
	}

	/**
	 * 保持している ResourceBundle オブジェクトの getString(String) メソッドを
	 * 実行し、その戻り値を返す。
	 * ただし、key が null の場合や、リソースが見つからない場合、戻り値が String
	 * ではない場合は、例外をスローせずに null を返す。
	 * @see ResourceBundle#getString(String)
	 * @param key
	 * @return
	 */
	public String getString(String key) {
		if (key == null) {
			return null;
		}
		
		synchronized (this.bundle) {
			// ResourceBundle#containsKey は Java 1.6 からなので使わない。
			
			try {
				return this.bundle.getString(key);
			} catch (MissingResourceException e) {
				return null;
			} catch (ClassCastException e) {
				return null;
			}
		}
	}
	
	/**
	 * 保持している ResourceBundle オブジェクトの getString(String) メソッドを
	 * 実行し、その戻り値を返す。
	 * ただし、key が null の場合や、リソースが見つからない場合、戻り値が String
	 * ではない場合は、第2引数（デフォルト値）を返す。
	 * @see ResourceBundle#getString(String)
	 * @param key
	 * @param def
	 * @return
	 */
	public String getString(String key, String def) {
		String value = getString(key);
		
		if (key != null) {
			return value;
		} else {
			return def;
		}
	}
	
	/** フォーマッタのキャッシュ */
	@SuppressWarnings("unchecked")
	private static final Map<String, MessageFormat>
			FORMATTER_MAP = new LRUMap(200);
	
	/**
	 * 第一引数keyでリソースバンドルを参照し、取得した文字列ををメッセージフォーマットと解釈し、
	 * 第二引数以下をパラメータとして埋め込んだ文字列を返す。
	 * keyがリソースバンドルに存在しない場合は、ログに警告を出力し、keyをそのまま返す。
	 * keyがnullの場合は、文字列"null"を返す。
	 * パラメータの埋め込みで例外が発生した場合、ログに警告ーを出力し、
	 * パラメータを埋め込む前のフォーマット文字列を返す。
	 * フォーマットの方式は、{@link MessageFormat} に従う。
	 * 
	 * @see MessageFormat
	 * @param key
	 * @param args
	 * @return
	 */
	public String format(String key, Object ... args) {
		
		String pattern = getString(key);
		
		if (pattern == null) {
			if (logger.isWarnEnabled()) {
				StringBuilder sb = new StringBuilder();
				sb.append("undefined message key : <");
				sb.append(key);
				sb.append(">");
				logger.warn(sb.toString());
			}
			
			return String.valueOf(key);
		}
		
		try {
			MessageFormat formatter = null;
			
			synchronized (FORMATTER_MAP) {
				// フォーマッタのキャッシュがあればそれを使う。なければ生成する。
				formatter = FORMATTER_MAP.get(pattern);
				
				if (formatter == null) {
					formatter = new MessageFormat(pattern);
					FORMATTER_MAP.put(pattern, formatter);
				}
			}
			
			synchronized (formatter) {
				return formatter.format(args);
			}
			
		} catch (IllegalArgumentException e) {
			if (logger.isWarnEnabled()) {
				StringBuilder sb = new StringBuilder();
				sb.append("message format failed.");
				sb.append(key);
				sb.append(" {");
				sb.append(StringUtils.join(args, ", "));
				sb.append("}");
				
				logger.warn(sb.toString() , e);
			}
			
			return pattern;
		}
	}
	
	/**
	 * 保持している ResourceBundle のデータを新しい Properties オブジェクトに
	 * 設定して返す。
	 * 
	 * @return
	 */
	public Properties toProperties() {
		Properties properties = new Properties();
		
		synchronized (this.bundle) {
			for (Enumeration<?> enm = this.bundle.getKeys(); enm.hasMoreElements();) {
				String key = (String) enm.nextElement();
				properties.put(key, this.bundle.getString(key));
			}
		}
		
		return properties;
	}
}
