package net.mikaboshi.util;

import static org.junit.Assert.*;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.apache.commons.lang.time.StopWatch;
import org.junit.Test;

public class ThreadSafeUtilsTest {

	public static void main(String[] args) {
		// 実行速度の比較
		testMatch();
		testMatches();
		testFormatDate();
		testFormatNumber();
		
		// マルチスレッドで正しく動くことを確認
		final ThreadSafeUtilsTest test = new ThreadSafeUtilsTest();
		
		ExecutorService ex = Executors.newFixedThreadPool(12);
		for (int i = 0; i < 10000; i++) {
			final int count = i;
			ex.execute(new Runnable() {
				public void run() {
					if (count % 3 == 0) {
						test.testNotMatch();
					} else if (count % 3 == 1) {
						test.testMatch1();
					} else {
						test.testMatch2();
					}
				}
			});
		}
		ex.shutdown();
	}
	
	@Test
	public void testNotMatch() {
		String[] result = ThreadSafeUtils.match("abc", "bcd");
		assertNull(result);
	}
	
	@Test
	public void testMatch1() {
		String[] result = ThreadSafeUtils.match("abcdefg", ".+b(.{3}).+");
		assertNotNull(result);
		assertEquals(1, result.length);
		assertEquals("cde", result[0]);
	}
	
	@Test
	public void testMatch2() {
		String[] result = ThreadSafeUtils.match("abcdefg", ".+b(.{3})(.+)");
		assertNotNull(result);
		assertEquals(2, result.length);
		assertEquals("cde", result[0]);
		assertEquals("fg", result[1]);
	}
	
	@Test
	public void testMatches_partial() {
		
		assertTrue( ThreadSafeUtils.matches("abcde", "bcd", true, false) );
		assertFalse( ThreadSafeUtils.matches("abcde", "bcd", false, false) );
	}
	
	@Test
	public void testMatches_caseSensitive() {
		
		assertTrue( ThreadSafeUtils.matches("abcde", "CD", true, false) );
		assertFalse( ThreadSafeUtils.matches("abcde", "CD", true, true) );
	}
	
	@Test(expected = PatternSyntaxException.class)
	public void testIsValidPattern() {
		
		ThreadSafeUtils.checkValidPattern("aaa");
		
		assertTrue(true);
		
		ThreadSafeUtils.checkValidPattern("a(aa");
		
		fail();
			
	}
	
	private static void testMatch() {
		String input = "http://code.google.com/p/mikaboshi/";
		String regex = "(http://|https://){1}[\\w\\.\\-/:]+";
		int count = 100000;
		
		{
			// ThreadSafeUtils.matchを使わない：ループの中でインスタンス生成
			StopWatch stopWatch = new StopWatch();
			stopWatch.start();
			
			for (int i = 0; i < count; i++) {
				Pattern pattern = Pattern.compile(regex);
				Matcher matcher = pattern.matcher(input);
				matcher.matches();
			}
			
			stopWatch.stop();
			
			System.out.println("ThreadSafeUtils.matchを使わない（ループの中でインスタンス生成 ） : " + stopWatch.getTime());
		}
		
		{
			// ThreadSafeUtils.matchを使う
			StopWatch stopWatch = new StopWatch();
			stopWatch.start();
			
			for (int i = 0; i < count; i++) {
				ThreadSafeUtils.match(input, regex);
			}
			
			stopWatch.stop();
			
			System.out.println("ThreadSafeUtils.matchを使う : " + stopWatch.getTime());
		}
		
		{
			// ThreadSafeUtils.matchを使わない：ループの外でインスタンス生成
			StopWatch stopWatch = new StopWatch();
			stopWatch.start();
			
			Pattern pattern = Pattern.compile(regex);
			
			for (int i = 0; i < count; i++) {
				Matcher matcher = pattern.matcher(input);
				matcher.matches();
			}
			
			stopWatch.stop();
			
			System.out.println("ThreadSafeUtils.matchを使わない（ループの外でインスタンス生成 ） : " + stopWatch.getTime());
		}
	}
	
