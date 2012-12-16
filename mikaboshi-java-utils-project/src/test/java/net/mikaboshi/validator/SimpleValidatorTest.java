package net.mikaboshi.validator;


import static net.mikaboshi.validator.SimpleValidator.validateNotBlank;
import static net.mikaboshi.validator.SimpleValidator.validateNotContainsInvalidCharactor;
import static net.mikaboshi.validator.SimpleValidator.validateNotNull;
import static net.mikaboshi.validator.SimpleValidator.validateNotNullJust1;
import static net.mikaboshi.validator.SimpleValidator.validateNotNullNorEmpty;
import static net.mikaboshi.validator.SimpleValidator.validateNotNullNorLength0;
import static net.mikaboshi.validator.SimpleValidator.validatePattern;
import static net.mikaboshi.validator.SimpleValidator.validatePositiveOrZero;
import static org.junit.Assert.assertEquals;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class SimpleValidatorTest {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}
	
	@Test
	public void testNotNull_NotNull() {
		validateNotNull("非null", "入力値1", ValidatorException.class);
	}
	
	@Test(expected = ValidatorException.class)
	public void testNotNull_Null() {
		validateNotNull(null, "入力値2", ValidatorException.class);
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testValidateNotNullJust1_AllNull() {
		Map<String, Object> data = new HashMap<String, Object>();
		
		data.put("data1", null);
		data.put("data2", null);
		data.put("data3", null);
		
		validateNotNullJust1(data, IllegalArgumentException.class);
	}
	
	@Test
	public void testValidateNotNullJust1_OK() {
		Map<String, Object> data = new HashMap<String, Object>();
		
		data.put("data1", "first");
		data.put("data2", null);
		data.put("data3", null);
		
		validateNotNullJust1(data, IllegalArgumentException.class);
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testValidateNotNullJust1_2Data() {
		Map<String, Object> data = new HashMap<String, Object>();
		
		data.put("data1", new ArrayList<Object>());
		data.put("data2", null);
		data.put("data3", new Integer(10));
		
		validateNotNullJust1(data, IllegalArgumentException.class);
	}
	
	@Test
	public void testValidateNotContainsInvalidCharactor_Blank() {
		validateNotContainsInvalidCharactor(
				"ABC",
				"データ",
				new char[] {},
				RuntimeException.class);
	}

	@Test(expected = RuntimeException.class)
	public void testValidateNotContainsInvalidCharactor_NG() {
		validateNotContainsInvalidCharactor(
				"ABC",
				"データ",
				new char[] {'B'},
				RuntimeException.class);
	}
	
	@Test
	public void testValidateNotBlank_OK() {
		validateNotBlank("aaa", "AAA", ValidatorException.class);
	}
	
	@Test(expected = ValidatorException.class)
	public void testValidateNotBlank_NG() {
		validateNotBlank("\t\r\n", "AAA", ValidatorException.class);
	}
	
	@Test
	public void testValidateNotNullNorLength0_OK() {
		validateNotNullNorLength0("\t", "AAA", ValidatorException.class);
	}
	
	@Test(expected = ValidatorException.class)
	public void testValidateNotNullNorLength0_NG1() {
		validateNotNullNorLength0("", "AAA", ValidatorException.class);
	}
	
	@Test(expected = ValidatorException.class)
	public void testValidateNotNullNorLength0_NG2() {
		validateNotNullNorLength0(null, "AAA", ValidatorException.class);
	}
	
	@Test
	public void testValidatePositiveOrZero_0() {
		validatePositiveOrZero(0, "aaa", IllegalArgumentException.class);
	}
	
	@Test
	public void testValidatePositiveOrZero_1() {
		validatePositiveOrZero(1, "aaa", IllegalArgumentException.class);
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testValidatePositiveOrZero_Negative() {
		validatePositiveOrZero(-1, "aaa", IllegalArgumentException.class);
	}
	
	@Test(expected = ValidatorError.class)
	public void testThrowException_NoArg() {
		validatePositiveOrZero(-1, "aaa", NoArgException.class);
	}
	
	@SuppressWarnings("serial")
	class NoArgException extends RuntimeException {
		public NoArgException() {
		}
	}
	
	@Test
	public void testValidatePattern_OK() {
		validatePattern("abc123", "[a-z]+[1-9]+", "aaa", IllegalArgumentException.class);
	}
	
	
	@Test(expected = IllegalArgumentException.class)
	public void testValidatePattern_NG() {
		validatePattern("abc123", "[a-z]+[1-9]{2}$", "aaa", IllegalArgumentException.class);
	}
	
	/**
	 * パターンのキャッシュを使う場合。
	 */
	@Test
	public void testValidatePattern_Cache() {
		validatePattern("abc123", "[a-z]+[1-9]+", "aaa", IllegalArgumentException.class);
		
		validatePattern("abczzzed99993", "[a-z]+[1-9]+", "bbb", IllegalArgumentException.class);
	}	
	
	@Test
	public void testValidateNotNullNorEmpty_OK() {
		List<String> list = new ArrayList<String>();
		list.add("aaa");
		validateNotNullNorEmpty(list, "list", ValidatorException.class);
	}
	
	@Test(expected = ValidatorException.class)
	public void testValidateNotNullNorEmpty_NG() {
		validateNotNullNorEmpty(null, "list", ValidatorException.class);
	}
	
	@Test(expected = ValidatorException.class)
	public void testExceptionClassIsNull() throws ValidatorError, Throwable {
		validateNotBlank("", "xxx", null);
	}
	
	@Test
	public void testAbsentMessage() throws SecurityException, NoSuchMethodException, IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		Object result = SimpleValidator.get("xxxxx", new Object[] {});
		assertEquals("xxxxx", result);
		
	}
}
