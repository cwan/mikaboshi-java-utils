package net.mikaboshi.property;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import net.mikaboshi.property.Property.Mode;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * <p>
 * Property アノテーションついたメソッドに値を設定するヘルパークラス。
 * </p><p>
 * プロパティの設定規則は、Property アノテーションのドキュメントを参照。
 * </p><p>
 * このクラスは、読み込み元の Properties および設定先のオブジェクトに対して
 * 同期は行わない。
 * </p>
 * @see Property
 * @author Takuma Umezawa
 * @since 0.1.2
 */
public final class PropertyUtils {
	
	private static Log logger = LogFactory.getLog(PropertyUtils.class); 

	private PropertyUtils() {}
	
	/**
	 * 引数で与えられたクラスから、プロパティ対象のメソッドとプロパティ名を抽出する。
	 * @param clazz プロパティを設定する対象のクラス
	 * @return　メソッドに対するプロパティ名を格納したマップ
	 * @throws IllegalArgumentException
	 *         メソッドの引数が不正な場合。
	 *         具体的には、数が1ではない場合、
	 *         または型が不正な場合（String, String配列、List以外）。
	 * 
	 */
	public static Map<Method, String> getPropertyNameMap(Class<?> clazz) {
		Map<Method, String> map = new HashMap<Method, String>();
		
		for (Method m : clazz.getMethods()) {
			if (!m.isAnnotationPresent(Property.class)) {
				continue;
			}
			
			if (m.getAnnotation(Property.class).mode() != Mode.SET) {
				continue;
			}

			if (m.getParameterTypes().length != 1 ||
				!( 	m.getParameterTypes()[0].equals(String.class) ||
					m.getParameterTypes()[0].equals(Boolean.class) ||
					m.getParameterTypes()[0].equals(Integer.class) ||
					m.getParameterTypes()[0].equals(Long.class) ||
					m.getParameterTypes()[0].equals(Double.class) ||
					m.getParameterTypes()[0].equals(String[].class) ||
					m.getParameterTypes()[0].equals(Boolean[].class) ||
					m.getParameterTypes()[0].equals(Integer[].class) ||
					m.getParameterTypes()[0].equals(Long[].class) ||
					m.getParameterTypes()[0].equals(Double[].class) ||
					m.getParameterTypes()[0].equals(List.class))) {
				
				throw new IllegalArgumentException(
					"プロパティの設定に失敗しました。" +
					"サポートされない引数型を持つメソッドに対してPropertyアノテーションが付けられています。" +
					"クラス名:" + clazz.getName() + ", " +
					"メソッド名:" + m.getName() + ", " +
					"引数型:" + Arrays.asList(m.getParameterTypes())
				);
			}
			
			String alias = m.getAnnotation(Property.class).alias();
			
			if (alias.length() != 0) {
				map.put(m, alias);
				continue;
			}
			
			String methodName = m.getName();
			
			if (methodName.startsWith("set") && methodName.length() > 3) {
				map.put(m, 
						Character.toLowerCase(methodName.charAt(3)) + 
						methodName.substring(4));
			} else {
				map.put(m, methodName);
			}
		}
		
		return map;
	}
	
	/**
	 * obj の Property アノテーションが付いたメソッドに、properties　から
	 * 取得したプロパティを引数に与えて invoke する。
	 * 
	 * @param obj プロパティを設定するオブジェクト
	 * @param properties プロパティの読み込み元
	 * @throws InvocationTargetException メソッドの invoke に失敗した場合
	 * @throws IllegalAccessException メソッドの invoke に失敗した場合
	 * @throws IllegalArgumentException メソッドの invoke に失敗した場合、
	 * 									または不正なアノテーションが付いていた場合
	 */
	public static void load(Object obj, Properties properties)
		throws IllegalAccessException, InvocationTargetException {
		
		Map<Method, String> map = getPropertyNameMap(obj.getClass());
		
		for (Method method : map.keySet()) {
			String propertyName = map.get(method);
			
			Object value = getPropertyByType(
					properties,
					propertyName,
					method.getParameterTypes()[0],
					method.getAnnotation(Property.class).elementType());
			
			if (value != null) {
				method.invoke(obj, new Object[] {value});
			}
		}
	}
	
