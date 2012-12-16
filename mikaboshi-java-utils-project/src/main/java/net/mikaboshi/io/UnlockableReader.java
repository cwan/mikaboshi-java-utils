package net.mikaboshi.io;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.CharBuffer;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

/**
 * ファイルをアンロックできるReader
 * 
 * @author Takuma Umezawa
 * @since 1.1.5
 */
public class UnlockableReader extends Reader {

	private Reader reader = null;
	private File file = null;
	private String charset = null;
	
	private long readChars = 0L;
	
	public UnlockableReader(File file, String charset) throws IOException {
		this.file = file;
		this.charset = charset;
		createReader();
	}
	
	@Override
	public void close() throws IOException {
		if (this.reader != null) {
			this.reader.close();
		}
	}
	
	@Override
	public void mark(int readAheadLimit) throws IOException {
		createReader();
		this.reader.mark(readAheadLimit);
	}
	
	@Override
	public boolean markSupported() {
		try {
			createReader();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		return this.reader.markSupported();
	}
	
	@Override
	public int read() throws IOException {
		createReader();
		return this.reader.read();
	}
	
	@Override
	public int read(char[] cbuf) throws IOException {
		createReader();
		return this.reader.read(cbuf);
	}

	@Override
	public int read(char[] cbuf, int off, int len) throws IOException {
		createReader();
		
		int result = this.reader.read(cbuf, off, len);
		
		if (result > 0) {
			this.readChars += result;
		}
		
		return result;
	}
	
	@Override
	public int read(CharBuffer target) throws IOException {
		createReader();
		return this.reader.read(target);
	}
	
	@Override
	public boolean ready() throws IOException {
		createReader();
		return this.reader.ready();
	}
	
	@Override
	public void reset() throws IOException {
		createReader();
		this.reader.reset();
	}
	
	@Override
	public long skip(long n) throws IOException {
		createReader();
		return this.reader.skip(n);
	}
	
	public void unlock() {
		IOUtils.closeQuietly(this.reader);
		this.reader = null;
	}
	
	private synchronized void createReader() throws IOException {
		if (this.reader != null) {
			return;
		}
		
		this.reader = new BufferedReader(
				new InputStreamReader(
						FileUtils.openInputStream(this.file),
						this.charset
				));
		
		if (this.readChars != 0) {
			this.reader.skip(this.readChars);
		}
	}
	
}