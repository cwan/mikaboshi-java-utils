package net.mikaboshi.io;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;

import net.mikaboshi.validator.SimpleValidator;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;

/**
 * ファイルをフィルタで検索し、検索結果のイテレータを提供するクラス。
 * @author Takuma Umezawa
 * @since 1.1.1
 */
public class FileIterable implements Iterable<File> {

	private File dir;

	private IOFileFilter filter;

	private boolean recursive;

	/**
	 * 検索条件を設定する。
	 * @param dir 検索対象のディレクトリ
	 * @param filter 検索条件が設定されたファイルフィルタ
	 * @param recursive ディレクトリを再帰して検索するならばtrue
	 * @throws NullPointerException dirまたはfilterがnullの場合
	 * @throws IOException dirが存在しない場合、またはディレクトリではない場合
	 */
	public FileIterable(
			File dir,
			IOFileFilter filter,
			boolean recursive)
			throws IOException {

		this.dir = dir;
		this.filter = filter;
		this.recursive = recursive;

		SimpleValidator.validateNotNull(dir, "dir", NullPointerException.class);
		SimpleValidator.validateNotNull(filter, "filter", NullPointerException.class);

		if (!dir.exists() || !dir.isDirectory()) {
			throw new IOException("directory does not exist <" + dir.getAbsolutePath() + ">");
		}
	}

	public Iterator<File> iterator() {
		IOFileFilter dirFilter =
			this.recursive ? TrueFileFilter.INSTANCE : null;

		return FileUtils.iterateFiles(this.dir, this.filter, dirFilter);
	}

}
