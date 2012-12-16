package net.mikaboshi.property;

import static net.mikaboshi.validator.SimpleValidator.validateFileCanRead;
import static net.mikaboshi.validator.SimpleValidator.validateFileExists;
import static net.mikaboshi.validator.SimpleValidator.validateNotBlank;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

/**
 * <p>
 * プロパティファイルから読み込んだ値を、{@link Property} アノテーションの付いたメソッドに設定する。
 * </p><p>
 * プロパティファイルの読み込みは {@link Properties#load(InputStream)} を使用するため、
 * ファイルのフォーマットはこれに従う。
 * </p><p>
 * このクラスは、対象オブジェクトおよび読み込み元のファイルに対して同期を行わない。
 * </p>
 * @author Takuma Umezawa
 * @since 0.1.2
 */
public class PropertyFileLoader {

	private File propertiesFile;
	
	/**
	 * プロパティの読み込み元のファイルを設定する。
	 * @param file
	 * @throws IOException ファイルが読み込めない場合
	 */
	public PropertyFileLoader(File file) throws IOException {
		
		validateFileExists(file, IOException.class);
		validateFileCanRead(file, IOException.class);
		
		this.propertiesFile = file;
	}
		
	private String resourcePath;
	
	/**
	 * リソースパスにより指定されたファイルを、プロパティの読み込み元とする。
	 * 
	 * @param resourcePath リソースのパス（例: net/mikaboshi/test.properties）
	 * @throws IOException リソースファイルが見つからない場合
	 * @throws IllegalArgumentException resourcePathがnullまたはブランクの場合
	 */
	public PropertyFileLoader(String resourcePath) throws IOException {
		validateNotBlank(
				resourcePath, "resourcePath", IllegalArgumentException.class);
		
		if (Thread.currentThread().getContextClassLoader().
				getResource(resourcePath) == null) {
			throw new IOException(
					"resourcePath could not be found <" +
					resourcePath + ">");
		}
		
		this.resourcePath = resourcePath;
	}
	
	/**
	 * オブジェクトobjにプロパティを設定する。
	 * プロパティファイルを既に読み込んでいる場合は再読込は行わない。
	 * 
	 * @param obj プロパティを設定するオブジェクト
	 * @throws IOException プロパティファイルの読み込みに失敗した場合
	 * @throws IllegalArgumentException リフレクションによるプロパティの設定に失敗した場合
	 */
	public void load(Object obj) throws IOException {
		load(obj, false);
	}
	
	/**
	 * オブジェクトobjにプロパティを設定する。
	 * ファイルの再読込を行うかどうかは、引数refreshで指定する。
	 * 
	 * @param obj プロパティを設定するオブジェクト
	 * @param refresh ファイルを読み直す場合はtrueを指定する。
	 * 			falseを指定した場合でも、1回も読み込んでいなければ読み込みを行う。
	 * @throws IOException プロパティファイルの読み込みに失敗した場合
	 * @throws IllegalArgumentException リフレクションによるプロパティの設定に失敗した場合
	 */
	public void load(Object obj, boolean refresh) 
			throws IOException {
		
		if (refresh || this.properties == null) {
			relaodPropertyFile();
		}
		
		try {
			PropertyUtils.load(obj, this.properties);
		} catch (IllegalAccessException e) {
			throw new IllegalArgumentException("プロパティの設定失敗", e);
		} catch (InvocationTargetException e) {
			throw new IllegalArgumentException("プロパティの設定失敗", e);
		}
	}
	
	private Properties properties;
	
	/**
	 * ファイルからプロパティを読み込む。
	 * @throws IOException
	 */
	private void relaodPropertyFile() throws IOException {
		
		if (this.propertiesFile != null) {
			this.properties = loadPropertyFile(this.propertiesFile);
		} else if (this.resourcePath != null) {
			this.properties = loadPropertyFromResource(this.resourcePath);
		} else {
			throw new AssertionError(
					"both of propertiesFile and resourcePath are null.");
		}
	} 
	
	/**
	 * プロパティファイルを読み込み、新しい Properties オブジェクトに設定して返す。
	 * プロパティ項目が存在しない場合は、空の Properties オブジェクトを返す。
	 * 
	 * @param f　プロパティファイル
	 * @return　値が設定された　Properties オブジェクト
	 * @throws IOException　ファイルが存在しない場合や、プロパティファイルを読み込みに失敗した場合
	 */
	public static Properties loadPropertyFile(File f) throws IOException {
		Properties prop = new Properties();
		
		InputStream inStream = null;
		
		try {
			inStream = new BufferedInputStream(
					FileUtils.openInputStream(f));
			prop.load(inStream);
			return prop;
		} finally {
			IOUtils.closeQuietly(inStream);
		}
	}
	
	/**
	 * クラスパス下のリソース内のプロパティファイルからプロパティを読み込む。
	 * プロパティ項目が存在しない場合は、空の Properties オブジェクトを返す。
	 * 
	 * @param resourcePath リソースのパス（例: net/mikaboshi/test.properties）
	 * @return 値が設定された　Properties オブジェクト
	 * @throws IOException　ファイルが存在しない場合や、プロパティファイルを読み込みに失敗した場合
	 */
	public static Properties loadPropertyFromResource(String resourcePath)
		throws IOException {
		
		Properties prop = new Properties();
		
		InputStream inStream = null;
		
		try {
			inStream = Thread.currentThread().getContextClassLoader().
				getResourceAsStream(resourcePath);
			prop.load(inStream);
			return prop;
		} finally {
			IOUtils.closeQuietly(inStream);
		}
	}
}
