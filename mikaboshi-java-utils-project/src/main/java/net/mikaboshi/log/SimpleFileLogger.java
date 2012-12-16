package net.mikaboshi.log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * 簡易ファイル出力ロガー。
 * ログファイルを明示的に閉じなくても、JVMの終了時に自動的に閉じる。
 *
 * 出力先のディレクトリが存在しない場合は、自動的に作成する。
 *
 * このクラスはログ出力先について同期を保証する。
 *
 * @author Takuma Umezawa
 */
public class SimpleFileLogger {

	private final long ROTATE_CHECK_INTERVAL = 5000L;

	private PrintWriter writer;

	private final File logFile;

	private final boolean append;

	private final boolean autoFlush;

	private long rotetaSize = -1L;

	private long lastRotateCheckTime = 0L;

	private final int bufferSize;

	/**
	 * ログ出力の各プロパティを指定する。
	 * バッファサイズはデフォルトを適用する。
	 *
	 * @param path ログファイルのパス
	 * @param append 追加書き込みモードならばtrue
	 * @param autoFlush 自動フラッシュモードならばtrue
	 * @throws IOException ログファイルのオープンに失敗した場合、ログディレクトリの作成に失敗した場合
	 */
	public SimpleFileLogger(String path, boolean append, boolean autoFlush) throws IOException {
		this(path, append, autoFlush, 8192);
	}

	/**
	 * ログ出力の各プロパティを指定する。
	 *
	 * @since 1.1.4
	 * @param path ログファイルのパス
	 * @param append 追加書き込みモードならばtrue
	 * @param autoFlush 自動フラッシュモードならばtrue
	 * @param bufferSize バッファサイズ
	 * @throws IOException ログファイルのオープンに失敗した場合、ログディレクトリの作成に失敗した場合
	 */
	public SimpleFileLogger(String path, boolean append, boolean autoFlush, int bufferSize) throws IOException {

		this.logFile = new File(path);
		this.append = append;
		this.autoFlush = autoFlush;
		this.bufferSize = bufferSize;

		open();
	}

	private void open() throws IOException {

		try {
			File dir = new File(new File(this.logFile.getCanonicalPath()).getParent());
			dir.mkdirs();
		} catch (SecurityException e) {
			throw new IOException("Could not make a log directory.", e);
		}

		try {
			FileWriter fileWriter =
					new FileWriter(this.logFile, this.append);
			BufferedWriter bufferedWriter =
				new BufferedWriter(fileWriter, this.bufferSize);

			this.writer = new PrintWriter(bufferedWriter, this.autoFlush);

		} catch (FileNotFoundException e) {
			throw new IOException("Could not open the log file.", e);
		}

		// シャットダウン時にファイルを閉じる
		Runtime.getRuntime().addShutdownHook(new Thread() {
			public void run() {
				close();
			}
		});
	}

	/**
	 * ログのローテーションサイズをバイト数で指定する。
	 * 0以下の値を指定した場合、ローテーションは行わない。
	 * @param size バイト数
	 */
	public synchronized void setRotetaSize(long size) {
		this.rotetaSize = size;
	}

	/**
	 * <p>
	 * ログファイルを削除する。
	 * </p><p>
	 * 削除対象のファイルは、コンストラクタで指定された path のファイル、および path と同じディレクトリにある
	 * ローテーションファイル（※）である。
	 * </p><p>
	 * ※ ローテーションファイル : {@code path + "." + n  (n = 1, 2, 3 ...)}
	 * </p>
	 */
	public synchronized void clean() throws IOException {
		close();

		String logFileName = logFile.getName();
		this.logFile.delete();

		for (File file : this.logFile.getAbsoluteFile().getParentFile().listFiles()) {

			if (!file.exists() || !file.isFile()) {
				continue;
			}

			if (file.getName().startsWith(logFileName) &&
				file.getName().matches(".+\\.\\d+$")) {
				file.delete();
			}
		}

		open();
	}

	/**
	 * ログを1行出力する
	 * @param log ログ文字列
	 */
	public synchronized void put(String log) throws IOException {
		rotate();
		this.writer.println(log);
	}

	/**
	 * ログをフォーマットで指定し、パラメータを埋め込んだ文字列を一行出力する。
	 * フォーマットおよびパラメータの埋め込み形式は、{@link PrintWriter#printf(String, Object...)}
	 * に従う。
	 * @param format ログのフォーマット（末尾の改行は自動的に付与される）
	 * @param args フォーマットに埋め込むパラメータ
	 */
	public synchronized void putf(String format, Object ... args) throws IOException {
		rotate();

		if (format == null) {
			this.writer.println("null");
			return;
		}
		this.writer.printf(format + "%n", args);
	}

	/**
	 * 明示的にログファイルを閉じる。
	 */
	public synchronized void close() {
		if (this.writer == null) {
			return;
		}
		flush();
		this.writer.close();
		this.writer = null;
	}

	/**
	 * 明示的にフラッシュする。
	 */
	public synchronized void flush() {
		if (!this.autoFlush) {
			this.writer.flush();
		}
	}

	/**
	 * ローテーションが必要ならばローテーションを行う。
	 */
	private void rotate() throws IOException {

		if (this.rotetaSize <= 0) {
			return;
		}

		if (System.currentTimeMillis() < this.lastRotateCheckTime + ROTATE_CHECK_INTERVAL) {
			return;
		}


		if (!this.logFile.isFile() || this.logFile.length() < this.rotetaSize) {
			return;
		}

		close();

		move(this.logFile.getAbsolutePath(), this.logFile, 1);

		open();

		this.lastRotateCheckTime = System.currentTimeMillis();
	}

	/**
	 * ファイルを退避する。
	 * @param path
	 * @param file
	 * @param index
	 */
	private void move(String path, File file, int index) {
		File evacuated = new File(path + "." + index);

		if (evacuated.exists() && evacuated.isFile()) {
			move(path, evacuated, index + 1);
		}

		file.renameTo(evacuated);
	}
}
