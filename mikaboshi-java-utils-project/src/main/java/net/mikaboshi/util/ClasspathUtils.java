package net.mikaboshi.util;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * <p>
 * クラスパスに関するユーティリティクラス。
 * </p>
 *
 * @author Takuma Umezawa
 * @since 1.1.0
 */
public class ClasspathUtils {

	/** ディレクトリの区切り文字（OS依存） */
	private final static String DIR_SEPARATOR = String.valueOf(IOUtils.DIR_SEPARATOR);

	/** クラスパスの区切り文字（OS依存） */
	private final static String PATH_SEPARATOR = System.getProperty("path.separator");

	/** パッケージの区切り文字 */
	private final static String PACKAGE_SEPARATOR = ".";

	private static final Log logger = LogFactory.getLog(ClasspathUtils.class);

	private final String[] classPaths;

	/**
	 * 実行時のクラスパスを適用する。
	 */
	public ClasspathUtils () {
		this(System.getProperty("java.class.path"));
	}

	/**
	 * クラスパスを明示的に指定する。
	 * @param classPath クラスパス。
	 *   対象の jar/zip ファイルやディレクトリパスを結合した文字列。
	 *   形式は OS 依存 （Windows ならば「;」区切り、Linux/Unix ならば「:」区切り）。
	 */
	public ClasspathUtils(String classPath) {

		if (classPath == null) {
			throw new NullPointerException("argument 'classPath' is null.");
		}

		this.classPaths = StringUtils.split(classPath, PATH_SEPARATOR);
	}

	/**
	 * クラスパスを明示的に指定する。
	 * @param classPaths クラスパスの配列
	 */
	public ClasspathUtils(String[] classPaths) {
		if (classPaths == null) {
			throw new NullPointerException("argument 'classPaths' is null.");
		}

		this.classPaths = classPaths;
	}

	/**
	 * <p>
	 * クラスパスから、引数で指定された条件に一致する全てのクラス名のセットを取得する。
	 * </p>
	 * <p>
	 * <ul>
	 * 	<li>noInnerClass が true の場合、内部クラス（ローカルクラス、メンバークラス、匿名クラス、合成クラス）は対象外となる</li>
	 *  <li>include を指定した場合、そのパターンにマッチしないクラスは対象外となる</li>
	 *  <li>exclude を指定した場合、そのパターンにマッチするクラスは対象外となる</li>
	 * </ul>
	 * </p>
	 * <p>
	 * noInnerClass を true に指定した場合、対象のクラスはロードされ、内部クラスかどうかチェックされる。
	 * そのため、大量にヒープ領域を消費することになり、{@link OutOfMemoryError} が発生するがある。
	 * <br>
	 * また、このクラスロード時に発生するエラー（{@link ClassNotFoundException}、
	 * {@link NoClassDefFoundError}、{@link UnsatisfiedLinkError}）は、
	 * 警告ログに出力される。
	 * </p>
	 *
	 * @param include 含むパターン（正規表現）。指定しない場合はnullにする。
	 * @param exclude 含まないパターン（正規表現）。指定しない場合はnullにする。
	 * @param noInnerClass 内部クラス（ローカルクラス、メンバークラス、匿名クラス）を除くかどうか
	 * @return クラス名（FQCN）のセット
	 * @throws IOException クラスファイルの読み込みに失敗した場合
	 */
	public Set<String> getClassNames(
			String include,
			String exclude,
			boolean noInnerClass) throws IOException {

		Pattern includePattern = createPattern(include);
		Pattern excludePattern = createPattern(exclude);

		Set<String> result = new HashSet<String>();

		for (String className : getClassNames()) {

			if (includePattern != null) {
				if (!includePattern.matcher(className).matches()) {
					continue;
				}
			}

			if (excludePattern != null) {
				if (excludePattern.matcher(className).matches()) {
					continue;
				}
			}

			if (noInnerClass) {

				try {
					Class<?> clazz = Class.forName(className);

					if (clazz.isMemberClass() || clazz.isLocalClass() || clazz.isAnonymousClass() || clazz.isSynthetic()) {
						continue;
					}

				} catch (ClassNotFoundException e) {
					if (logger.isWarnEnabled()) {
						logger.warn("ClassNotFound : " + className);
					}
					continue;
				} catch (NoClassDefFoundError e) {
					if (logger.isWarnEnabled()) {
						logger.warn("NoClassDefFound : " + className);
					}
					continue;
				} catch (UnsatisfiedLinkError e) {
					if (logger.isWarnEnabled()) {
						logger.warn("UnsatisfiedLinkError : " + className);
					}
					continue;
				}
			}

			result.add(className);
		}

		return result;
	}

