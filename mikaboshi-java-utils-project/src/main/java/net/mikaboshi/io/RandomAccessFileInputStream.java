package net.mikaboshi.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * {@link RandomAccessFile} からデータを取得する {@link InputStream}。
 *
 * @since 1.1.8
 */
public class RandomAccessFileInputStream extends InputStream {

	private static final Log logger = LogFactory.getLog(RandomAccessFileInputStream.class);

	private final RandomAccessFile file;

	private long markedPos = 0L;

	public RandomAccessFileInputStream(RandomAccessFile f) {
		this.file = f;
	}


	@Override
	public synchronized int read() throws IOException {
		return this.file.read();
	}

	@Override
	public synchronized int read(byte[] b) throws IOException {
		return this.file.read(b);
	}


	@Override
	public synchronized int read(byte[] b, int off, int len) throws IOException {
		return this.file.read(b, off, len);
	}


	@Override
	public synchronized long skip(long n) throws IOException {

		if (n <= 0L) {
			return 0L;
		}

		long pos = this.file.getFilePointer();
		long len = this.file.length();

		long newpos = pos + n;

		if (newpos > len) {
			newpos = len;
		}

		this.file.seek(newpos);

		return newpos - pos;
	}


	@Override
	public synchronized int available() throws IOException {

		long d = this.file.length() - this.file.getFilePointer();

		if (d > (long) Integer.MAX_VALUE) {
			return 0;
		}

		return (int) (d);
	}


	@Override
	public synchronized void close() throws IOException {
		this.file.close();
	}


	@Override
	public synchronized void mark(int readlimit) {

		try {
			this.markedPos = this.file.getFilePointer();
		} catch (IOException e) {

			logger.warn("Unable to mark for a RamdomAccessFile.", e);

			this.markedPos = 0L;
		}
	}


	@Override
	public synchronized void reset() throws IOException {

		this.file.seek(this.markedPos);
	}


	@Override
	public boolean markSupported() {
		return true;
	}

}
