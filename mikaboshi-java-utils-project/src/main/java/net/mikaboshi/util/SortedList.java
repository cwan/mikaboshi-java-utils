package net.mikaboshi.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import net.mikaboshi.validator.SimpleValidator;

/**
 * <p>
 * 要素の追加時に挿入位置を決め、順序を保証するリスト。
 * </p>
 * <p>
 * 挿入位置は、コンストラクタ引数で渡された{@link Comparator}オブジェクトを使用する。
 *　Comparatorを使用しない場合は、{@link #newInstance(Class)}でインスタンスを生成する。
 * この場合、順序の比較は{@link Comparable#compareTo(Object)}が使用される。
 * </p>
 * <p>
 * 順序が保証できなくなるため、{@link #add(int, Collection)}, 
 * {@link #addAll(int, Collection)}, {@link #set(int, Object)}を
 * このクラスで使用することはできない。
 * </p>
 * <p>
 * 単に、ソートされたリストが欲しい場合には{@link  ArrayList}を使用して、最後に
 * {@link Collections#sort(List)}を一回実行したほうが速い。
 * しかし、{@link #contains(Object)}や{@link #indexOf(Object)},
 * {@link #lastIndexOf(Object)}を頻繁に実行する場合は、このクラスを使用した方が速くなることがある。
 * </p>
 * <p>
 * Listの実装は、{@link  ArrayList}を使用する。従って、このクラスは同期化されない。
 * </p>
 * 
 * @author Takuma Umezawa
 * @since 1.1.6
 *
 * @param <E> リストの要素
 */
public class SortedList<E> implements List<E> {
	
	private List<E> list = new ArrayList<E>();
	
	private Comparator<E> comparator = null;
	
	/**
	 * デフォルトコンストラクタ。
	 * 追加の際の要素の比較には、要素自身の{@link Comparable#compareTo}メソッドを使用する。
	 * そのため、この場合は要素はComparableインターフェースを実装している必要がある。
	 * 要素の型が呼び出し側から指定できないため、デフォルトコンストラクタはprivateにして、
	 * {@link #newInstance(Class)}メソッドでインスタンスを生成することにする。
	 */
	private SortedList() {
	}
	
	/**
	 * Comparatorを指定するコンストラクタ。
	 * 追加の際の要素の比較には、引数のComrapatorを使用する。
	 * 
	 * @param comparator
	 * @throws NullPointerException comparatorがnullの場合にスローされる。
	 */
	public SortedList(Comparator<E> comparator) {
		SimpleValidator.validateNotNull(comparator, "comparator");
		this.comparator = comparator;
	}
	
	/**
	 * Comparatorを指定しないで、SortedListインスタンスを生成する。
	 * 追加の際の要素の比較には、引数のクラスの{@link Comparable#compareTo(Object)}を使用する。
	 * 
	 * @param <T> 要素の型
	 * @param clazz
	 * @return
	 */
	public static <T extends Comparable<T>> SortedList<T> newInstance(Class<T> clazz) {
		return new SortedList<T>();
	}

	/* (非 Javadoc)
	 * @see java.util.List#size()
	 */
	public int size() {
		return this.list.size();
	}

