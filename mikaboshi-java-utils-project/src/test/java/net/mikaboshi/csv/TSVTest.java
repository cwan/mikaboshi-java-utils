package net.mikaboshi.csv;


import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import net.mikaboshi.csv.CSVFileUtils;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;

import static junit.framework.Assert.*;

/**
 * TSV（タブ区切り）のテスト。
 * CSV以外では最も使われるケースであるため、特別にテストする。
 * @author Takuma Umezawa
 *
 */
public class TSVTest {
	
	private File outFile;
	private File inFile;
	private File expectedOutFile;

	@Before
	public void setUp() throws Exception {
		String base = "src/test/resources/" + this.getClass().getName().replace('.', '/');
		
		this.outFile = new File(base + "_Out.txt");
		this.inFile = new File(base + "_In.txt");
		this.expectedOutFile = new File(base + "_ExpectedOut.txt");
	}
	
	private static List<List<String>> data;
	static {
		List<String> line1 = new ArrayList<String>();
		line1.add("abc");
		line1.add("def");
		line1.add("hij");
		
		List<String> line2 = new ArrayList<String>();
		line2.add("あいう");
		line2.add("えお");
		
		List<String> line3 = new ArrayList<String>();
		line3.add("ABC");
		line3.add("DEFG\tHIJ");
		line3.add("KLM\"NOP");
		line3.add("KLM\"NO\r\nP");
		
		data = new ArrayList<List<String>>();
		data.add(line1);	
		data.add(line2);
		data.add(line3);
	}
	
	/**
	 * TSVファイル書き出しのテスト
	 * @throws IOException
	 */
	@Test
	public void testWriteFile() throws IOException {
		if (this.outFile.exists()) {
			this.outFile.delete();
		}
		
		CSVStrategy tsvStrategy = new StandardCSVStrategy();
		tsvStrategy.setDelimiter("\t");
		tsvStrategy.setLineSeparator(IOUtils.LINE_SEPARATOR_WINDOWS);
		
		new CSVFileUtils(tsvStrategy, "UTF-8").writeFile(data, this.outFile);
		
		assertTrue(this.outFile.exists());
		
		String outData = FileUtils.readFileToString(this.outFile, "UTF-8");
		String expected = FileUtils.readFileToString(this.expectedOutFile, "UTF-8");
		
		assertEquals(expected, outData);
	}
	
	/**
	 * CSVファイル読み取りのテスト
	 * @throws IOException 
	 */
	@Test
	public void testReadFile() throws IOException {
		assertTrue(this.inFile.exists());
		
		CSVStrategy tsvStrategy = new StandardCSVStrategy();
		tsvStrategy.setDelimiter("\t");
		
		List<List<String>> readData =
			new CSVFileUtils(tsvStrategy, "UTF-8").readFile(this.inFile);
		
		assertEquals(data.size(), readData.size());
		
		for (int i = 0; i < readData.size(); i++) {
			assertEquals("i=" + i, data.get(i).size(), readData.get(i).size());
			
			for (int j = 0; j > readData.get(i).size(); j++) {
				assertEquals("i=" + i + ",j=" + j,
						data.get(i).get(j), readData.get(i).get(j));
			}
		}
	}
}
