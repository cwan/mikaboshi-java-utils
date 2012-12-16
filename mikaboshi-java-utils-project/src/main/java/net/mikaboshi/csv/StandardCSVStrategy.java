package net.mikaboshi.csv;

import static net.mikaboshi.validator.SimpleValidator.validateNotContainsInvalidCharactor;
import static net.mikaboshi.validator.SimpleValidator.validateNotNull;
import static net.mikaboshi.validator.SimpleValidator.validateNotNullNorLength0;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import net.mikaboshi.util.MkStringUtils;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * <p>
 * 一般的な方法で、CSVのエンコード、デコードを行う。
 * </p><p>
 * エンコード、デコード方式の詳細：
 * <ul>
 *   <li>項目のデフォルト区切り文字（指定可能）： 「,」 （カンマ）</li>
 *   <li>項目の引用符： 「"」（二重引用符）</li>
 *   <li>エンコード時、項目中に二重引用符があった場合は、 二重引用符２つに置換する。</li>
 *   <li>エンコード時、項目がnullの場合、{@link AbstractCSVStrategy#getNullString()}を出力する。 </li>
 *   <li>項目中に、二重引用符またはカンマがある場合、先頭と末尾に二重引用符を付けて出力する。
 *   	（alwaysQuote=falseならば、それ以外の場合は先頭と末尾の二重引用符はつけない） </li>
 *   <li>デコードは、上記エンコードと逆の処理を行う。</li>
 *   <li>デコード時、不正な形式だった場合（二重引用符が閉じていないなど）、
 *       エラーとはせずにそのまま次のカンマまたは行末までの文字列を返す。</li>
 * </ul>
 * </p>
 * 
 * @author Takuma Umezawa
 */
public class StandardCSVStrategy extends AbstractCSVStrategy {

	private static final long serialVersionUID = 1640073211487732341L;

	private static Log logger = LogFactory.getLog(StandardCSVStrategy.class);

	/** 項目の引用符 */
	private static final char QUOTE = '"';
	
	/** Stringでの引用符 */
	private static final String QUOTE_AS_STRING =
			StringUtils.EMPTY + QUOTE;
	
	/** 引用符のエスケープ形式 */
	private static final String ESCAPED_QUOTE = 
			StringUtils.EMPTY + QUOTE + QUOTE;
	
	/** 空項目の引用形 */
	private static final String QUOTED_EMPTY =
			StringUtils.EMPTY + QUOTE + QUOTE;
	
	private boolean alwaysQuote = false;

	public StandardCSVStrategy() {}	
	
	/**
	 * <p>
	 * 未加工の項目を、CSV出力形式に変換する。
	 * </p><p>
	 * 引数の rawItem は、Object#toString() の戻り値で評価する。
	 * <ul>
	 *   <li>rawItem中に二重引用符があるならば、二重引用符２つに変換する。</li>
	 *   <li>rawItemがnullまたは空文字ならば、二重引用符２つを出力する。</li>
	 *   <li>rawItem中にカンマまたは二重引用符がある場合、rawItemの先頭と 末尾に二重引用符を付けて出力する。
	 *   （alwaysQuote=falseならば、それ以外の場合は先頭と末尾の二重引用符はつけない）</li>
	 * </ul>
	 * </p>  
	 * @param rawItem 未加工のCSV項目
	 * @return rawItemを出力形式に変換した値。
	 */
	public String escape(Object rawItem) {
		if (rawItem == null) {
			if (this.alwaysQuote) {
				return this.quotedNullString;
			} else {
				return getNullString();
			}
		}
		
		if (rawItem.toString().length() == 0) {
			if (this.alwaysQuote) {
				return QUOTED_EMPTY;
			} else {
				return StringUtils.EMPTY;
			}
		}

		String strItem = rawItem.toString();

		// 引用符が必要かどうか
		boolean isRequiredQuotation =
			this.alwaysQuote ||
			containsSpecialChars(strItem) ||
			StringUtils.contains(strItem, getDelimiter());

		// 引用符のエスケープ
		String escapedItem = StringUtils.replace(
					strItem, QUOTE_AS_STRING, ESCAPED_QUOTE);

		if (isRequiredQuotation) {
			return QUOTE + escapedItem + QUOTE;
		} else {
			return escapedItem;
		}
	}
	
	/** エスケープが必要な文字 */
	private static final char[] SPECIAL_CHARS =	new char[] {QUOTE, '\r', '\n'};
	
	/**
	 * 引数strがエスケープ必要な文字を含んでいればtrueを返す
	 * @param str
	 * @return
	 */
	private boolean containsSpecialChars(String str) {
		return !StringUtils.containsNone(str, SPECIAL_CHARS);
	}

	/**
	 * <p>
	 * 出力形式に加工されたCSV項目を、本来の文字列に変換して返す。
	 * </p><p>
	 * <ul>
	 *   <li>両端が引用符で囲まれていたら取り除く。</li>
	 *   <li>引用符が２つ連続していたら１つに置換する。</li>
	 * </ul>
	 * </p><p>
	 * 引数が不正なケース（引用符が閉じていないなど）は、
	 * このメソッドではチェックしない。
	 * </p>
	 * 
	 * @param item 引用符が付けられ、エスケープされているかもしれないCSV項目
	 * @return 変換後の文字列
	 */
	public String unescape(String item) {

		if (item.length() <= 1) {
			return item;
		}

		if (item.charAt(0) != QUOTE ||
			item.charAt(item.length() - 1) != QUOTE) {

			// 両端に引用符が付かないならば、そのまま
			return item;
		}

		// 両端の引用符を取り外す
		String unquoted = item.substring(1, item.length() - 1);

		// 連続する引用符を１つにする
		return StringUtils.replace(
				unquoted, ESCAPED_QUOTE, QUOTE_AS_STRING);
	}

	/* (non-Javadoc)
	 * @see net.mikaboshi.csv.CSVStrategy#csvLines(java.io.Reader)
	 */
	public Iterable<String[]> csvLines(final Reader reader) {
		validateNotNull(reader, "reader", NullPointerException.class);
		
		return new Iterable<String[]>() {
			public Iterator<String[]> iterator() {
				return new CSVIterator(reader);
			}
		};
	}
	
	/**
	 * 常に引用符を付ける場合はtrueを設定する。（デフォルトはfalse）
	 * @param alwaysQuote
	 * @since 1.1.5
	 */
	public void setAlwaysQuote(boolean alwaysQuote) {
		this.alwaysQuote = alwaysQuote;
	}
	
	private String quotedNullString = QUOTE + getNullString() + QUOTE;
	
	@Override
	public void setNullString(String nullString) {
		super.setNullString(nullString);
		this.quotedNullString = QUOTE + getNullString() + QUOTE;
	}
	
	/**
	 * <p>
	 * CSVデータを読み込み、1行ごとに読み込むIterator。
	 * 1行内の項目は、Stringの配列で保持する。
	 * </p><p>
	 * ここでいう「行」は、物理行ではなく論理行である。
	 * つまり、引用符で囲まれた中に改行コードがあった場合は、
	 * １つの行と見なす。
	 * </p><p>
	 * <i>(注意)このクラスのインスタンスは非同期である。</i> 
	 * </p>
	 */
	public class CSVIterator implements Iterator<String[]> {

		private final Reader reader;
		
		/** 現在見ている文字 */
		private char currentChar;
		
		/** 次の文字 */
		private char nextChar;
		
		/** 次の文字がない場合はtrue */
		private boolean noNextChar = true;
		
		/** マーク時の次の文字 */
		private char nextCharOnMark;
		
		/** マーク時に次の文字がない場合はtrue */
		private boolean noNextCharOnMark = true;
		
		private static final int END_OF_STREAM = -1;
		
		/**
		 * <p>
		 * 入力データ（CSV）であるreaderと、文字列をCSV項目に切り出す
		 * 処理を行うCSVStrategyを設定する。
		 * </p><p>
		 * （注意） ここでセットしたReaderオブジェクトに対し、直接 {@link Reader#mark(int)}や
		 * {@link Reader#reset()}を呼び出してはならない。
		 * このクラスの{@link #mark(int)}や{@link #reset()}を使用すること。
		 * </p>
		 * 
		 * @param reader
		 * @param csvStrategy
		 */
		public CSVIterator(Reader reader) {
			if (reader instanceof BufferedReader) {
				this.reader = reader;
			} else {
				this.reader = new BufferedReader(reader);
			}
		}
		
		/**
		 * 次の論理行があるかどうか判定する。
		 * 
		 * @return 
		 *      次の論理行がある場合：true
		 *      次の論理行がない場合：false
		 */
		public boolean hasNext() {
			if (this.noNextChar) {
				shiftChar();
			}
			
			return !this.noNextChar;
		}
		
		/**
		 * 次のCSV行データを返す。
		 * 
		 * @return 次のCSV行データ
		 * @throws NoSuchElementException 繰り返し処理でそれ以上要素がない場合
		 */
		public String[] next() throws NoSuchElementException {
			
			if (!hasNext()) {
				throw new NoSuchElementException("End of CSV line.");
			}
			
			List<String> items = new ArrayList<String>();
			
			// 引用の中かどうか
			boolean isInQuote = false;
			
			StringBuilder sb = new StringBuilder();
			DelimiterBuffer delimiterBuffer 
					= new DelimiterBuffer(getDelimiter());
			
			while (true) {
				shiftChar();
				
				if (this.noNextChar) {
					// ストリームの終わりまで来た
					sb.append(this.currentChar);
					
					break;
				}
				
				delimiterBuffer.nextChar(this.currentChar);
				
				if (this.currentChar == '\r' && !isInQuote) {
					if (this.nextChar == '\n' && !isInQuote) {
						// \r\nの場合は2文字進める
						shiftChar();
					}
					// 引用の外で改行が来たら論理行の終わり
					break;
				}
				
				if (this.currentChar == '\n' && !isInQuote) {
					// 引用の外で改行が来たら論理行の終わり
					break;
				}
				
				if (delimiterBuffer.isDelimiter()) {
					if (isInQuote) {
						// 引用の中で区切り文字が来た場合
						sb.append(this.currentChar);
					} else {
						// 引用の外で区切り文字が来た
						
						// 末尾の（区切り文字長 - 1）を削る
						String str = MkStringUtils.cutTail(sb.toString(), getDelimiter().length() - 1);
						
						items.add(unescape(str));
						sb = new StringBuilder();
						delimiterBuffer.clear();
						
						if (this.nextChar == '\n' || this.nextChar == '\r') {
							// 次が改行ならば、空項目を追加
							items.add(StringUtils.EMPTY);
						}
					}
					
					continue;
				}
				
				if (this.currentChar == QUOTE) {
					if (isInQuote) {
						if (this.nextChar == QUOTE) {
							// 引用の中で、二文字続けて引用符が来た場合
							sb.append(QUOTE);
							sb.append(QUOTE);
							shiftChar();
							continue;
						}
						
						// 引用中に引用符が来て、次が引用符では場合はない場合、
						// ここで項目が区切れると見なす。
						// 連続しない引用符⇒区切り文字という不正なインプットが
						// ありうるが、それは無視する。（これによって、可逆性は失われる）
					}
					
					sb.append(this.currentChar);
					isInQuote = !isInQuote;
					continue;
				}
				
				// 引用の外で一般の文字が来た場合
				sb.append(this.currentChar);
			}
			
			if (sb.length() != 0) {
				items.add(unescape(sb.toString()));
			}
			
			if (!items.isEmpty()) {
				String lastItem = items.get(items.size() - 1);
				
				if (lastItem.endsWith(getDelimiter())) {
					// 末尾が区切り文字で終わっている場合、
					// 区切り文字を取って、最後に空項目を追加する。
					// 場当たり的だが、逐次チェックするようりパフォーマンスはよいはず
					
					String str = MkStringUtils.cutTail(lastItem, getDelimiter().length());
					items.set(items.size() - 1, str);
					items.add(StringUtils.EMPTY);
				}
			}
			
			return items.toArray(new String[items.size()]);
		}

		/**
		 * CSVIteratorでは使用しない。
		 * 
		 * @throws UnsupportedOperationException
		 *              このメソッドが使用された場合、常にスローされる
		 */
		public void remove() throws UnsupportedOperationException {
			throw new UnsupportedOperationException(
					"Could not use CSVIterator.remove().");
		}
		
		/**
		 * Readerにマークをセットする。
		 * @param readAheadLimit
		 * @throws IOException Readerがマークをサポートしない場合
		 * @since 1.1.5
		 */
		public void mark(int readAheadLimit) throws IOException {
			this.reader.mark(readAheadLimit);
			
			// 先読み文字の退避
			this.noNextCharOnMark = this.noNextChar;
			
			if (!this.noNextCharOnMark) {
				this.nextCharOnMark = this.nextChar;
			}
		}
		
		/**
		 * Readerをマーク位置にリセットする。
		 * @throws IOException Readerがリセットをサポートしない場合。
		 * @since 1.1.5
		 */
		public void reset() throws IOException {
			this.reader.reset();
			
			// 退避していた先読み文字の復元
			this.noNextChar = this.noNextCharOnMark;
			
			if (!this.noNextChar) {
				this.nextChar = this.nextCharOnMark;
			}
		}
		
		/**
		 * readerから１文字バッファに読み込む。
		 */
		private void shiftChar() {
			int next;
			
			try {
				// ストリームの最後にきた場合は、-1が返ってくる
				next = this.reader.read();
				
			} catch (IOException e) {
				logger.warn("CSV read error.", e);
				next = END_OF_STREAM;
			}
			
			this.currentChar = this.nextChar;
			
			if (next == END_OF_STREAM) {
				this.noNextChar = true;
			} else {
				this.nextChar = (char) next;
				this.noNextChar = false;
			}
		}
		
	}
	
	static class DelimiterBuffer {
		private String delimiter;
		private char[] buffer;
		
		public DelimiterBuffer(String delimiter) {
			validateNotNullNorLength0(
					delimiter, "delimiter",
					IllegalArgumentException.class);
			validateNotContainsInvalidCharactor(
					delimiter, "delimiter", SPECIAL_CHARS,
					IllegalArgumentException.class);
			
			this.delimiter = delimiter;
			clear();
		}
		
		public void nextChar(char c) {
			// ひとつずつ前にずらし、末尾に新しい文字を設定する
			for (int i = 0; i < this.delimiter.length() - 1; i++) {
				this.buffer[i] = this.buffer[i + 1];
			}

			this.buffer[this.delimiter.length() - 1] = c;
		}
		
		public boolean isDelimiter() {
			return this.delimiter.equals(new String(this.buffer));
		}
		
		public void clear() {
			this.buffer = new char[this.delimiter.length()];
		}
	}
}