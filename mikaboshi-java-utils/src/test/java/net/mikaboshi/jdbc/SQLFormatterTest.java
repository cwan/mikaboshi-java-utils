package net.mikaboshi.jdbc;

import net.mikaboshi.jdbc.SQLFormatter;

import org.apache.commons.lang.StringUtils;
import org.junit.Test;

import junit.framework.TestCase;

public class SQLFormatterTest extends TestCase {

	@Test
	public void testTokenize() {
		String sql = "select * from hoge \r\n" +
			"where (a = 'xxx' or a<>'x''y') and c=?\n" +
			"order by a asc, b desc";
		
		String[] tokens = new SQLFormatter().tokenize(sql);
		
		assertEquals("select", tokens[0]);
		assertEquals("*", tokens[1]);
		assertEquals("from", tokens[2]);
		assertEquals("hoge", tokens[3]);
		assertEquals("where", tokens[4]);
		assertEquals("(", tokens[5]);
		assertEquals("a", tokens[6]);
		assertEquals("=", tokens[7]);
		assertEquals("'xxx'", tokens[8]);
		assertEquals("or", tokens[9]);
		assertEquals("a", tokens[10]);
		assertEquals("<>", tokens[11]);
		assertEquals("'x''y'", tokens[12]);
		assertEquals(")", tokens[13]);
		assertEquals("and", tokens[14]);
		assertEquals("c", tokens[15]);
		assertEquals("=", tokens[16]);
		assertEquals("?", tokens[17]);
		assertEquals("order", tokens[18]);
		assertEquals("by", tokens[19]);
		assertEquals("a", tokens[20]);
		assertEquals("asc", tokens[21]);
		assertEquals(",", tokens[22]);
		assertEquals("b", tokens[23]);
		assertEquals("desc", tokens[24]);
		
		assertEquals(25, tokens.length);
	}
	
	@Test
	public void testFormat() {
		String sql = "select * from hoge \r\n" +
			"where (a = 'xxx' or a <> 'x''y' and b!=c) and c=?\n" +
			"order by a asc, b desc";
		
		String[] expected = {
				"select",
				"\t*",
				"from",
				"\thoge",
				"where",
				"\t(",
				"\t\ta = 'xxx'",
				"\t\tor a <> 'x''y'",
				"\t\tand b != c",
				"\t)",
				"\tand c = ?",
				"order by",
				"\ta asc,",
				"\tb desc"
		};
		
		assertEquals(StringUtils.join(expected, "\n"),
				new SQLFormatter("\t", "\n").format(sql));
		
	}
	
	@Test
	public void testFormat2() {
		String sql = "select * from ( select a, b, c, CasE when a = 1 then 'a' when b = z then 'b' else 'x' END from hoge UNION select * from fuga ) x";
		
		String[] expected = {
				"select",
				"  *",
				"from",
				"  (",
				"    select",
				"      a,",
				"      b,",
				"      c,",
				"      CasE",
				"        when a = 1 then 'a'",
				"        when b = z then 'b'",
				"        else 'x'",
				"      END",
				"    from",
				"      hoge",
				"    UNION",
				"    select",
				"      *",
				"    from",
				"      fuga",
				"  )",
				"  x",
		};
		
		assertEquals(StringUtils.join(expected, "\n"),
				new SQLFormatter("  ", "\n").format(sql));
	}
	
	@Test
	public void testFormat3() {
		String sql = "select count(*) from hoge";
		String result = new SQLFormatter("  ", "\n").format(sql);
		
		String[] expected = {
				"select",
				"  count(*)",
				"from",
				"  hoge"
		};
		
		assertEquals(StringUtils.join(expected, "\n"), result);
	}
	
	@Test
	public void testFormat_文字列リテラル中に不等号() {
		String sql = "select '<br/>' from hoge";
		String result = new SQLFormatter("  ", "\n").format(sql);
		
		String[] expected = {
				"select",
				"  '<br/>'",
				"from",
				"  hoge"
		};
		
		assertEquals(StringUtils.join(expected, "\n"), result);
	}
	
	@Test
	public void testInsert_INSERT文() {
		String sql = "INSERT INTO HOGE (a, b, c) VALUES (1, '22', '333')";
		String result = new SQLFormatter("  ", "\n").format(sql);
		
		String[] expected = {
				"INSERT INTO HOGE",
				"(",
				"  a,",
				"  b,",
				"  c",
				")",
				"VALUES",
				"(",
				"  1,",
				"  '22',",
				"  '333'",
				")",
				""
		};
		
		assertEquals(StringUtils.join(expected, "\n"), result);
		
	}
}
