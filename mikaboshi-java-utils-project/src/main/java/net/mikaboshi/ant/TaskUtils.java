package net.mikaboshi.ant;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.types.Resource;
import org.apache.tools.ant.types.resources.FileResource;

/**
 * Antタスクのユーティリティクラス
 * @author Takuma Umezawa
 *
 */
public final class TaskUtils {
	
	private TaskUtils() {}
	
	/**
	 * ファイルの集合を取得する。
	 * 
	 * @param file このファイルを加える。
	 * @param dir このディレクトリの直下にあるファイルを加える。
	 * @param fileSetList このfilesetで抽出されたファイルを加える。
	 * @param unite
	 * 			trueの場合、file, dir, fileSetListの和集合を返す。
	 * 			falseの場合、file, dir, fileSetListのいずれか１つのファイル集合を返す。
	 * 			（falseで複数が指定されていた場合は、file, dir, fileSetListの優先順位で返す。）
	 * @return
	 */
	public static Set<File> getFileSet(
			File file,
			File dir,
			List<FileSet> fileSetList,
			boolean unite) throws BuildException {
		
		Set<File> result = new HashSet<File>();
		
		if (file != null) {
			result.add(file);
			
			if (!unite) {
				return result;
			}
		}
		
		if (dir != null) {
		
			for (String content : dir.list()) {
				File fileOrDir;
				
				try {
					fileOrDir = new File(
							dir.getCanonicalPath() + IOUtils.DIR_SEPARATOR + content);
				} catch (IOException e) {
					throw new BuildException(e);
				}
				
				if (fileOrDir.isFile()) {
					result.add(fileOrDir);
				}
			}
			
			if (!unite) {
				return result;
			}
		}
		
		for (FileSet fs : fileSetList) {
			
			for (@SuppressWarnings("unchecked")
				 Iterator<Resource> iter = fs.iterator(); iter.hasNext();) {
				Resource resource = iter.next();
				
				if (resource instanceof FileResource && !resource.isDirectory()) {
					result.add(((FileResource) resource).getFile());
				}
			}
		}
		
		return result;
	}

}
