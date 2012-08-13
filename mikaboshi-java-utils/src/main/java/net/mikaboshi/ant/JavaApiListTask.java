package net.mikaboshi.ant;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;

import net.mikaboshi.csv.CSVStrategy;
import net.mikaboshi.csv.StandardCSVStrategy;
import net.mikaboshi.util.ClasspathUtils;
import net.mikaboshi.util.ResourceBundleWrapper;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.Path;
import org.apache.tools.ant.types.Reference;

/**
 * Java のクラス、メソッド、定数の一覧を出力する Ant タスク。
 * 
 * @author Takuma Umezawa
 * @since 1.1.0
 */
public class JavaApiListTask extends Task {
	
	private final M17NTaskLogger logger;
	
	public JavaApiListTask() {
		super();
		
		this.logger = new M17NTaskLogger(
				this, AntConstants.LOG_MESSAGE_BASE_NAME);
		
	}
	
	private boolean innerClass = false;
	
	/**
	 * 内部クラス（ローカルクラス、メンバークラス、匿名クラス、合成クラス）を出力対象とするかどうか。
	 * 省略時は false （出力対象としない）
	 * @param innerClass 内部クラスを出力対象とするならば true。
	 */
	public void setInnerClass(boolean innerClass) {
		this.innerClass = innerClass;
	}
	
	
	private String classNameInclude = null;
	
	/**
	 * 出力対象とするクラスの名前（FQCN）の正規表現パターンを指定する。
	 * 省略時は、classNameExclude で指定されたものを除く全てのクラスを出力対象とする。
	 * @param classNameInclude クラス名（FQCN）の正規表現パターン。
	 */
	public void setClassNameInclude(String classNameInclude) {
		this.classNameInclude = classNameInclude;
	}
	
	private String classNameExclude = null;
	
	/**
	 * 出力対象から除くクラスの名前（FQCN）の正規表現パターンを指定する。
	 * 省略時は、classNameInclude に一致する全てのクラスを出力対象とする。
	 * @param classNameExclude クラス名（FQCN）の正規表現パターン
	 */
	public void setClassNameExclude(String classNameExclude) {
		this.classNameExclude = classNameExclude;
	}
	
	private File output = null;
	
	/**
	 * 実行結果である API 一覧ファイル（CSV形式）の出力先のパスを指定する。
	 * 省略時は、コンソールに出力される。
	 * 生成先のディレクトリが存在しない場合、自動的に生成される。
	 * @param path 出力先ファイルパス
	 */
	public void setOutput(String path) {
		if (StringUtils.isBlank(path)) {
			throw new BuildException("output is blank");
		}
		
		this.output = new File(path);
		
		// ディレクトリの生成
		if (this.output.getParent() != null) {
			try {
				FileUtils.forceMkdir(new File(this.output.getParent()));
			} catch (IOException e) {
				throw new BuildException(e);
			}
		}
	}
	
	private String charset = Charset.defaultCharset().name();
	
	/**
	 * 出力ファイルの文字セットを設定する。
	 * 省略時は、システムデフォルトが適用される。
	 * output属性を指定していない場合、この属性は無視される。
	 * @param charset 文字セット
	 */	
	public void setCharset(String charset) {
		this.charset = charset;
	}
	
	/**
	 * 出力ファイルの文字セットを取得する。
	 * @return
	 */
	protected String getCharset() {
		return this.charset;
	}
	
	private String delimiter = ",";
	
	/**
	 * 出力される CSV データの区切り文字を指定する。
	 * 省略時は、「,」が使用される。
	 * @param delimiter CSV の区切り文字
	 */
	public void setDelimiter(String delimiter) {
		this.delimiter = delimiter;
	}
	
	private Path classpath = null;
	
	/**
	 * クラスパスをネスト要素で設定する。
	 * @param classpath
	 */
	public void setClasspath(Path classpath) {
		this.classpath = classpath;
	}
	
	/**
	 * クラスパスを生成する。
	 * @return
	 */
	public Path createClasspath() {
		if (this.classpath == null) {
			this.classpath = new Path(getProject());
		}
		return this.classpath.createPath();
	}
	
	/**
	 * クラスパス参照を設定する。
	 * @param r
	 */
	public void setClasspathRef(Reference r) {
		createClasspath().setRefid(r);
	}
	
