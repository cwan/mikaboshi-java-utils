package net.mikaboshi.jdbc;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Stack;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;

/**
 * SQLの整形を行う。
 * 
 * @author Takuma Umezawa
 */
public class SQLFormatter {

	private static final Set<String> toplevelKeywords;
	
	static {
		toplevelKeywords = new HashSet<String>();
		toplevelKeywords.add("select");
		toplevelKeywords.add("from");
		toplevelKeywords.add("where");
		toplevelKeywords.add("having");
		toplevelKeywords.add("order by");
		toplevelKeywords.add("group by");
		toplevelKeywords.add("update");
		toplevelKeywords.add("insert");
		toplevelKeywords.add("delete");
		toplevelKeywords.add("set");
	}
	
	private static final Set<String> newLineKeywords;
	
	static {
		newLineKeywords = new HashSet<String>();
		newLineKeywords.add("values");
		newLineKeywords.add("inner join");
		newLineKeywords.add("left outer join");
		newLineKeywords.add("left join");
		newLineKeywords.add("right outer join");
		newLineKeywords.add("right join");
		newLineKeywords.add("natural join");
		newLineKeywords.add("on");
		newLineKeywords.add("using");
		newLineKeywords.add("and");
		newLineKeywords.add("or");
		newLineKeywords.add("when");
		newLineKeywords.add("else");
	}
	
	private static final Set<String> pairKeywords;
	
	static {
		pairKeywords = new HashSet<String>();
		pairKeywords.add("order by");
		pairKeywords.add("group by");
		pairKeywords.add("primary key");
		pairKeywords.add("foreign key");
		pairKeywords.add("insert into");
		pairKeywords.add("create table");
		pairKeywords.add("drop table");
		pairKeywords.add("alter table");
		pairKeywords.add("create view");
		pairKeywords.add("drop view");
		pairKeywords.add("alter view");
		pairKeywords.add("inner join");
		pairKeywords.add("natural join");
		pairKeywords.add("left join");
		pairKeywords.add("right join");
	}
	
	private static final Set<String> trioKeywords;
	
	static {
		trioKeywords = new HashSet<String>();
		trioKeywords.add("left outer join");
		trioKeywords.add("right outer join");
	}
	
	private String indent;
	
	private String lineSparator;
	
	/**
	 * インデント、改行文字を指定する。
	 * @param indent
	 * @param lineSeparator
	 */
	public SQLFormatter(String indent, String lineSeparator) {
		this.indent = indent;
		this.lineSparator = lineSeparator;
	}
	
	/**
	 * デフォルトのインデント（タブ）、改行文字（システム標準）を使う。
	 */
	public SQLFormatter() {
		this.indent = "\t";
		this.lineSparator = IOUtils.LINE_SEPARATOR;
	}
	
	/**
	 * SQLを、改行・インデントで整形する。
	 * @param sql
	 * @return
	 */
	public String format(String sql) {
		if (sql == null) {
			return null;
		}
		
		String[] tokens = tokenize(sql);
		tokens = mergeMultiWordKeyword(tokens);
		tokens = mergePerenthesis(tokens);
		
		return format(tokens);
	}
	
