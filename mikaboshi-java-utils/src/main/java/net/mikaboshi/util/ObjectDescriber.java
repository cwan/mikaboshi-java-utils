package net.mikaboshi.util;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import net.mikaboshi.validator.SimpleValidator;


/**
 * <p>
 * オブジェクトの内容を文字列化する。
 * </p>
 * <p>
 * mode = FIELD を指定した場合、{@link Class#getDeclaredFields()}} で取得できる
 * すべてのフィールドを書き出す。フィールドの型が String, Number, Boolean 以外の場合、
 * クラス名だけを書き出す。
 * </p>
 * <p>
 * mode = ACCESSOR を指定した場合、{@link Class#getMethods()}} で取得できる
 * メソッドで、戻り値が void ではなく、引数がないもので、かつメソッド名が get で始まるもの、
 * およびメソッド名が is / has で始まり、戻り値が Boolean のメソッドの戻り値を書き出す。
 * 戻り値の型が String, Number, Boolean 以外の場合、クラス名だけを書き出す。
 * </p>
 * <p>
 * mode = FIELD_RECURSIVE を指定した場合、{@link Class#getDeclaredFields()}} で取得できる
 * すべてのフィールドを書き出す。フィールドの型が String, Number, Boolean 以外の場合、
 * 再帰的な探索を行う。
 * </p>
 * <p>
 * mode = ACCESSOR を指定した場合、{@link Class#getMethods()}} で取得できる
 * メソッドで、戻り値が void ではなく、引数がないもので、かつメソッド名が get で始まるもの、
 * およびメソッド名が is / has で始まり、戻り値が Boolean のメソッドの戻り値を書き出す。
 * 戻り値の型が String, Number, Boolean 以外の場合、再帰的な探索を行う。
 * </p>
 * <p>
 * 値は、以下の形式で書き出される。
 * </p>
 * <p>
 * <b>CharacterSequence, Number, Boolean</b><br>
 * {@code "値" "<クラス名@ハッシュ値>"}
 * </p>
 * <p>
 * <b>配列</b><br>
 * {@code ["値1" "<クラス名@ハッシュ値>", "値2" "<クラス名@ハッシュ値>"]} 
 * </p>
 * <p>
 * <b>コレクション</b><br>
 * {@code ["値1" "<クラス名@ハッシュ値>", "値2" "<クラス名@ハッシュ値>"] "<コレクションクラス名@ハッシュ値>"}
 * </p>
 * <p>
 * <b>マップ</b><br>
 * {@code {"キー1" "<クラス名@ハッシュ値>" : "値1" "<クラス名@ハッシュ値>", "キー2" "<クラス名@ハッシュ値>" : "値2" "<クラス名@ハッシュ値>"}
 * </p>
 * <b>その他オブジェクト</b><br>
 * {@code {"フィールド名1" : "値1" "<クラス名@ハッシュ値>", "フィールド名2" : "値2" "<クラス名@ハッシュ値>"]}
 * </p>
 * <p>
 * 循環参照を検出した場合、"(Circular reference)" と出力する。
 * </p>
 * <p>
 * このクラスでは、同期処理を行わない。
 * </p>
 * @author Takuma Umezawa
 * @since 1.1.5
 */
public class ObjectDescriber {

	/** オブジェクト内容の取得方法 */
	public static enum Mode {
		
		/** フィールド */
		FIELD,
		
		/** アクセサメソッド */
		ACCESSOR,
		
		/** フィールド（再帰） */
		FIELD_RECURSIVE,
		
		/** アクセサメソッド（再帰） */
		ACCESSOR_RECURSIVE
	}
	
	private StringBuilder buffer = new StringBuilder();
	
	/** 循環参照のチェック */
	private Set<Object> circularRefCheck = new HashSet<Object>();
	
	private Mode mode;
	
	/**
	 * デフォルトコンストラクタ。
	 * mode = FIELD を適用する。
	 */
	public ObjectDescriber() {
		this(Mode.FIELD);
	}
	
	/**
	 * モードを指定するコンストラクタ。
	 * @param mode
	 */
	public ObjectDescriber(Mode mode) {
		
		SimpleValidator.validateNotNull(mode, "mode");
		
		this.mode = mode;
	}
	
