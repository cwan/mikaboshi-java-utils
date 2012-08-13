package net.mikaboshi.ant;

import java.util.ArrayList;
import java.util.List;

/**
 * 正規表現パターンにより操作対象をフィルタするネスト要素。
 * @author Takuma Umezawa
 *　@since 1.0.1
 */
public class Patterns {
	
	public Patterns() {
		super();
	}
	
	private List<Include> includeList = new ArrayList<Include>();
	
	private List<Exclude> excludeList = new ArrayList<Exclude>();

	public Include createInclude() {
		Include include = new Include();
		
		this.includeList.add(include);
		
		return include;
	}
	
	public Exclude createExclude() {
		Exclude exclude = new Exclude();
		
		this.excludeList.add(exclude);
		
		return exclude;
	}
	
	public static String getPatternString(String s) {
		
		if (!s.startsWith("^")) {
			s = ".*" + s; 
		}
		
		if (!s.endsWith("$")) {
			s = s + ".*";
		}
		
		return s;
	}
	
	/**
	 * 引数の文字列が、このパターンフィルタにマッチするか。
	 * @param str
	 * @return
	 */
	public boolean isMatch(String str) {
		
		if (str == null) {
			return false;
		}
		
		if (this.includeList.isEmpty() &&
				this.excludeList.isEmpty()) {
			return true;
		}
		
		for (Exclude e : this.excludeList) {
			if (str.matches(getPatternString(e.text))) {
				return false;
			}
		}
		
		if (this.includeList.isEmpty()) {
			// excludeのみ指定された場合、excludeにマッチしなければOKとする
			return true;
		}
		
		for (Include i : this.includeList) {
			if (str.matches(getPatternString(i.text))) {
				return true;
			}
		}
		
		return false;
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder("Patterns: ");
		
		sb.append("include[ ");
		
		for (int i = 0; i < this.includeList.size(); i++) {
			if (i != 0) {
				sb.append(", ");
			}
			sb.append("/");
			sb.append(this.includeList.get(i).text);
			sb.append("/");
		}
		
		sb.append(" ], exclude[ ");
		
		for (int i = 0; i < this.excludeList.size(); i++) {
			if (i != 0) {
				sb.append(", ");
			}
			sb.append("/");
			sb.append(this.excludeList.get(i).text);
			sb.append("/");
		}
		
		sb.append(" ]");
		
		return sb.toString();
	}
	
	public static class Include {
		
		public Include() {
		}
		
		String text;
		
		public void addText(String text) {
			this.text = text;
		}
		
	}
	
	public static class Exclude {
		
		public Exclude() {
		}
		
		String text;
		
		public void addText(String text) {
			this.text = text;
		}
	}
}