	/**
	 * SQL文をトークンに分割する
	 */
	public String[] tokenize(String sql) {
		List<String> tokenList = new ArrayList<String>();
		StringBuilder charBuf = new StringBuilder();
		
		boolean isQuoted = false;
		int length = sql.length();
		char endOfString = Character.valueOf((char) 0);
		
		for (int i = 0; i < length; i++) {
			char c = sql.charAt(i);
			char next = (i == length - 1) ? endOfString : sql.charAt(i + 1);
			
			if (Character.isWhitespace(c)) {
				if (charBuf.length() == 0) {
					// トークンの先頭のスペースはスキップ
					continue;
				}
				
				if (!isQuoted) {
					// 引用の外ならば、そこでトークン終了
					tokenList.add(charBuf.toString());
					charBuf = new StringBuilder();
				} else {
					// 引用の中ならば、トークンに追加
					charBuf.append(c);
				}
				
				continue;
			}
			
			if (c == '\'') {
				isQuoted = !isQuoted;
				charBuf.append(c);
				continue;
			}
			
			if (isQuoted) {
				// 文字列リテラル中ならば特殊文字もそのまま
				charBuf.append(c);
				continue;
			}
			
			if (c == ',' || c == ';') {
				
				if (charBuf.length() != 0) {
					// その前の文字までをトークンに切り出す
					tokenList.add(charBuf.toString());
					charBuf = new StringBuilder();
				}
				
				// この文字自体もトークンにする
				tokenList.add(Character.toString(c));
				continue;
			}
			
			if (c == '(' || c == ')') {
				
				if (charBuf.toString().trim().length() != 0) {
					// その前の文字までをトークンに切り出す
					tokenList.add(charBuf.toString());
					charBuf = new StringBuilder();
				}
				
				// この文字自体もトークンにする
				tokenList.add(Character.toString(c));
				continue;
			}
			
			if (c == '=' ||
				c == '<' && next != '=' && next != '>' ||
				c == '>' && next != '=') {
				
				if (charBuf.toString().trim().length() != 0) {
					// その前の文字までをトークンに切り出す
					tokenList.add(charBuf.toString());
					charBuf = new StringBuilder();
				}
				
				// この文字自体もトークンにする
				tokenList.add(Character.toString(c));
				continue;
			}
			
			if ( c == '<' && next == '=' ||
					c == '<' && next == '>' ||
					c == '>' && next == '=' ||
					c == '!' && next == '=' ) {
				
				if (charBuf.toString().trim().length() != 0) {
					// その前の文字までをトークンに切り出す
					tokenList.add(charBuf.toString());
					charBuf = new StringBuilder();
				}
				
				// この文字と次の文字をトークンにする
				tokenList.add("" + c + next);
				i++;
				continue;
			}
			
			// 通常の文字
			charBuf.append(c);
		}
		
		if (charBuf.toString().trim().length() != 0) {
			tokenList.add(charBuf.toString().trim());
		}
		
		return tokenList.toArray(new String[tokenList.size()]);
	}
	
	/**
	 * 複数のトークンから構成されるキーワードをまとめる。
	 * @param tokens
	 * @return
	 */
	protected String[] mergeMultiWordKeyword(String[] tokens) {
		List<String> result = new ArrayList<String>();
		
		for (int i = 0; i < tokens.length; i++) {
			
			if (i == tokens.length - 1) {
				result.add(tokens[i]);
				break;
			}
			
			String pair = tokens[i] + " " + tokens[i + 1];
		
			if (pairKeywords.contains(pair.toLowerCase())) {
				result.add(pair);
				i++;
				continue;
			}
			
			if (i == tokens.length - 2) {
				result.add(tokens[i]);
				result.add(tokens[i + 1]);
				break;
			}
			
			String trio = tokens[i] + " " + tokens[i + 1] + " " + tokens[i + 2];
			
			if (trioKeywords.contains(trio.toLowerCase())) {
				result.add(trio);
				i += 2;
				continue;
			}
			
			result.add(tokens[i]);
		}
		
		return result.toArray(new String[result.size()]);
	}
	
	/**
	 * 括弧の中がサブクエリ・述語以外ならば、１項に結合する
	 */
	protected String[] mergePerenthesis(String[] tokens) {
		List<String> result = new ArrayList<String>();
		int start = 0;
		boolean isStatement = false;
		boolean isIntoOrValues = false;
		
		List<String> buf = new ArrayList<String>();
		
		for (int i = 0; i < tokens.length; i++) {
			buf.add(tokens[i]);
			
			if (tokens[i].equals("(")) {
				start = i;
				isStatement = false;
				
				if (i > 1 && tokens[i - 2].equalsIgnoreCase("insert into") ||
					i > 0 && tokens[i - 1].equalsIgnoreCase("values")) {
					isIntoOrValues = true;
				}
				
				continue;
			}
			
			if (tokens[i].equals(")")) {
				if (isStatement || isIntoOrValues) {
					isStatement = false;
					isIntoOrValues = false;
					continue;
				}
				
				List<String> sub = new ArrayList<String>();
				for (int j = start; j <= i; j++) {
					sub.add(tokens[j]);
				}
				
				result.addAll(buf.subList(0, buf.size() - sub.size()));
				
				result.add("(" +
						StringUtils.join(sub.subList(1, sub.size() - 1), " ") +
						")");
				
				List<String> mergedList = new ArrayList<String>(result);
				for (int j = i + 1; j < tokens.length; j++) {
					mergedList.add(tokens[j]);
				}

				return mergePerenthesis((String[]) mergedList.toArray(new String[mergedList.size()]));
			}
			
			String lowerCaseToken = tokens[i].toLowerCase();
			
			if (lowerCaseToken.equals("select") ||
					lowerCaseToken.equals("and") ||
					lowerCaseToken.equals("or") ||
					lowerCaseToken.equals("=") ||
					lowerCaseToken.equals("<") ||
					lowerCaseToken.equals(">") ||
					lowerCaseToken.equals("<=") ||
					lowerCaseToken.equals(">=") ||
					lowerCaseToken.equals("<>") ||
					lowerCaseToken.equals("!=")) {
				isStatement = true;
			}
		}
		
		result.addAll(buf);
		
		return result.toArray(new String[result.size()]);
	}
	