	private boolean publicMethod = false;
	
	/**
	 * public スコープのメソッドを出力するかどうかを指定する。
	 * 省略時は、false （出力しない）。
	 * @param publicMethod 出力するならば true。 
	 */
	public void setPublicMethod(boolean publicMethod) {
	    this.publicMethod = publicMethod;
	}
	
	private boolean protectedMethod = false;
	
	/**
	 * protected スコープのメソッドを出力するかどうかを指定する。
	 * 省略時は、false （出力しない）。
	 * @param protectedMethod 出力するならば true。 
	 */
	public void setProtectedMethod(boolean protectedMethod) {
	    this.protectedMethod = protectedMethod;
	}
	
	private boolean packagePrivateMethod = false;
	
	/**
	 * package private（アクセス修飾子なし）スコープのメソッドを出力するかどうかを指定する。
	 * 省略時は、false （出力しない）。
	 * @param packagePrivateMethod 出力するならば true。 
	 */
	public void setPackagePrivateMethod(boolean packagePrivateMethod) {
	    this.packagePrivateMethod = packagePrivateMethod;
	}

	private boolean privateMethod = false;
	
	/**
	 * private スコープのメソッドを出力するかどうかを指定する。
	 * 省略時は、false （出力しない）。
	 * @param privateMethod 出力するならば true。 
	 */
	public void setPrivateMethod(boolean privateMethod) {
	    this.privateMethod = privateMethod;
	}
	
	private boolean publicField = false;
	
	/**
	 * 定数（public スコープ） フィールドを出力するかどうかを指定する。
	 * 省略時は、false （出力しない）。
	 * @param publicField 出力するならば true。 
	 */
	public void setPublicField(boolean publicField) {
	    this.publicField = publicField;
	}
	
	private CSVStrategy csvStrategy = null;
	
	/**
	 * CSV の出力を行うオブジェクトを取得する。
	 * @return
	 */
	protected CSVStrategy getCSVStragegy() {
		if (this.csvStrategy == null) {
			this.csvStrategy = new StandardCSVStrategy();
			this.csvStrategy.setDelimiter(delimiter);
		}
		
		return this.csvStrategy;
	}
	
	private PrintWriter writer = null;
	
	/**
	 * クラスパスを解析し、API 一覧を出力する。
	 */
	@Override
	public void execute() throws BuildException {
		
		ClasspathUtils classpathUtils = null;
		
		if (this.classpath != null) {
			classpathUtils = new ClasspathUtils(this.classpath.list());
		} else {
			// クラスパスが指定されていないときは、実行環境のクラスパスを適用する
			classpathUtils = new ClasspathUtils();
		}
		
		OutputStream os = null;
		
		try {
			// 出力先の設定
			if (this.output != null) {
				os = FileUtils.openOutputStream(this.output);
				this.writer = new PrintWriter(new OutputStreamWriter(os, getCharset()));
				
				this.logger.info("file.output", this.output.getCanonicalPath());
				
			} else {
				os = System.out;
				this.writer = new PrintWriter(new OutputStreamWriter(os));
			}
			
			// クラス名一覧の取得
			Set<String> classNames = classpathUtils.getClassNames(
					this.classNameInclude, this.classNameExclude, !this.innerClass);
			
			// ヘッダ出力
			printHeader();
			
			// 各行出力
			for (String className : classNames) {
				printClass(className);
			}
			
		} catch (Throwable e) {
			// NoClassDefFoundError 等がスローされる可能性があるので、Throwable でキャッチする
			throw new BuildException(e);
		} finally {
			IOUtils.closeQuietly(this.writer);
			IOUtils.closeQuietly(os);
		}
	}
	
	/**
	 * ヘッダ行を出力する。
	 */
	protected void printHeader() {
		
		List<String> data = new ArrayList<String>();
		
		data.add(getLabel("column.class.access_modifier"));
		data.add(getLabel("column.class.final"));
		data.add(getLabel("column.class.static"));
		data.add(getLabel("column.class.type"));
		data.add(getLabel("column.class.inner_type"));
		data.add(getLabel("column.class.package"));
		data.add(getLabel("column.class.name"));
		
		if (existsMemberColumns()) {
			data.add(getLabel("column.member.type"));
			data.add(getLabel("column.member.access_modifier"));
			data.add(getLabel("column.member.abstract"));
			data.add(getLabel("column.member.final"));
			data.add(getLabel("column.member.native"));
			data.add(getLabel("column.member.static"));
			data.add(getLabel("column.member.strictfp"));
			data.add(getLabel("column.member.transient"));
			data.add(getLabel("column.member.sync"));
			data.add(getLabel("column.member.class"));
			data.add(getLabel("column.member.name"));
			data.add(getLabel("column.member.parameter"));		
		}
		
		getCSVStragegy().printLine(
				data.toArray(new String[data.size()]), 
				this.writer);
	}
	
