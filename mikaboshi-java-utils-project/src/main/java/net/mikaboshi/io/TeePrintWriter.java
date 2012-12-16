package net.mikaboshi.io;

import java.io.PrintWriter;

import net.mikaboshi.validator.SimpleValidator;

/**
 * 別の出力先を指定するPrintWriter
 * 
 * @author Takuma Umezawa
 * @since 1.1.5
 */
public class TeePrintWriter extends PrintWriter {

	private PrintWriter branch;
	
	public TeePrintWriter(PrintWriter out, PrintWriter branch) {
		super(out);
		SimpleValidator.validateNotNull(branch, "branch");
		this.branch = branch;
	}

	@Override
	public void println() {
		super.println();
		this.branch.println();
	}

	@Override
	public void write(char[] buf, int off, int len) {
		super.write(buf, off, len);
		this.branch.write(buf, off, len);
	}

	@Override
	public void write(int c) {
		super.write(c);
		this.branch.write(c);
	}

	@Override
	public void write(String s, int off, int len) {
		super.write(s, off, len);
		this.branch.write(s, off, len);
	}
	
	@Override
	public void flush() {
		super.flush();
		this.branch.flush();
	}
	
	@Override
	public void close() {
		super.close();
		this.branch.close();
	}
}
