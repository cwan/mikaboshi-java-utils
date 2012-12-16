package net.mikaboshi.io;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.RegexFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.junit.Test;

public class FileIterableTest {

	private final static File dir = 
		new File("src/test/resources/net/mikaboshi/io/FileIterableTestDir");
	
	@Test
	public void testNotRecursive() throws IOException {
		
		FileIterable iterable = new FileIterable(
				dir, TrueFileFilter.INSTANCE, false);
		
		List<String> pathList = new ArrayList<String>(); 
		
		for (File file : iterable) {
			pathList.add(file.getName());
		}
		
		Collections.sort(pathList);
		
		assertEquals(3, pathList.size());
		assertEquals("test1.txt", pathList.get(0));
		assertEquals("test3.txt", pathList.get(1));
		assertEquals("test5.txt", pathList.get(2));
	}
	
	@Test
	public void testRecursive() throws IOException {
		
		FileIterable iterable = new FileIterable(
				dir, new RegexFileFilter("^test\\d.txt$"), true);
		
		List<String> pathList = new ArrayList<String>(); 
		
		for (File file : iterable) {
			pathList.add(file.getName());
		}
		
		Collections.sort(pathList);
		
		assertEquals(6, pathList.size());
		assertEquals("test1.txt", pathList.get(0));
		assertEquals("test2.txt", pathList.get(1));
		assertEquals("test3.txt", pathList.get(2));
		assertEquals("test4.txt", pathList.get(3));
		assertEquals("test5.txt", pathList.get(4));
		assertEquals("test6.txt", pathList.get(5));
	}
	
	@Test
	public void testNameFilter() throws IOException {
		
		IOFileFilter filter = FileFilterFactory.or(
				FileFilterUtils.nameFileFilter("test2.txt"),
				FileFilterUtils.nameFileFilter("test5.txt"));
		
		FileIterable iterable = new FileIterable(dir, filter, true);
		
		List<String> pathList = new ArrayList<String>(); 
		
		for (File file : iterable) {
			pathList.add(file.getName());
		}
		
		Collections.sort(pathList);
		
		assertEquals(2, pathList.size());
		assertEquals("test2.txt", pathList.get(0));
		assertEquals("test5.txt", pathList.get(1));
	}
}