	/**
	 * トークンに分割されたSQLから、改行・インデントありのSQL文字列に整形する。
	 * @param tokens
	 * @return
	 */
	protected String format(String[] tokens) {
		
		PositiveLevel indentLv = new PositiveLevel();
		
		StringBuilder result = new StringBuilder();
		
		// カッコのレベル
		Stack<Integer> parenthesisStartLevel = new Stack<Integer>();
		
		for (int i = 0; i < tokens.length; i++) {
			String token = tokens[i];
			String tokenl = token.toLowerCase();
			
			if (";".equals(token)) {
				indentLv.reset();
				result = addNewLine(result);
				result.append(token);
				continue;
			}
			
			if (toplevelKeywords.contains(tokenl)) {
				if (i == 0 || !tokens[i - 1].equals("(") && !tokens[i - 1].toLowerCase().equals("union")) {
					indentLv.down();
				}
				
				result = addNewLine(result);
				
				result.append(StringUtils.repeat(indent, indentLv.getLevel()));
				result.append(token);
				result.append(lineSparator);
				
				indentLv.up();
				continue;
			}
			
			if (newLineKeywords.contains(tokenl)) {
				result = addNewLine(result);
				result.append(StringUtils.repeat(indent, indentLv.getLevel()));
				result.append(token);
				
				continue;
			}
			
			if (token.equals("(")) {
				parenthesisStartLevel.push(indentLv.getLevel());
				
				result = addNewLine(result);
				result.append(StringUtils.repeat(indent, indentLv.getLevel()));
				result.append(token);
				result.append(lineSparator);
				
				indentLv.up();
				continue;
			}
			
			if (token.startsWith("(")) {
				// 関数の引数
				int lastIndex = result.length() - 1;
				if (result.charAt(lastIndex) == ' ') {
					result.deleteCharAt(lastIndex);
				}
				result.append(token);
				continue;
			}
			
			if (token.equals(")")) {
				result = addNewLine(result);
				
				if (!parenthesisStartLevel.isEmpty()) {
					indentLv.setLevel(parenthesisStartLevel.pop());
				} else {
					indentLv.setLevel(0);
				}
				
				result.append(StringUtils.repeat(indent, indentLv.getLevel()));
				result.append(token);
				result.append(lineSparator);
				
				continue;
			}
			
			if (tokenl.equals("union")) {
				result = addNewLine(result);
				indentLv.down();
				
				result.append(StringUtils.repeat(indent, indentLv.getLevel()));
				result.append(token);
				result.append(lineSparator);
				
				continue;
			}
			
			if (tokenl.equals("case")) {
				result = addNewLine(result);
				
				result.append(StringUtils.repeat(indent, indentLv.getLevel()));
				result.append(token);
				result.append(lineSparator);
				indentLv.up();
				
				continue;
			}
			
			if (tokenl.equals("end")) {
				result = addNewLine(result);
				indentLv.down();
				
				result.append(StringUtils.repeat(indent, indentLv.getLevel()));
				result.append(token);
				result.append(lineSparator);
				
				continue;
			}
			
			if (token.equals(",")) {
				result.append(token);
				result.append(lineSparator);
				continue;
			}
			
			if (result.toString().endsWith(lineSparator)) {
				result.append(StringUtils.repeat(indent, indentLv.getLevel()));
				result.append(token);
			} else if (result.length() == 0) {
				result.append(token);
			} else if (result.toString().endsWith(" ")) {
				result.append(token);
			} else {
				result.append(" ");
				result.append(token);
			}
		}
		
		return result.toString();
	}
	
	private StringBuilder addNewLine(StringBuilder sb) {
		if (sb.length() == 0 || sb.toString().endsWith(lineSparator)) {
			return sb;
		} else {
			return sb.append(lineSparator);
		}
	}
	
	private static class PositiveLevel {
		int level = 0;
		public PositiveLevel() {}
		
		public void up() {
			level++;
		}
		
		public void down() {
			level--;
			if (level < 0) {
				level = 0;
			}
		}
		
		public int getLevel() {
			return level;
		}
		
		public void setLevel(Integer lv) {
			if (lv != null) {
				this.level = lv;
			} else {
				this.level = 0;
			}
		}
		
		public void reset() {
			this.level = 0;
		}
	}
}
