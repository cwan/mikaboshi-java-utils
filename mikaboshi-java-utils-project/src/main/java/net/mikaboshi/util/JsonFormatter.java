package net.mikaboshi.util;

import net.mikaboshi.validator.SimpleValidator;

import org.apache.commons.io.IOUtils;

/**
 * <p>
 * JSON (JavaScript Object Notation) 文字列のフォーマットを行う。
 * </p>
 * @author Takuma Umezawa
 * @since 1.1.5
 */
public class JsonFormatter {

	private String indent = "\t";
	
	private String lineSeparator = IOUtils.LINE_SEPARATOR;
	
	/**
	 * デフォルトコンストラクタ。
	 * インデント＝タブ（\t）、改行文字＝システムデフォルトを適用する。
	 */
	public JsonFormatter() {
	}
	
	/**
	 * インデント、改行コードを指定するコンストラクタ。
	 * @param indent
	 * @param lineSeparator
	 * @throws NullPointerException indent, lineSeparatorがnullの場合
	 */
	public JsonFormatter(String indent, String lineSeparator) {
		
		SimpleValidator.validateNotNull(indent, "indent");
		SimpleValidator.validateNotNull(lineSeparator, "lineSeparator");
		
		this.indent = indent;
		this.lineSeparator = lineSeparator;
	}
	
	/**
	 * strをJSON整形する
	 * @param str
	 * @return
	 * @throws NullPointerException strがnullの場合
	 */
	public String format(String str) {
		
		SimpleValidator.validateNotNull(str, "str");
		
		StringBuilder buffer = new StringBuilder();
		
		final int NOT_QUOTED = -1;
		
		int quote = NOT_QUOTED;
		int indentLv = 0;
		boolean prevWhiteSpace = false;
		
		int[] codePointArray = CodePointUtils.toCodePointArray(str);
		
		for (int i  = 0; i < codePointArray.length; i++) {
			int cp = codePointArray[i];
		
			if ( quote == cp &&
				 buffer.length() >= 1 &&
				 buffer.charAt(buffer.length() - 1) != '\\' ) {
				
				// 文字列（引用符で囲まれた範囲）の終了
				quote = NOT_QUOTED;
				buffer.appendCodePoint(cp);
				prevWhiteSpace = false;
			
			} else if ( quote != NOT_QUOTED ) {
				// 文字列（引用符で囲まれた範囲）であるならば、なんでも追加
				// ※ 上で、引用符の終了をチェックしている
				
				buffer.appendCodePoint(cp);
				prevWhiteSpace = false;
				
			} else  if ( cp == (int) '"' || cp == (int) '\'' ) {
				// 文字列（引用符で囲まれた範囲）の開始
				quote = cp;
				buffer.appendCodePoint(cp);
				prevWhiteSpace = false;
				
			} else if ( cp == (int) '\r' || cp == (int) '\n' ) {
				// 引用符の外の改行は無視
				prevWhiteSpace = true;
			
			} else if ( cp == (int) '[') {
				// 配列の開始
				
				buffer.appendCodePoint(cp);
				
				if ( i != codePointArray.length - 1 &&
					 codePointArray[i + 1] != (int) ']' ) {
					
					buffer.append(this.lineSeparator);
					addIndent(buffer, ++indentLv);
					prevWhiteSpace = true;
				} else {
					prevWhiteSpace = false;
				}
				
			} else if ( cp == (int) '{') {
				// オブジェクトの開始
				
				buffer.appendCodePoint(cp);
				
				if ( i != codePointArray.length - 1 &&
					 codePointArray[i + 1] != (int) '}' ) {
					
					buffer.append(this.lineSeparator);
					addIndent(buffer, ++indentLv);
					prevWhiteSpace = true;
				} else {
					prevWhiteSpace = false;
				}
				
			} else if (cp == (int) ',') {
				// プロパティの区切り文字
				
				buffer.appendCodePoint(cp);
				buffer.append(this.lineSeparator);
				addIndent(buffer, indentLv);
				prevWhiteSpace = true;
				
			} else if ( cp == (int) ']' ) {
				// 配列の終了
				if ( i != 0 && codePointArray[i - 1] != (int) '[') {
					buffer.append(this.lineSeparator);
					addIndent(buffer, --indentLv);
				}
				
				buffer.appendCodePoint(cp);
				prevWhiteSpace = false;
				
			} else if ( cp == (int) '}' ) {
				// オブジェクトの終了
				if ( i != 0 && codePointArray[i - 1] != (int) '{') {
					buffer.append(this.lineSeparator);
					addIndent(buffer, --indentLv);
				}
				
				buffer.appendCodePoint(cp);
				prevWhiteSpace = false;
				
			} else if ( Character.isWhitespace(cp) ) {
				
				if (!prevWhiteSpace) {
					// ホワイトスペースが連続する場合は省略する
					buffer.appendCodePoint(cp);
					prevWhiteSpace = true;
				}
				
			} else if ( cp == (int) ':' ) {
				
				if (!prevWhiteSpace) {
					// 直前にスペースがない場合は挿入する
					buffer.append(' ');
				}
				
				buffer.append(": ");
				
				prevWhiteSpace = true;
				
			} else {
				// その他（一般の文字）
				buffer.appendCodePoint(cp);
				prevWhiteSpace = false;
			}
		}
		
		return buffer.toString();
		
	}
	
	protected void addIndent(StringBuilder buffer, int indentLevel) {
		
		for (int i = 0; i < indentLevel; i++) {
			buffer.append(this.indent);
		}
	}
	
}
