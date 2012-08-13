package net.mikaboshi.property;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.apache.commons.lang.StringUtils;

/**
 * <p>
 * オブジェクトのプロパティを表すアノテーション。
 * </p>
 * <p>
 * このアノテーションは、public メソッドに対して指定する。
 * オブジェクトと外部のファイル等との間で値の移行を行う場合、このアノテーションで指定する。
 * プロパティ値の読み込み元および書き込み先は、実装クラスによって決められる。
 * </p>
 * <h3>mode 属性について</h3>
 * <p>
 * 外部から読み込んだ値を Java オブジェクトに設定する場合は、mode 属性に Mode.SET を指定する。
 * Java オブジェクトのプロパティを外部に書き出す場合は、mode 属性に Mode.GET を指定する。
 * mode 属性を省略した場合、読み込み専用（Mode.SET）が適用される。
 * </p>
 * <h3>alias 属性について</h3>
 * <p>
 * alias 属性を指定した場合、その値がプロパティ名として使用される。
 * alias 属性を省略した場合、メソッド名から以下の規則に従ってプロパティ名が決められる。
 * <ul>
 * 	<li>mode = Mode.SET においてメソッド名が "set" で始まる場合、
 *      先頭の "set" を除き、先頭を小文字にした文字列<br />
 *      例: setAaaBbb → aaaBbb
 *  </li>
 * 	<li>mode = Mode.GET においてメソッド名が "get" で始まる場合、
 *      先頭の "get" を除き、先頭を小文字にした文字列<br />
 *      例: getAaaBbb → aaaBbb
 *  </li>
 * 	<li>メソッド名が "set"、"get" で始まらない場合は、メソッド名全体</li>
 * </ul>
 * </p>
 * <h3>引数について</h3>
 * <p>
 * mode = Mode.SET が指定されたメソッドの引数は1つでなくてはならない。
 * 戻り値の有無は任意である。
 * </p><p>
 * 引数の型は String、Boolean、Integer、Long、Double、配列、リスト（java.util.List）
 * のいずれかとする。
 * 配列およびリストの要素は、String、Boolean、Integer、Long、Double のいずれかとする。
 * リストの場合は、elementType 属性で、リストの要素の型を指定する。（省略した場合は、String とする）
 * </p><p>
 * 引数の型が String、Boolean、Integer、Long、Double の場合、上記の規則で決められた
 * プロパティ名に完全一致するプロパティが読み込み元から取得され、設定される。
 * </p><p>
 * 引数の型が配列またはリストの場合、上記の規則で決められたプロパティ名の末尾に [ と ] で
 * 囲った番号（[0], [1], [2]　...）をつけたプロパティが読み込み元から取得され、
 * この順番の配列またはリストが設定される。
 * なお、番号は 0 からの連番でなければならず、欠番があった場合はそれ以降のプロパティは読み込まれない。
 * </p><p>
 * ⇒ 例: メソッド m1(String[] args)　には、m1[0]、m1[1]、m1[2] ... が設定される。
 * </p><p>
 * Boolean、Integer、Long、Double は、各々の parseXxx メソッドで解釈した値が設定される。
 * 解釈できない文字列の場合、エラーログに例外が書き出されるが、外に例外はスローされない。
 * </p>
 * 
 * <h3>戻り値について</h3>
 * <p>
 * mode = Mode.GET が指定されたメソッドには引数はあってはならない。
 * 戻り値の型は 配列、リスト（java.util.List）、および任意の Object とする。
 * </p><p>
 * 戻り値の型が 配列またはリストの場合、上記の規則で決められたプロパティ名の末尾に
 * [ と ] で囲った番号（[0], [1], [2]　...）をつけたものをプロパティとし、
 * 各々の要素の toString() の戻り値を書き出す。
 * 要素が null の場合、その要素は書き出されない。（配列番号は欠番にはしない）
 *　</p><p>
 * ⇒ 例: getProp1() メソッドの戻り値が配列 {"xxx", null, "yyy"} の場合、
 * prop1[0]=xxx、prop1[1]=yyy が書き出される。
 * </p><p>
 * 戻り値の型が配列やリストでない通常の Object の場合、上記の規則で決められた
 * プロパティ名で toString() の戻り値が書き出される。
　* </p><p>
 * 戻り値が null の場合、そのプロパティは書き出されない。
 * </p>
 * 
 * @author Takuma Umezawa
 * @since 0.1.2
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Property {
	
	/**
	 * mode 属性に設定するフラグ
	 */
	public enum Mode {
		/** アノテーションを設定したプロパティに値を設定する */
		SET,
		/** アノテーションを設定したプロパティから値を取得する */
		GET
	}
	
	/** プロパティの別名。メソッド名とは異なる場合に指定する */
	String alias() default StringUtils.EMPTY;
	
	/** 
	 * プロパティをオブジェクトに設定するならば Mode.SET、
	 * オブジェクトから取得するならば Mode.GET を指定する。
	 * デフォルトは Mode.SET。
	 */
	Mode mode() default Mode.SET;
	
	/**
	 * List プロパティの型を指定する。
	 * List 以外で指定しても無視される。
	 * 有効な型は、String, Boolean, Integer, Double, Object のみ。
	 * デフォルトは String。
	 */
	Class<?> elementType() default String.class;
}
