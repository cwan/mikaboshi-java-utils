package net.mikaboshi.property;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.Properties;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * <p>
 * {@link Property} アノテーションの付いたメソッドの戻り値を、ファイルに書き出す。
 * </p><p>
 * プロパティファイルの読み込みは {@link Properties#store(OutputStream, String)}
 * を使用するため、ファイルのフォーマットはこれに従う。
 * </p><p>
 * このクラスは、対象オブジェクトおよび書き出し先のファイルに対して同期を行わない。
 * </p>
 * 
 * @author Takuma Umezawa
 * @since 0.1.2
 */
public class PropertyFileStorer {

	private File propertiesFile;
	
	private static Log logger = LogFactory.getLog(PropertyFileStorer.class);
	
	/**
	 * プロパティを書き出す先のファイルを設定する。
	 * @param file
	 */
	public PropertyFileStorer(File file) {
		this.propertiesFile = file;
	}
	
	/**
	 * プロパティをファイルに書き出す。
	 * 
	 * @param obj プロパティを保存したいオブジェクト
	 * @throws IOException ファイルの書き込みに失敗した場合
	 * @throws IllegalArgumentException リフレクションによるプロパティの取得に失敗した場合
	 */
	public void store(Object obj) throws IOException {
		
		Properties properties = null;
		
		try {
			properties = PropertyUtils.store(obj);
		} catch (IllegalAccessException e) {
			throw new IllegalArgumentException("プロパティの取得失敗", e);
		} catch (InvocationTargetException e) {
			throw new IllegalArgumentException("プロパティの取得失敗", e);
		}
		
		OutputStream out = null;
		
		try {
			out = new BufferedOutputStream(
					new FileOutputStream(this.propertiesFile));
			
			properties.store(out, null);
		} finally {
			IOUtils.closeQuietly(out);
		}
	}
	
	/**
	 * JVMの終了時にプロパティをファイルに書き出す。
	 * エラーが発生した場合は、エラーログが出力される。
	 * @param obj　プロパティを保存したいオブジェクト
	 */
	public void storeOnExit(final Object obj) {
		Runtime.getRuntime().addShutdownHook(new Thread() {
			public void run() {
				try {
					store(obj);
				} catch (Exception e) {
					logger.error("プロパティファイルの書き出しに失敗しました。", e);
				}
			}
		});
	}
}
