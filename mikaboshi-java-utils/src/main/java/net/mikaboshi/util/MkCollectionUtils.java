package net.mikaboshi.util;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Map;

import net.mikaboshi.validator.SimpleValidator;

/**
 * コレクションフレームワークに関するユーティリティクラス。
 * 
 * @author Takuma Umezawa
 * @since 1.1.5
 *
 */
public final class MkCollectionUtils {

	private MkCollectionUtils() {}
	
	
	/**
	 * コレクションを配列に変換する。
	 * @param <T> コレクション/配列の要素の型
	 * @param collection
	 * @param clazz
	 * @return
	 * @throws NullPointerException collectionまたはclazzがnullの場合
	 */
	public static <T> T[] toArray(Collection<T> collection, Class<T> clazz) {
		
		SimpleValidator.validateNotNull(collection, "collection");
		SimpleValidator.validateNotNull(clazz, "clazz");
		
		@SuppressWarnings("unchecked")
		T[] array = (T[]) Array.newInstance(clazz, collection.size());
		return collection.toArray(array);
	}
	
	/**
	 * コレクションを配列に変換する。
	 * このメソッドでは、引数の要素は1つ以上存在しなければならない。
	 * また、要素の型が均一でない（サブクラスのオブジェクトが入り混じっている）場合は使用できない。
	 * 
	 * @param <T>
	 * @param collection コレクション/配列の要素の型
	 * @return
	 * @throws NullPointerException collectionまたはclazzがnullの場合
	 * @throws IllegalArgumentException collectionの要素数が0の場合
	 * @throws ArrayStoreException 要素の型が均一でない場合
	 */
	public static <T> T[] toArray(Collection<T> collection) {
		
		SimpleValidator.validateNotNull(collection, "collection");
		SimpleValidator.validatePositive(
				collection.size(), "size of collection",
				IllegalArgumentException.class);
		
		@SuppressWarnings("unchecked")
		Class<T> clazz = (Class<T>) collection.toArray()[0].getClass();
		
		return toArray(collection, clazz);
	}
	
	/**
	 * 引数のコレクションがnullまたは要素数が0ならばtrueを返す。
	 * @param collection
	 * @return
	 */
	public static boolean isNullOrEmpty(Collection<?> collection) {
		return collection == null || collection.isEmpty();
	}
	
	/**
	 * 引数のマップがnullまたは要素数が0ならばtrueを返す。
	 * @param map
	 * @return
	 * @since 1.1.5
	 */
	public static boolean isNullOrEmpty(Map<?, ?> map) {
		return map == null || map.isEmpty();
	}
	
	/**
	 * 引数のコレクションがnullでなく、要素数が1以上ならばtrueを返す。
	 * @param collection
	 * @return
	 */
	public static boolean isNotEmpty(Collection<?> collection) {
		return !isNullOrEmpty(collection);
	}
	
	/**
	 * 引数のマップがnullでなく、要素数が1以上ならばtrueを返す。
	 * @param map
	 * @return
	 */
	public static boolean isNotEmpty(Map<?, ?> map) {
		return !isNullOrEmpty(map);
	}
}