	/**
	 * メンバー列を出力するかどうか
	 * @return
	 */
	private boolean existsMemberColumns() {
		return this.publicMethod
			|| this.protectedMethod 
			|| this.packagePrivateMethod 
			|| this.privateMethod 
			|| this.publicField;
	}
	
	/** bool列が当てはまる場合に出力する文字列 */
	private static final String APPLY = "Y";
	
	/** bool列が当てはまらない場合に出力する文字列 */
	private static final String NOT_APPLY = "N";
	
	/** bool列が評価対象外の場合に出力する文字列 */
	private static final String NOT_APPLICABLE = StringUtils.EMPTY;
	
	/** package private のクラス・メソッドのアクセススコープとして出力する文字列 */
	private static final String PACKAGE_PRIVATE_SCOPE = StringUtils.EMPTY;
	
	/**
	 * １つのクラスについて出力を行う。
	 * @param className
	 * @throws ClassNotFoundException
	 * @throws NoClassDefFoundError
	 * @throws UnsatisfiedLinkError
	 */
	protected void printClass(String className) 
			throws ClassNotFoundException, NoClassDefFoundError, UnsatisfiedLinkError {
		
		Class<?> clazz = Class.forName(className);
		int mod = clazz.getModifiers();
		
		List<String> data = new ArrayList<String>();
		
		// アクセス修飾子
		if (Modifier.isPublic(mod)) {
			data.add("public");
		} else if (Modifier.isProtected(mod)) {
			data.add("protected");
		} else if (Modifier.isPrivate(mod)) {
			data.add("protected");
		} else {
			data.add(PACKAGE_PRIVATE_SCOPE);
		}
		
		// final
		data.add(Modifier.isFinal(mod) ? APPLY : NOT_APPLY);
		
		// static
		data.add(Modifier.isStatic(mod) ? APPLY : NOT_APPLY);
		
		// クラス種別
		if (clazz.isAnnotation()) {
			data.add(getLabel("column.class.type.annotation"));
		} else if (clazz.isEnum()) {
			data.add(getLabel("column.class.type.enum"));
		} else if (clazz.isInterface()) {
			data.add(getLabel("column.class.type.interface"));
		} else if (Modifier.isAbstract(mod)) {
			data.add(getLabel("column.class.type.abstract"));
		} else {
			data.add(NOT_APPLICABLE);
		}
		
		// 内部クラス種別
		if (clazz.isLocalClass()) {
			data.add(getLabel("column.class.inner_type.local"));
		} else if (clazz.isMemberClass()) {
			data.add(getLabel("column.class.inner_type.member"));
		} else if (clazz.isAnonymousClass()) {
			data.add(getLabel("column.class.inner_type.anonymous"));
		} else if (clazz.isSynthetic()) {
			data.add(getLabel("column.class.inner_type.synthetic"));
		} else {
			data.add(NOT_APPLICABLE);
		}
		
		// パッケージ名
		data.add(clazz.getPackage().getName());
		
		// クラス名
		data.add(clazz.getSimpleName());
		
		getCSVStragegy().printLine(
				data.toArray(new String[data.size()]), 
				this.writer);
		
		if (!existsMemberColumns()) {
			return;
		}
		
		printMethods(clazz, data.size());
		
		printFields(clazz, data.size());
	}
	