	private static void testMatches() {
		String input = "http://code.google.com/p/mikaboshi/";
		String regex = "(http://|https://){1}[\\w\\.\\-/:]+";
		int count = 100000;
		
		{
			// ThreadSafeUtils.matchesを使わない：ループの中でインスタンス生成
			StopWatch stopWatch = new StopWatch();
			stopWatch.start();
			
			for (int i = 0; i < count; i++) {
				input.matches(regex);
			}
			
			stopWatch.stop();
			
			System.out.println("ThreadSafeUtils.matchesを使わない（ループの中でインスタンス生成 ） : " + stopWatch.getTime());
		}
		
		{
			// ThreadSafeUtils.matchesを使う
			StopWatch stopWatch = new StopWatch();
			stopWatch.start();
			
			for (int i = 0; i < count; i++) {
				ThreadSafeUtils.matches(input, regex);
			}
			
			stopWatch.stop();
			
			System.out.println("ThreadSafeUtils.matchesを使う : " + stopWatch.getTime());
		}
		
		{
			// ThreadSafeUtils.matchesを使わない：ループの外でインスタンス生成
			StopWatch stopWatch = new StopWatch();
			stopWatch.start();
			
			Pattern pattern = Pattern.compile(regex);
			
			for (int i = 0; i < count; i++) {
				Matcher matcher = pattern.matcher(input);
				matcher.matches();
			}
			
			stopWatch.stop();
			
			System.out.println("ThreadSafeUtils.matchesを使わない（ループの外でインスタンス生成 ） : " + stopWatch.getTime());
		}
	}
	
	private static void testFormatDate() {
		String pattern = "yyyy/MM/dd";
		Date date = new Date();
		int count = 100000;
		
		{
			// ThreadSafeUtils.formatDateを使わない：ループの中でインスタンス生成
			StopWatch stopWatch = new StopWatch();
			stopWatch.start();
			
			for (int i = 0; i < count; i++) {
				SimpleDateFormat format = new SimpleDateFormat(pattern);
				format.format(date);
			}
			
			stopWatch.stop();
			
			System.out.println("ThreadSafeUtils.formatDateを使わない（ループの中でインスタンス生成 ） : " + stopWatch.getTime());
		}
		
		{
			// ThreadSafeUtils.formatDateを使う
			StopWatch stopWatch = new StopWatch();
			stopWatch.start();
			
			for (int i = 0; i < count; i++) {
				ThreadSafeUtils.formatDate(date, pattern);
			}
			
			stopWatch.stop();
			
			System.out.println("ThreadSafeUtils.formatDateを使う : " + stopWatch.getTime());
		}
		
		{
			// ThreadSafeUtils.formatDateを使わない：ループの外でインスタンス生成
			StopWatch stopWatch = new StopWatch();
			stopWatch.start();
			
			SimpleDateFormat format = new SimpleDateFormat(pattern);
			
			for (int i = 0; i < count; i++) {
				format.format(date);
			}
			
			stopWatch.stop();
			
			System.out.println("ThreadSafeUtils.formatDateを使わない（ループの外でインスタンス生成 ） : " + stopWatch.getTime());
		}
		
	}
	
	public static void testFormatNumber() {
		String pattern = "000000";
		int count = 100000;
		
		{
			// ThreadSafeUtils.formatNumberを使わない：ループの中でインスタンス生成
			StopWatch stopWatch = new StopWatch();
			stopWatch.start();
			
			for (int i = 0; i < count; i++) {
				DecimalFormat format = new DecimalFormat(pattern);
				format.format(i);
			}
			
			stopWatch.stop();
			
			System.out.println("ThreadSafeUtils.formatNumberを使わない（ループの中でインスタンス生成 ） : " + stopWatch.getTime());
		}
		
		{
			// ThreadSafeUtils.formatNumberを使う
			StopWatch stopWatch = new StopWatch();
			stopWatch.start();
			
			for (int i = 0; i < count; i++) {
				ThreadSafeUtils.formatNumber(i, pattern);
			}
			
			stopWatch.stop();
			
			System.out.println("ThreadSafeUtils.formatNumberを使う : " + stopWatch.getTime());
		}
		
		{
			// ThreadSafeUtils.formatNumberを使わない：ループの外でインスタンス生成
			StopWatch stopWatch = new StopWatch();
			stopWatch.start();
			
			DecimalFormat format = new DecimalFormat(pattern);
			
			for (int i = 0; i < count; i++) {
				format.format(i);
			}
			
			stopWatch.stop();
			
			System.out.println("ThreadSafeUtils.formatNumberを使わない（ループの外でインスタンス生成 ） : " + stopWatch.getTime());
		}
	}

}
