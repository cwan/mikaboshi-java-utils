package net.mikaboshi.io;

import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.io.Reader;

import org.apache.commons.io.IOUtils;
import org.junit.Test;

public class RandomAccessFileInputStreamTest {

	@Test
	public void read_Windows31J() throws IOException {

		String charset = "Windows-31J";

		RandomAccessFile randomAccessFile = new RandomAccessFile(
				new File("src/test/resources/net/mikaboshi/io/RandomAccessFileInputStreamTest_" + charset + ".txt"), "r");


		InputStream in = null;
		Reader reader = null;


		try {
			in = new RandomAccessFileInputStream(randomAccessFile);

			reader = new BufferedReader(new InputStreamReader(in, charset));

			assertEquals('a', reader.read());
			assertEquals('b', reader.read());
			assertEquals('c', reader.read());
			assertEquals('d', reader.read());
			assertEquals('e', reader.read());
			assertEquals('\r', reader.read());
			assertEquals('\n', reader.read());

			assertEquals('\r', reader.read());
			assertEquals('\n', reader.read());

			assertEquals('あ', reader.read());
			assertEquals('い', reader.read());
			assertEquals('う', reader.read());
			assertEquals('え', reader.read());
			assertEquals('お', reader.read());
			assertEquals('\r', reader.read());
			assertEquals('\n', reader.read());

			assertEquals(-1, reader.read());

		} finally {

			IOUtils.closeQuietly(reader);
			IOUtils.closeQuietly(in);
		}

	}

	@Test
	public void read_UTF8() throws IOException {

		String charset = "UTF-8";

		RandomAccessFile randomAccessFile = new RandomAccessFile(
				new File("src/test/resources/net/mikaboshi/io/RandomAccessFileInputStreamTest_" + charset + ".txt"), "r");


		InputStream in = null;
		Reader reader = null;


		try {
			in = new RandomAccessFileInputStream(randomAccessFile);

			reader = new BufferedReader(new InputStreamReader(in, charset));

			assertEquals('a', reader.read());
			assertEquals('b', reader.read());
			assertEquals('c', reader.read());
			assertEquals('d', reader.read());
			assertEquals('e', reader.read());
			assertEquals('\r', reader.read());
			assertEquals('\n', reader.read());

			assertEquals('\r', reader.read());
			assertEquals('\n', reader.read());

			assertEquals('あ', reader.read());
			assertEquals('い', reader.read());
			assertEquals('う', reader.read());
			assertEquals('え', reader.read());
			assertEquals('お', reader.read());
			assertEquals('\r', reader.read());
			assertEquals('\n', reader.read());

			assertEquals(-1, reader.read());

		} finally {

			IOUtils.closeQuietly(reader);
			IOUtils.closeQuietly(in);
		}

	}

}