	/**
	 * 型を指定してプロパティを取得する。
	 * 
	 * @param <T> 引数の型
	 * @param <U> 引数がListの場合、要素の型
	 * @param properties 読み込み元のプロパティ
	 * @param propertyName プロパティの名前（配列番号は付かない）
	 * @param type 引数の型
	 * @param elementType 引数がListの場合、要素の型
	 * @return
	 */
	static <T, U> T getPropertyByType(
			Properties properties, 
			String propertyName, 
			Class<T> type,
			Class<U> elementType) {
		
		if (type.equals(String.class)) {
			return type.cast(properties.getProperty(propertyName));
		}
		
		if (type.equals(Boolean.class)) {
			return type.cast(parseBoolean(
					properties.getProperty(propertyName),
					propertyName));
		}
		
		if (type.equals(Integer.class)) {
			return type.cast(parseInteger(
					properties.getProperty(propertyName),
					propertyName));
		}
		
		if (type.equals(Long.class)) {
			return type.cast(parseLong(
					properties.getProperty(propertyName),
					propertyName));
		}
		
		if (type.equals(Double.class)) {
			return type.cast(parseDouble(
					properties.getProperty(propertyName),
					propertyName));
		}
		
		if (type.equals(Object.class)) {
			return type.cast(properties.getProperty(propertyName));
		}
		
		if (Object[].class.isAssignableFrom(type)) {
			List<String> list = getArrayValues(properties, propertyName);
			
			if (type.equals(String[].class)) {
				return type.cast(list.toArray(new String[list.size()]));
			}
			
			if (type.equals(Boolean[].class)) {
				Boolean[] arr = new Boolean[list.size()];
				for (int i = 0; i < arr.length; i++) {
					arr[i] = parseBoolean(list.get(i), propertyName);
				}
				return type.cast(arr);
			}
			
			if (type.equals(Integer[].class)) {
				Integer[] arr = new Integer[list.size()];
				for (int i = 0; i < arr.length; i++) {
					arr[i] = parseInteger(list.get(i), propertyName);
				}
				return type.cast(arr);
			}
			
			if (type.equals(Integer[].class)) {
				Integer[] arr = new Integer[list.size()];
				for (int i = 0; i < arr.length; i++) {
					arr[i] = parseInteger(list.get(i), propertyName);
				}
				return type.cast(arr);
			}
			
			if (type.equals(Long[].class)) {
				Long[] arr = new Long[list.size()];
				for (int i = 0; i < arr.length; i++) {
					arr[i] = parseLong(list.get(i), propertyName);
				}
				return type.cast(arr);
			}
			
			if (type.equals(Double[].class)) {
				Double[] arr = new Double[list.size()];
				for (int i = 0; i < arr.length; i++) {
					arr[i] = parseDouble(list.get(i), propertyName);
				}
				return type.cast(arr);
			}
			
			if (type.equals(Object[].class)) {
				return type.cast(list.toArray());
			}
		}
		
		if (type.equals(List.class)) {
			List<String> sList = getArrayValues(properties, propertyName);
			
			if (elementType.equals(String.class) ||
				elementType.equals(Object.class)) {
				return type.cast(sList);
			}
			
			if (elementType.equals(Boolean.class)) {
				List<Boolean> list = new ArrayList<Boolean>(sList.size());
				for (String s : sList) {
					list.add(parseBoolean(s, propertyName));
				}
				return type.cast(list);
			}
			
			if (elementType.equals(Integer.class)) {
				List<Integer> list = new ArrayList<Integer>(sList.size());
				for (String s : sList) {
					list.add(parseInteger(s, propertyName));
				}
				return type.cast(list);
			}
			
			if (elementType.equals(Long.class)) {
				List<Long> list = new ArrayList<Long>(sList.size());
				for (String s : sList) {
					list.add(parseLong(s, propertyName));
				}
				return type.cast(list);
			}
			
			if (elementType.equals(Double.class)) {
				List<Double> list = new ArrayList<Double>(sList.size());
				for (String s : sList) {
					list.add(parseDouble(s, propertyName));
				}
				return type.cast(list);
			}
		}

		throw new IllegalArgumentException(
				"Illegal parameter type : " + type.getName());
	}
	
	static Boolean parseBoolean(String s, String propName) {
		if (s == null) {
			return null;
		}
		
		if ("true".equalsIgnoreCase(s)) {
			return Boolean.TRUE;
		} else if ("false".equalsIgnoreCase(s)) {
			return Boolean.FALSE;
		} else {
			if (logger.isDebugEnabled()) {
				logger.debug("Illegal value as string <" + s + "> @ " + propName);
			}
			return null;
		}
	}
	
	static Integer parseInteger(String s, String propName) {
		if (s == null) {
			return null;
		}
		
		try {
			return Integer.parseInt(s);
		} catch (NumberFormatException e) {
			if (logger.isDebugEnabled()) {
				logger.debug("Illegal value as integer <" + s + "> @ " + propName);
			}
			return null;
		}
	}
	
