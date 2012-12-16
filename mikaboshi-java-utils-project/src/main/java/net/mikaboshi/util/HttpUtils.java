package net.mikaboshi.util;

import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import javax.servlet.ServletContext;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import net.mikaboshi.validator.SimpleValidator;

/**
 * HTTPに関するユーティリティクラス。
 * 
 * @author Takuma Umezawa
 * @since 1.1.5
 */
public final class HttpUtils {
	
	private HttpUtils() {};
	
	/**
	 * 指定日時のヘッダタイムスタンプ文字列を取得する。
	 * 例：　Sat, 26 Jun 2010 07:23:32 GMT
	 * 
	 * @param date
	 * @return
	 */
	public static String getTimeStamp(Date date) {
		
		SimpleValidator.validateNotNull(date, "date");
		
		return ThreadSafeUtils.formatDate(
				date, 
				"EEE, dd MMM yyyy HH:mm:ss z",
				Locale.US,
				TimeZone.getTimeZone("GMT"));
	}
	
	/**
	 * システム日時のヘッダタイムスタンプ文字列を取得する。
	 * 
	 * @see #getTimeStamp(Date)
	 * @return
	 */
	public static String getTimeStamp() {
		return getTimeStamp(new Date());
	}
	
	/**
	 * HTTPリクエストヘッダを取得する。
	 * ヘッダ情報が取得できない場合はnullを返す。
	 * @param request
	 * @return
	 */
	public static Map<String, List<String>> getRequestHeaders(HttpServletRequest request) {
		
		SimpleValidator.validateNotNull(request, "request");
		
		@SuppressWarnings("unchecked")
		Enumeration<String> headerNames = request.getHeaderNames();
		
		if (headerNames == null) {
			return null;
		}
		
		Map<String, List<String>> result = new HashMap<String, List<String>>();
		
		while (	headerNames.hasMoreElements() ) {
			
			String headerName = headerNames.nextElement();
			
			@SuppressWarnings("unchecked")
			Enumeration<String> headerValues = request.getHeaders(headerName);
			
			if (headerValues == null) {
				continue;
			}
			
			List<String> headerValueList = new ArrayList<String>();
			
			while ( headerValues.hasMoreElements() ) {
				headerValueList.add(headerValues.nextElement());
			}
			
			result.put(headerName, headerValueList);
		}
		
		return result;
	}
	
	/**
	 * リクエストアトリビュートを取得する。
	 * @param request
	 * @return
	 */
	public static Map<String, Object> getRequestAttributes(ServletRequest request) {
		
		SimpleValidator.validateNotNull(request, "request");
		
		Map<String, Object> result = new HashMap<String, Object>();
		
		@SuppressWarnings("unchecked")
		Enumeration<String> attributeNames = request.getAttributeNames();
		
		while ( attributeNames.hasMoreElements() ) {
			String name = attributeNames.nextElement();
			result.put(name, request.getAttribute(name));
		}
		
		return result;
	}
	
	/**
	 * セッションアトリビュートを取得する。
	 * 注) このメソッドでは、同期を行わない。
	 * @param session
	 * @return
	 */
	public static Map<String, Object> getSessionAttributes(HttpSession session) {
		
		SimpleValidator.validateNotNull(session, "session");
		
		Map<String, Object> result = new HashMap<String, Object>();
		
		@SuppressWarnings("unchecked")
		Enumeration<String> attributeNames = session.getAttributeNames();
		
		while ( attributeNames.hasMoreElements() ) {
			String name = attributeNames.nextElement();
			result.put(name, session.getAttribute(name));
		}
		
		return result;
	}
	
	/**
	 * コンテキストアトリビュートを取得する。
	 * 注) このメソッドでは、同期を行わない。
	 * @param context
	 * @return
	 */
	public static Map<String, Object> getContextAttributes(ServletContext context) {
		
		SimpleValidator.validateNotNull(context, "context");
		
		Map<String, Object> result = new HashMap<String, Object>();
		
		@SuppressWarnings("unchecked")
		Enumeration<String> attributeNames = context.getAttributeNames();
		
		while ( attributeNames.hasMoreElements() ) {
			String name = attributeNames.nextElement();
			result.put(name, context.getAttribute(name));
		}
		
		return result;
	}
	
	/**
	 * クライアントロケールを取得する。
	 * @param request
	 * @return
	 */
	public static List<Locale> getLocales(ServletRequest request) {
		
		SimpleValidator.validateNotNull(request, "request");
		
		@SuppressWarnings("unchecked")
		Enumeration<Locale> locales = request.getLocales();
		
		List<Locale> result = new ArrayList<Locale>();
		
		while ( locales.hasMoreElements() ) {
			result.add(locales.nextElement());
		}
		
		return result;
	}
}
