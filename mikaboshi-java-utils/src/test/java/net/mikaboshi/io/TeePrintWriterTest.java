package net.mikaboshi.io;

import static org.junit.Assert.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Locale;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.NullOutputStream;
import org.apache.commons.lang.StringUtils;
import org.junit.Test;

/**
 * TeePrintWriterのテスト
 * @since 1.1.5
 */
public class TeePrintWriterTest {

	@Test
	public void test1() throws IOException {
		
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		
		TeePrintWriter writer = new TeePrintWriter(
				new PrintWriter(NullOutputStream.NULL_OUTPUT_STREAM, true), 
				new PrintWriter(out, true));

		writer.append('a');
		writer.append("b");
		writer.write("c", 0, 1);
		writer.format("%s", "d");
		writer.format(Locale.getDefault(), "%s", "e");
		writer.print(true);
		writer.print('f');
		writer.print(new char[] {'g'});
		writer.print(1.0d);
		writer.print(2.0f);
		writer.print(3);
		writer.print(4L);
		writer.print(new Object() {
			@Override
			public String toString() {
				return "h";
			}
		});
		writer.print("i");
		writer.printf("%s", "j");
		writer.printf(Locale.getDefault(), "%s", "k");
		writer.println();
		writer.println(true);
		writer.println('l');
		writer.println(new char[] {'m'});
		writer.println(5.0d);
		writer.println(6.0f);
		writer.println(7);
		writer.println(8L);
		writer.println(new Object() {
			@Override
			public String toString() {
				return "n";
			}
		});
		writer.write(new char[] {'o'});
		writer.write((int) 'p');
		writer.write("q");
		writer.write(new char[] {'r'}, 0, 1);
		writer.write("s", 0, 1);
		writer.println();	// 最後がprintlnでないと出力されない
		
		String expected =
			StringUtils.join(new String[] {
			"abcdetruefg1.02.034hijk",
			"true", "l", "m", "5.0", "6.0", "7", "8", "n",
			"opqrs"
			}, IOUtils.LINE_SEPARATOR) + IOUtils.LINE_SEPARATOR;
			
		assertEquals(expected, out.toString());
	}
}
