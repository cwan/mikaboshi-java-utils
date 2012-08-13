package net.mikaboshi.io;

import org.apache.commons.io.filefilter.AndFileFilter;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.OrFileFilter;

/**
 * ファイルフィルタを簡易に作成するユーティリティクラス。
 * @author Takuma Umezawa
 * @since 1.1.1
 */
public final class FileFilterFactory {

	private FileFilterFactory() {}
	
	/**
	 * 可変引数のフィルタを論理積で結合する。
	 * @param filters
	 * @return
	 */
	public static IOFileFilter and(IOFileFilter ... filters) {
		AndFileFilter andFileFilter = new AndFileFilter();
		
		for (IOFileFilter filter : filters) {
			andFileFilter.addFileFilter(filter);
		}
		
		return andFileFilter;
	}
	
	/**
	 * 可変引数のフィルタを論理和で結合する。
	 * @param filters
	 * @return
	 */
	public static IOFileFilter or(IOFileFilter ... filters) {
		OrFileFilter andFileFilter = new OrFileFilter();
		
		for (IOFileFilter filter : filters) {
			andFileFilter.addFileFilter(filter);
		}
		
		return andFileFilter;
	}
}