	/**
	 * １つのクラスに属するメソッドを出力する。
	 * @param clazz
	 * @param classItemSize
	 */
	protected void printMethods(Class<?> clazz, int classItemSize) {
		
		for (Method method : clazz.getDeclaredMethods()) {
			
			List<String> data = new ArrayList<String>();
			
			// クラス項目分、空の列を挿入
			for (int i = 0; i < classItemSize; i++) {
				data.add(StringUtils.EMPTY);
			}
			
			// メンバー種別
			data.add(getLabel("column.member.type.method"));
			
			// アクセス識別子
			int mod = method.getModifiers();
			
			if (Modifier.isPublic(mod)) {
				if (!this.publicMethod) {
					continue;
				}
				data.add("public");
				
			} else 	if (Modifier.isProtected(mod)) {
				if (!this.protectedMethod) {
					continue;
				}
				data.add("protected");
				
			} else 	if (Modifier.isPrivate(mod)) {
				if (!this.privateMethod) {
					continue;
				}
				data.add("private");
				
			} else {
				if (!this.packagePrivateMethod) {
					continue;
				}
				data.add(PACKAGE_PRIVATE_SCOPE);
			}
			
			// abstract
			data.add(Modifier.isAbstract(mod) ? APPLY : NOT_APPLY);
			
			// final
			data.add(Modifier.isFinal(mod) ? APPLY : NOT_APPLY);
			
			// native
			data.add(Modifier.isNative(mod) ? APPLY : NOT_APPLY);
			
			// static
			data.add(Modifier.isStatic(mod) ? APPLY : NOT_APPLY);
			
			// strictfp
			data.add(Modifier.isStrict(mod) ? APPLY : NOT_APPLY);
			
			// transient
			data.add(NOT_APPLICABLE);
			
			// synchronized
			data.add(Modifier.isSynchronized(mod) ? "synchronized" : NOT_APPLICABLE);
			
			// 型
			if (method.getReturnType().getCanonicalName() != null) {
				data.add(method.getReturnType().getCanonicalName());
			} else {
				data.add(method.getReturnType().getName());
			}
			
			// 名前
			data.add(method.getName());
			
			// 引数
			StringBuilder paramTypes = new StringBuilder();
			
			for (Class<?> c : method.getParameterTypes()) {
				if (paramTypes.length() != 0) {
					paramTypes.append(", ");
				}
				
				if (c.getCanonicalName() != null) {
					paramTypes.append(c.getCanonicalName());
				} else {
					paramTypes.append(c.getName());
				}
			}
			
			data.add(paramTypes.toString());
			
			getCSVStragegy().printLine(data.toArray(
					new String[data.size()]), this.writer);
		}
	}
	
	/**
	 * １つのクラスに属するフィールドを出力する。
	 * @param clazz
	 * @param classItemSize
	 */
	protected void printFields(Class<?> clazz, int classItemSize) {
		
		for (Field field : clazz.getDeclaredFields()) {
			List<String> data = new ArrayList<String>();
			
			// クラス項目分、空の列を挿入
			for (int i = 0; i < classItemSize; i++) {
				data.add(StringUtils.EMPTY);
			}
			
			// メンバー種別
			data.add(getLabel("column.member.type.field"));
			
			// アクセス識別子
			int mod = field.getModifiers();
			
			if (Modifier.isPublic(mod)) {
				if (!this.publicField) {
					continue;
				}
				data.add("public");
				
			} else {
				continue;
			}
			
			// abstract
			data.add(NOT_APPLICABLE);
			
			// final
			data.add(Modifier.isFinal(mod) ? APPLY : NOT_APPLY);
			
			// native
			data.add(NOT_APPLICABLE);
			
			// static
			data.add(Modifier.isStatic(mod) ? APPLY : NOT_APPLY);
			
			// strictfp
			data.add(NOT_APPLICABLE);
			
			// transient
			data.add(Modifier.isTransient(mod) ? APPLY : NOT_APPLY);
			
			// volatile
			data.add(Modifier.isVolatile(mod) ? "volatile" : NOT_APPLICABLE);
			
			// 型
			if (field.getType().getCanonicalName() != null) {
				data.add(field.getType().getCanonicalName());
			} else {
				data.add(field.getType().getName());
			}
			
			// 名前
			data.add(field.getName());
			
			// 引数
			data.add(NOT_APPLICABLE);
			
			getCSVStragegy().printLine(
					data.toArray(new String[data.size()]), 
					this.writer);
		}
	}
	
	private final ResourceBundleWrapper rb = 
		new ResourceBundleWrapper(ResourceBundle.getBundle("net.mikaboshi.ant.java_api_list"));
	
	protected String getLabel(String key) {
		return this.rb.getString(key);
	}

}