	static Long parseLong(String s, String propName) {
		if (s == null) {
			return null;
		}
		
		try {
			return Long.parseLong(s);
		} catch (NumberFormatException e) {
			if (logger.isDebugEnabled()) {
				logger.debug("Illegal value as long <" + s + "> @ " + propName);
			}
			return null;
		}
	}
	
	static Double parseDouble(String s, String propName) {
		if (s == null) {
			return null;
		}
		
		try {
			return Double.parseDouble(s);
		} catch (NumberFormatException e) {
			if (logger.isDebugEnabled()) {
				logger.debug("Illegal value as double <" + s + "> @ " + propName);
			}
			return null;
		}
	}
	
	/**
	 * obj のProperty アノテーション（type = Mode.GET）が付いたメソッドから
	 * 取得した値を設定した新しい Properties オブジェクトを返す。
	 * 1つのプロパティがない場合でも、空のProperties オブジェクトを返す。
	 * 
	 * @param obj プロパティを取得するオブジェクト
	 * @return　プロパティの書き出し先
	 * @throws IllegalAccessException メソッドの invoke に失敗した場合
	 * @throws InvocationTargetException メソッドの invoke に失敗した場合
	 * @throws IllegalArgumentException メソッドの invoke に失敗した場合
	 * 									または不正なアノテーションが付いていた場合
	 */
	public static Properties store(Object obj) 
		throws IllegalAccessException, InvocationTargetException {
		
		Properties properties = new Properties();
		
		for (Method m : obj.getClass().getMethods()) {
			if (!m.isAnnotationPresent(Property.class)) {
				continue;
			}
			
			if (m.getAnnotation(Property.class).mode() != Mode.GET) {
				continue;
			}
			
			if (m.getParameterTypes().length != 0) {
				throw new IllegalArgumentException(
						"プロパティの取得に失敗しました。引数が存在します。" +
						"クラス名:" + m.getClass().getName() + ", " +
						"メソッド名:" + m.getName() + ", " +
						"引数:" + Arrays.asList(m.getParameterTypes())
				);
			}
			
			Class<?> returnType = m.getReturnType();
			
			if (returnType == null) {
				throw new IllegalArgumentException(
						"プロパティの取得に失敗しました。戻り値がvoidです。" +
						"クラス名:" + m.getClass().getName() + ", " +
						"メソッド名:" + m.getName()
				);
			}
			
			String propertyName = getStorePropertyName(m);
			Object value = m.invoke(obj);
			
			if (value == null) {
				continue;
			}
			
			if (Object[].class.isAssignableFrom(returnType)) {
				int index = 0;
				for (Object o : (Object[]) value) {
					if (o != null) {
						properties.put(
							propertyName + "[" + index++ + "]", o.toString());
					}
				}
				continue;
			}
			
			if (returnType.equals(List.class)) {
				int index = 0;
				for (Object o : (List<?>) value) {
					if (o != null) {
						properties.put(
							propertyName + "[" + index++ + "]", o.toString());
					}
				}
				continue;
			}
			
			// その他の型
			properties.put(propertyName, value.toString());
		}
		
		return properties;
	}
	
	/**
	 * 書き込むプロパティの名前を決める。
	 * @param m
	 * @return
	 */
	private static String getStorePropertyName(Method m) {
		String alias = m.getAnnotation(Property.class).alias();
		
		if (alias.length() != 0) {
			return alias;
		}
		
		String name = m.getName();
		
		if (name.startsWith("get") && name.length() > 3) {
			return Character.toLowerCase(name.charAt(3)) + name.substring(4);
		}
		
		return name;
	}
	
	
	
	/**
	 * 引数 propertyName に配列番号が付いたプロパティのリストを取得する。 
	 * 一致するプロパティが1つも無い場合は、空のリストが返る。
	 * 
	 * @param properties
	 * @param propertyName
	 * @return
	 */
	public static List<String> getArrayValues(
			Properties properties, String propertyName) {
		List<String> list = new ArrayList<String>();
		
		for (int i = 0; i < Integer.MAX_VALUE; i++) {
			
			// 注) ArrayListの最大要素数はInteger.MAX_VALUEなので、
			//    それ以上は設定しない。
			
			String value = properties.getProperty(
					propertyName + "[" + i + "]");
			
			if (value == null) {
				// 見つからなかった時点で終了（番号のスキップは認めない）
				break;
			}
			
			list.add(value);
		}
		
		return list;
	}
	
}