	public String toString(Object object) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		return toString(null, object);
	}
	
	public String toString(String name, Object object) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		
		this.circularRefCheck.add(object);
		
		append(name, object);
		
		return this.buffer.toString();
	}
	
	private void append(String name, Object object) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		
		if (name != null) {
			quote(name);
			this.buffer.append(" : ");
		}
		
		append(object);
	}
	
	private void append(Object object) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		
		if (object == null) {
			this.buffer.append("null");
			return;
		}
		
		if (object instanceof CharSequence ||
			object instanceof Number ||
			object instanceof Boolean) {
			
			quote(object.toString());
			appendClassNameAndHashCode(object);
			
			return;
		}
		
		if (object instanceof Object[]) {
			
			Object[] array = (Object[]) object;
			
			this.buffer.append("[");
			
			for (int i = 0; i < array.length; i++) {
				
				if (i != 0) {
					this.buffer.append(", ");
				}
				
				append(array[i]);
			}
			
			this.buffer.append("]");
			
			return;
		}
		
		if (object instanceof Collection<?>) {
			
			Collection<?> collection = (Collection<?>) object;
			
			this.buffer.append("[");
			
			int i = 0;
			for (Iterator<?> iter = collection.iterator(); iter.hasNext();) {
				
				if (i++ != 0) {
					this.buffer.append(", ");
				}
				
				append(iter.next());
			}
			
			this.buffer.append("]");
			appendClassNameAndHashCode(collection);
			
			return;
		}
		
		if (object instanceof Map<?, ?>) {
			
			this.buffer.append("{");
			
			int i = 0;
			
			for (Map.Entry<?, ?> entry : ((Map<?, ?>) object).entrySet()) {
				
				if (i++ != 0) {
					this.buffer.append(", ");
				}
				
				append(entry.getKey());
				
				this.buffer.append(" : ");
				
				append(entry.getValue());
			}
			
			this.buffer.append("}");
			
			appendClassNameAndHashCode(object);
			
			return;
		}
		
		// その他
		{
			this.buffer.append("{");
			
			switch (this.mode) {
				case FIELD:
				case FIELD_RECURSIVE:
					// リフレクションでフィールドの値を出力する
				
					Field[] fields = object.getClass().getDeclaredFields();
					
					for (int i = 0; i < fields.length; i++) {
						
						if (i != 0) {
							this.buffer.append(", ");
						}
						
						Field field = fields[i];
						field.setAccessible(true);
						
						Object value = field.get(object);
						
						if (value == null ||
							value instanceof CharSequence ||
							value instanceof Number ||
							value instanceof Boolean) {
							
							append(field.getName(), value);
							
						} else if (value instanceof Class<?>) {
							
							quote(field.getName());
							this.buffer.append(" :");
							appendClassNameAndHashCode(value);
							
						} else {
							
							if (this.mode == Mode.FIELD_RECURSIVE) {
								if ( this.circularRefCheck.contains(value) ) {
									quote(field.getName());
									this.buffer.append(" : (Circular reference)");
									appendClassNameAndHashCode(value);
									
								} else {
									this.circularRefCheck.add(value);
									append(field.getName(), value);
								}
							} else {
								quote(field.getName());
								this.buffer.append(" :");
								appendClassNameAndHashCode(value);
							}
						}
					}
						
					break;
				
				case ACCESSOR:
				case ACCESSOR_RECURSIVE:
	
					// publicのgetXxx, isXxxメソッドの値を出力する
					
					int i = 0;
					
					for (Method method : object.getClass().getMethods()) {
						
						if (method.getParameterTypes().length != 0) {
							continue;
						}
						
						if (method.getReturnType() == null || 
							"void".equals(method.getReturnType().toString())) {
							// ※ 戻り値がvoidの場合、API仕様では何も書いていないが、
							//   toStringが"void"のクラスが返る
							continue;
						}
						
						if ( "getClass".equals(method.getName()) ) {
							continue;
						}
						
						String propertyName = null;
						
						if (method.getName().length() > 3 && 
							method.getName().startsWith("get")) {
							
							propertyName = method.getName().substring(3);
							
						} else if ( method.getName().length() > 2 && 
									method.getName().startsWith("is") ) {
							
							String returnType = method.getReturnType().getName();
							
							if ("java.lang.Boolean".equals(returnType) || "boolean".equals(returnType)) {
								propertyName = method.getName().substring(2);
							}
						} else if ( method.getName().length() > 3 && 
									method.getName().startsWith("has") ) {
							
							String returnType = method.getReturnType().getName();
							
							if ("java.lang.Boolean".equals(returnType) || "boolean".equals(returnType)) {
								propertyName = method.getName().substring(3);
							}
						}
						
						if (propertyName == null) {
							continue;
						}
						
						if (propertyName.length() > 1) {
							propertyName = Character.toLowerCase(propertyName.charAt(0)) + propertyName.substring(1);
						} else {
							propertyName = propertyName.toLowerCase();
						}
						
						if (i++ != 0) {
							this.buffer.append(", ");
						}
						
						method.setAccessible(true);
						Object value;
						
						try {
							value = method.invoke(object);
						} catch (InvocationTargetException ex) {
							value = ex.getCause();
						}
						
						if (value == null ||
							value instanceof CharSequence ||
							value instanceof Number ||
							value instanceof Boolean) {
							
							append(propertyName, value);
							
						} else if (value instanceof Class<?>) {
							
							quote(propertyName);
							this.buffer.append(" :");
							appendClassNameAndHashCode(value);
							
						} else {
							
							if (this.mode == Mode.ACCESSOR_RECURSIVE) {
							
								if ( this.circularRefCheck.contains(value) ) {
									quote(propertyName);
									this.buffer.append(" : (Circular reference)");
									appendClassNameAndHashCode(value);
									
								} else {
									this.circularRefCheck.add(value);
									append(propertyName, value);
								}
							} else {
								quote(propertyName);
								this.buffer.append(" :");
								appendClassNameAndHashCode(value);
							}
						}
					}
					
					break;
				
				default:
			}

			
			this.buffer.append("}");
			
			appendClassNameAndHashCode(object);
		}
	}
	
	private void appendClassNameAndHashCode(Object object) {
		if (object != null) {
			this.buffer.append(" \"<");
			this.buffer.append( escape(object.getClass().getName()) );
			this.buffer.append("@");
			this.buffer.append(Integer.toHexString(object.hashCode()));
			this.buffer.append(">\"");
		}
	}
	
	/**
	 * 引用符をつけてバッファに追加する。
	 * @param string
	 */
	private void quote(String string) {
		
		if (string == null) {
			this.buffer.append(string);
			return;
		}
		
		this.buffer.append('"');
		
		this.buffer.append( escape(string) );
		
		this.buffer.append('"');
	}
	
	private String escape(String string) {
		
		if (string == null) {
			return null;
		}
		
		return string.replace("\\", "\\\\").replace("\"", "\\\"");
	}
	
}