	/**
	 *
	 * @param regex
	 * @return
	 */
	private static Pattern createPattern(String regex) {
		if (regex == null) {
			return null;
		}

		if (!regex.startsWith("^") && !regex.startsWith(".*")) {
			regex = ".*" + regex;
		}

		if (!regex.endsWith("$") && !regex.endsWith(".*")) {
			regex += ".*";
		}

		return Pattern.compile(regex);
	}

	/**
	 * クラスパスから、全てのクラス名のセットを取得する。
	 * @return クラス名（FQCN）のセット
	 * @throws IOException　クラスファイルの読み込みに失敗した場合
	 */
	public Set<String> getClassNames() throws IOException {

		Set<String> classNames = new HashSet<String>();

		for (String path : this.classPaths) {
			File f = new File(path);

			if (!f.exists()) {
				if (logger.isWarnEnabled()) {
					logger.warn("target classpath '" + path + "' does not exist.");
				}

				continue;
			}

			if (f.isDirectory()) {
				// クラスディレクトリの中を探す
				searchDirectory(f, classNames);
			} else {
				// jar/zipとして解釈する
				searchArchive(f, classNames);
			}
		}

		return classNames;
	}

	/**
	 * ディレクトリの中から*.classファイルを探し、クラス名してセットに追加する
	 * @param dir
	 * @param classNames
	 */
	private static void searchDirectory(File dir, Set<String> classNames) {

		List<File> fileList =
			(List<File>) FileUtils.listFiles(dir, new String[] {"class"}, true);

		for (File file : fileList) {
			String relativePath =
				file.getAbsolutePath().substring(dir.getAbsolutePath().length() + 1);

			if (!file.isFile()) {
				continue;
			}

			if (!relativePath.endsWith(".class")) {
				continue;
			}

			if (relativePath.contains("-")) {
				// package-info等を除く
				continue;
			}

			// 拡張子除去
			String noExt = relativePath
					.replaceAll("\\.class$", "");

			// 区切り文字変換
			String className = StringUtils.replace(
					noExt, DIR_SEPARATOR, PACKAGE_SEPARATOR);

			classNames.add(className);
		}
	}

	/**
	 * jar/zip の中から *.class ファイルを探し、クラス名してセットに追加する。
	 * @param file
	 * @param classNames
	 * @throws IOException
	 * @throws FileNotFoundException
	 */
	private static void searchArchive(File file, Set<String> classNames) throws FileNotFoundException, IOException {
		JarInputStream jis = null;

		try {
			jis = new JarInputStream(new BufferedInputStream(
					new FileInputStream(file)));

			JarEntry entry;
			while ((entry = jis.getNextJarEntry()) != null) {

				String name = entry.getName();

				if (!name.endsWith(".class")) {
					continue;
				}

				if (name.contains("-")) {
					// package-info等を除く
					continue;
				}

				String noExt =
						name.replaceAll("\\.class$", "");

				// 区切り文字変換（Windowsでも「/」になる）
				String className = StringUtils.replace(
						noExt, "/", PACKAGE_SEPARATOR);

				classNames.add(className);
			}
		} finally {
			IOUtils.closeQuietly(jis);
		}
	}


}
