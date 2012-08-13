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
 * �v�f�̒ǉ����ɑ}���ʒu�����߁A������ۏ؂��郊�X�g�B
 * </p>
 * <p>
 * �}���ʒu�́A�R���X�g���N�^�����œn���ꂽ{@link Comparator}�I�u�W�F�N�g���g�p����B
 *�@Comparator���g�p���Ȃ��ꍇ�́A{@link #newInstance(Class)}�ŃC���X�^���X�𐶐�����B
 * ���̏ꍇ�A�����̔�r��{@link Comparable#compareTo(Object)}���g�p�����B
 * </p>
 * <p>
 * �������ۏ؂ł��Ȃ��Ȃ邽�߁A{@link #add(int, Collection)}, 
 * {@link #addAll(int, Collection)}, {@link #set(int, Object)}��
 * ���̃N���X�Ŏg�p���邱�Ƃ͂ł��Ȃ��B
 * </p>
 * <p>
 * �P�ɁA�\�[�g���ꂽ���X�g���~�����ꍇ�ɂ�{@link  ArrayList}���g�p���āA�Ō��
 * {@link Collections#sort(List)}�������s�����ق��������B
 * �������A{@link #contains(Object)}��{@link #indexOf(Object)},
 * {@link #lastIndexOf(Object)}��p�ɂɎ��s����ꍇ�́A���̃N���X���g�p�������������Ȃ邱�Ƃ�����B
 * </p>
 * <p>
 * List�̎����́A{@link  ArrayList}���g�p����B�]���āA���̃N���X�͓���������Ȃ��B
 * </p>
 * 
 * @author Takuma Umezawa
 * @since 1.1.6
 *
 * @param <E> ���X�g�̗v�f
 */
public class SortedList<E> implements List<E> {
	
	private List<E> list = new ArrayList<E>();
	
	private Comparator<E> comparator = null;
	
	/**
	 * �f�t�H���g�R���X�g���N�^�B
	 * �ǉ��̍ۂ̗v�f�̔�r�ɂ́A�v�f���g��{@link Comparable#compareTo}���\�b�h���g�p����B
	 * ���̂��߁A���̏ꍇ�͗v�f��Comparable�C���^�[�t�F�[�X���������Ă���K�v������B
	 * �v�f�̌^���Ăяo��������w��ł��Ȃ����߁A�f�t�H���g�R���X�g���N�^��private�ɂ��āA
	 * {@link #newInstance(Class)}���\�b�h�ŃC���X�^���X�𐶐����邱�Ƃɂ���B
	 */
	private SortedList() {
	}
	
	/**
	 * Comparator���w�肷��R���X�g���N�^�B
	 * �ǉ��̍ۂ̗v�f�̔�r�ɂ́A������Comrapator���g�p����B
	 * 
	 * @param comparator
	 * @throws NullPointerException comparator��null�̏ꍇ�ɃX���[�����B
	 */
	public SortedList(Comparator<E> comparator) {
		SimpleValidator.validateNotNull(comparator, "comparator");
		this.comparator = comparator;
	}
	
	/**
	 * Comparator���w�肵�Ȃ��ŁASortedList�C���X�^���X�𐶐�����B
	 * �ǉ��̍ۂ̗v�f�̔�r�ɂ́A�����̃N���X��{@link Comparable#compareTo(Object)}���g�p����B
	 * 
	 * @param <T> �v�f�̌^
	 * @param clazz
	 * @return
	 */
	public static <T extends Comparable<T>> SortedList<T> newInstance(Class<T> clazz) {
		return new SortedList<T>();
	}

	/* (�� Javadoc)
	 * @see java.util.List#size()
	 */
	public int size() {
		return this.list.size();
	}

	/* (�� Javadoc)
	 * @see java.util.List#isEmpty()
	 */
	public boolean isEmpty() {
		return this.list.isEmpty();
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @throws ClassCastException
	 * 				�f�t�H���g�R���X�g���N�^���g�p���A
	 * 				o��Comparable�C���^�[�t�F�[�X���������Ă��Ȃ��ꍇ�ɃX���[�����B
	 */
	@SuppressWarnings("unchecked")
	public boolean contains(Object o) {
		return binarySearch((E) o) >= 0;
	}

	/* (�� Javadoc)
	 * @see java.util.List#iterator()
	 */
	public Iterator<E> iterator() {
		return this.list.iterator();
	}

	/* (�� Javadoc)
	 * @see java.util.List#toArray()
	 */
	public Object[] toArray() {
		return this.list.toArray();
	}

	/* (�� Javadoc)
	 * @see java.util.List#toArray(T[])
	 */
	public <T> T[] toArray(T[] a) {
		return this.list.toArray(a);
	}

	/**
	 * {@inheritDoc}
	 * 
	 * <p>
	 * ���X�g�̏�����ۏ؂��ėv�f��ǉ�����B
	 * </p>
	 * <p>
	 * ��������̗v�f�����łɑ��݂���ꍇ�́A�ǉ��v�f�̈ʒu�͕ۏ؂��Ȃ��B
	 * </p>
	 * <p>
	 * �R���X�g���N�^��{@link Comparator}���w�肳��Ă��Ȃ��ꍇ�A����e��null�̏ꍇ��
	 * {@link NullPointerException}���X���[�����B
	 * �R���X�g���N�^��Comparator���w�肳��Ă���ꍇ�Anull�̈�����
	 * Comparator�̎����Ɉˑ�����B
	 * </p>
	 * 
	 * @throws ClassCastException
	 * 				�f�t�H���g�R���X�g���N�^���g�p���A
	 * 				o��Comparable�C���^�[�t�F�[�X���������Ă��Ȃ��ꍇ�ɃX���[�����B
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

	/* (�� Javadoc)
	 * @see java.util.List#remove(java.lang.Object)
	 */
	public boolean remove(Object o) {
		return this.list.remove(o);
	}

	/* (�� Javadoc)
	 * @see java.util.List#containsAll(java.util.Collection)
	 */
	public boolean containsAll(Collection<?> c) {
		return this.list.containsAll(c);
	}

	/**
	 * {@inheritDoc}
	 * 
	 * <p>
	 * ���̃��\�b�h�Œǉ������ꍇ���A�����͕ۏႳ���B
	 * </p>
	 * 
	 * @throws ClassCastException
	 * 				�f�t�H���g�R���X�g���N�^���g�p���A
	 * 				o��Comparable�C���^�[�t�F�[�X���������Ă��Ȃ��ꍇ�ɃX���[�����B
	 */
	public boolean addAll(Collection<? extends E> c) {
		
		boolean modified = false;
		
		// ������ۏႷ�邽�߁A	���̃N���X��add���g��
		for (E e : c) {
			add(e);
			modified = true;
		}
		
		return modified;
	}

	/**
	 * �������ۏ؂ł��Ȃ����߁A���̃N���X�ł͎g�p�ł��Ȃ��B
	 * @throws UnsupportedOperationException ��ɃX���[�����B
	 */
	public boolean addAll(int index, Collection<? extends E> c) {
		throw new UnsupportedOperationException();
	}

	/* (�� Javadoc)
	 * @see java.util.List#removeAll(java.util.Collection)
	 */
	public boolean removeAll(Collection<?> c) {
		return this.list.removeAll(c);
	}

	/* (�� Javadoc)
	 * @see java.util.List#retainAll(java.util.Collection)
	 */
	public boolean retainAll(Collection<?> c) {
		return this.list.retainAll(c);
	}

	/* (�� Javadoc)
	 * @see java.util.List#clear()
	 */
	public void clear() {
		this.list.clear();
	}

	/* (�� Javadoc)
	 * @see java.util.List#get(int)
	 */
	public E get(int index) {
		return this.list.get(index);
	}

	/**
	 * �������ۏ؂ł��Ȃ����߁A���̃N���X�ł͎g�p�ł��Ȃ��B
	 * @throws UnsupportedOperationException ��ɃX���[�����B
	 */
	public E set(int index, E element) {
		throw new UnsupportedOperationException();
	}

	/**
	 * �������ۏ؂ł��Ȃ����߁A���̃N���X�ł͎g�p�ł��Ȃ��B
	 * @throws UnsupportedOperationException ��ɃX���[�����B
	 */
	public void add(int index, E element) {
		throw new UnsupportedOperationException();
	}

	/* (�� Javadoc)
	 * @see java.util.List#remove(int)
	 */
	public E remove(int index) {
		return this.list.remove(index);
	}

	/**
	 * {@inheritDoc}
	 * 
	 * <p>
	 * ���X�g���\�[�g����Ă���Ƃ����O��ŁA���X�g���̍ŏ��̏o���ʒu���擾����B
	 * �R���X�g���N�^��{@link Comparator}���w�肳��Ă��Ȃ��ꍇ�́A������g�p����B
	 * </p>
	 * <p>
	 * �R���X�g���N�^��Comparator���w�肳��Ă��Ȃ��ꍇ�A����e��null�̏ꍇ��
	 * {@link NullPointerException}���X���[�����B
	 * �R���X�g���N�^��Comparator���w�肳��Ă���ꍇ�Anull�̈�����
	 * Comparator�̎����Ɉˑ�����B
	 * </p>
	 * 
	 * @throws ClassCastException
	 * 				�f�t�H���g�R���X�g���N�^���g�p���A
	 * 				o��Comparable�C���^�[�t�F�[�X���������Ă��Ȃ��ꍇ�ɃX���[�����B
	 */
	public int indexOf(Object o) {
		
		@SuppressWarnings("unchecked")
		E e1 = (E) o;
		
		int pos = binarySearch(e1);
		
		if (pos < 0) {
			return -1;
		}
		
		// �ŏ��̏o���ʒu�𒲂ׂ邽�߁A���X�g���P���O�����Ɍ�������
		while (pos >= 0) {
			
			E e2 = this.list.get(pos);
			
			if ( compare(e1, e2) != 0 ) {
				return pos + 1;
			}
			
			pos--;
		}
		
		// ���X�g�̐擪�܂œ����������ꍇ
		return 0;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * <p>
	 * ���X�g���\�[�g����Ă���Ƃ����O��ŁA���X�g���̍Ō�̏o���ʒu���擾����B
	 * �R���X�g���N�^��{@link Comparator}���w�肳��Ă��Ȃ��ꍇ�́A������g�p����B
	 * </p>
	 * <p>
	 * �R���X�g���N�^��Comparator���w�肳��Ă��Ȃ��ꍇ�A����e��null�̏ꍇ��
	 * {@link NullPointerException}���X���[�����B
	 * �R���X�g���N�^��Comparator���w�肳��Ă���ꍇ�Anull�̈�����
	 * Comparator�̎����Ɉˑ�����B
	 * </p>
	 * 
	 * @throws ClassCastException
	 * 				�f�t�H���g�R���X�g���N�^���g�p���A
	 * 				o��Comparable�C���^�[�t�F�[�X���������Ă��Ȃ��ꍇ�ɃX���[�����B
	 */
	public int lastIndexOf(Object o) {

		@SuppressWarnings("unchecked")
		E e1 = (E) o;
		
		int pos = binarySearch(e1);
		
		if (pos < 0) {
			return -1;
		}
		
		// �Ō�̏o���ʒu�𒲂ׂ邽�߁A���X�g���P��������Ɍ�������
		
		int lastIndex = this.list.size() - 1;
		
		while (pos <= lastIndex) {
			
			E e2 = this.list.get(pos);
			
			if ( compare(e1, e2) != 0 ) {
				return pos - 1;
			}
			
			pos++;
		}
		
		// ���X�g�̍Ō�܂œ����������ꍇ
		return lastIndex;
	}

	/* (�� Javadoc)
	 * @see java.util.List#listIterator()
	 */
	public ListIterator<E> listIterator() {
		return this.list.listIterator();
	}

	/* (�� Javadoc)
	 * @see java.util.List#listIterator(int)
	 */
	public ListIterator<E> listIterator(int index) {
		return this.list.listIterator(index);
	}

	/* (�� Javadoc)
	 * @see java.util.List#subList(int, int)
	 */
	public List<E> subList(int fromIndex, int toIndex) {
		return this.list.subList(fromIndex, toIndex);
	}
	
	/**
	 * �����̗v�f���̃��X�g���̏o���ʒu��񕪃T�[�`�ŋ��߂�B
	 * ��������̗v�f���������݂���ꍇ�A�ǂ̗v�f�̈ʒu��Ԃ����͕ۏ؂��Ȃ��B
	 * �v�f�����X�g�ɑ��݂��Ȃ��ꍇ�́A(-(�}���|�C���g) - 1)��Ԃ��B
	 * 
	 * @param e
	 * @return
	 * @throws ClassCastException
	 * 				�f�t�H���g�R���X�g���N�^���g�p���A
	 * 				o��Comparable�C���^�[�t�F�[�X���������Ă��Ȃ��ꍇ�ɃX���[�����B
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
	 * 				�f�t�H���g�R���X�g���N�^���g�p���A
	 * 				o��Comparable�C���^�[�t�F�[�X���������Ă��Ȃ��ꍇ�ɃX���[�����B
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