	/* (非 Javadoc)
	 * @see java.util.List#isEmpty()
	 */
	public boolean isEmpty() {
		return this.list.isEmpty();
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @throws ClassCastException
	 * 				デフォルトコンストラクタを使用し、
	 * 				oがComparableインターフェースを実装していない場合にスローされる。
	 */
	@SuppressWarnings("unchecked")
	public boolean contains(Object o) {
		return binarySearch((E) o) >= 0;
	}

	/* (非 Javadoc)
	 * @see java.util.List#iterator()
	 */
	public Iterator<E> iterator() {
		return this.list.iterator();
	}

	/* (非 Javadoc)
	 * @see java.util.List#toArray()
	 */
	public Object[] toArray() {
		return this.list.toArray();
	}

	/* (非 Javadoc)
	 * @see java.util.List#toArray(T[])
	 */
	public <T> T[] toArray(T[] a) {
		return this.list.toArray(a);
	}

	/**
	 * {@inheritDoc}
	 * 
	 * <p>
	 * リストの順序を保証して要素を追加する。
	 * </p>
	 * <p>
	 * 同じ順列の要素がすでに存在する場合は、追加要素の位置は保証しない。
	 * </p>
	 * <p>
	 * コンストラクタで{@link Comparator}が指定されていない場合、引数eがnullの場合は
	 * {@link NullPointerException}がスローされる。
	 * コンストラクタでComparatorが指定されている場合、nullの扱いは
	 * Comparatorの実装に依存する。
	 * </p>
	 * 
	 * @throws ClassCastException
	 * 				デフォルトコンストラクタを使用し、
	 * 				oがComparableインターフェースを実装していない場合にスローされる。
	 */
	public boolean add(E e) {
		
		int pos = binarySearch(e);
		
		if (pos < 0) {
			this.list.add(-pos - 1, e);
		} else {
			this.list.add(pos + 1, e);
		}
		
		return true;
	}

	/* (非 Javadoc)
	 * @see java.util.List#remove(java.lang.Object)
	 */
	public boolean remove(Object o) {
		return this.list.remove(o);
	}

	/* (非 Javadoc)
	 * @see java.util.List#containsAll(java.util.Collection)
	 */
	public boolean containsAll(Collection<?> c) {
		return this.list.containsAll(c);
	}

	/**
	 * {@inheritDoc}
	 * 
	 * <p>
	 * このメソッドで追加した場合も、順序は保障される。
	 * </p>
	 * 
	 * @throws ClassCastException
	 * 				デフォルトコンストラクタを使用し、
	 * 				oがComparableインターフェースを実装していない場合にスローされる。
	 */
	public boolean addAll(Collection<? extends E> c) {
		
		boolean modified = false;
		
		// 順序を保障するため、	このクラスをaddを使う
		for (E e : c) {
			add(e);
			modified = true;
		}
		
		return modified;
	}

	/**
	 * 順序が保証できないため、このクラスでは使用できない。
	 * @throws UnsupportedOperationException 常にスローされる。
	 */
	public boolean addAll(int index, Collection<? extends E> c) {
		throw new UnsupportedOperationException();
	}

	/* (非 Javadoc)
	 * @see java.util.List#removeAll(java.util.Collection)
	 */
	public boolean removeAll(Collection<?> c) {
		return this.list.removeAll(c);
	}

	/* (非 Javadoc)
	 * @see java.util.List#retainAll(java.util.Collection)
	 */
	public boolean retainAll(Collection<?> c) {
		return this.list.retainAll(c);
	}

	/* (非 Javadoc)
	 * @see java.util.List#clear()
	 */
	public void clear() {
		this.list.clear();
	}

	/* (非 Javadoc)
	 * @see java.util.List#get(int)
	 */
	public E get(int index) {
		return this.list.get(index);
	}

	/**
	 * 順序が保証できないため、このクラスでは使用できない。
	 * @throws UnsupportedOperationException 常にスローされる。
	 */
	public E set(int index, E element) {
		throw new UnsupportedOperationException();
	}

	/**
	 * 順序が保証できないため、このクラスでは使用できない。
	 * @throws UnsupportedOperationException 常にスローされる。
	 */
	public void add(int index, E element) {
		throw new UnsupportedOperationException();
	}

	/* (非 Javadoc)
	 * @see java.util.List#remove(int)
	 */
	public E remove(int index) {
		return this.list.remove(index);
	}

	/**
	 * {@inheritDoc}
	 * 
	 * <p>
	 * リストがソートされているという前提で、リスト中の最初の出現位置を取得する。
	 * コンストラクタで{@link Comparator}が指定されていない場合は、それを使用する。
	 * </p>
	 * <p>
	 * コンストラクタでComparatorが指定されていない場合、引数eがnullの場合は
	 * {@link NullPointerException}がスローされる。
	 * コンストラクタでComparatorが指定されている場合、nullの扱いは
	 * Comparatorの実装に依存する。
	 * </p>
	 * 
	 * @throws ClassCastException
	 * 				デフォルトコンストラクタを使用し、
	 * 				oがComparableインターフェースを実装していない場合にスローされる。
	 */
	public int indexOf(Object o) {
		
		@SuppressWarnings("unchecked")
		E e1 = (E) o;
		
		int pos = binarySearch(e1);
		
		if (pos < 0) {
			return -1;
		}
		
		// 最初の出現位置を調べるため、リストを１つずつ前方向に検索する
		while (pos >= 0) {
			
			E e2 = this.list.get(pos);
			
			if ( compare(e1, e2) != 0 ) {
				return pos + 1;
			}
			
			pos--;
		}
		
		// リストの先頭まで等しかった場合
		return 0;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * <p>
	 * リストがソートされているという前提で、リスト中の最後の出現位置を取得する。
	 * コンストラクタで{@link Comparator}が指定されていない場合は、それを使用する。
	 * </p>
	 * <p>
	 * コンストラクタでComparatorが指定されていない場合、引数eがnullの場合は
	 * {@link NullPointerException}がスローされる。
	 * コンストラクタでComparatorが指定されている場合、nullの扱いは
	 * Comparatorの実装に依存する。
	 * </p>
	 * 
	 * @throws ClassCastException
	 * 				デフォルトコンストラクタを使用し、
	 * 				oがComparableインターフェースを実装していない場合にスローされる。
	 */
	public int lastIndexOf(Object o) {

		@SuppressWarnings("unchecked")
		E e1 = (E) o;
		
		int pos = binarySearch(e1);
		
		if (pos < 0) {
			return -1;
		}
		
		// 最後の出現位置を調べるため、リストを１つずつ後方向に検索する
		
		int lastIndex = this.list.size() - 1;
		
		while (pos <= lastIndex) {
			
			E e2 = this.list.get(pos);
			
			if ( compare(e1, e2) != 0 ) {
				return pos - 1;
			}
			
			pos++;
		}
		
		// リストの最後まで等しかった場合
		return lastIndex;
	}

	/* (非 Javadoc)
	 * @see java.util.List#listIterator()
	 */
	public ListIterator<E> listIterator() {
		return this.list.listIterator();
	}

	/* (非 Javadoc)
	 * @see java.util.List#listIterator(int)
	 */
	public ListIterator<E> listIterator(int index) {
		return this.list.listIterator(index);
	}

	/* (非 Javadoc)
	 * @see java.util.List#subList(int, int)
	 */
	public List<E> subList(int fromIndex, int toIndex) {
		return this.list.subList(fromIndex, toIndex);
	}
	
	/**
	 * 引数の要素中のリスト中の出現位置を二分サーチで求める。
	 * 同じ順列の要素が複数存在する場合、どの要素の位置を返すかは保証しない。
	 * 要素がリストに存在しない場合は、(-(挿入ポイント) - 1)を返す。
	 * 
	 * @param e
	 * @return
	 * @throws ClassCastException
	 * 				デフォルトコンストラクタを使用し、
	 * 				oがComparableインターフェースを実装していない場合にスローされる。
	 */
	protected int binarySearch(E e) {
		
		if (this.comparator != null) {
			return Collections.binarySearch(this.list, e, this.comparator);
		} else {
			
			@SuppressWarnings("unchecked")
			List<? extends Comparable<? super E>> l = 
					(List<? extends Comparable<? super E>>) this.list;
			
			return Collections.binarySearch(l, e);
		}
	}
	
	/**
	 * 
	 * @param e1
	 * @param e2
	 * @return
	 * @throws ClassCastException
	 * 				デフォルトコンストラクタを使用し、
	 * 				oがComparableインターフェースを実装していない場合にスローされる。
	 */
	@SuppressWarnings("unchecked")
	protected int compare(E e1, E e2) {
		
		if (this.comparator != null) {
			return this.comparator.compare(e1, e2);
		} else {
			return ((Comparable<E>) e1).compareTo(e2);
		}
	}

}
