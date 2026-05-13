package edu.uky.cs.nil.tt;

import java.io.IOException;
import java.io.Reader;

/**
 * A {@link Reader} which throws an exception if it reads more than a certain
 * number of characters before encountering an end of line. This reader is used
 * to secure other IO utilities like {@link java.io.BufferedReader} which read
 * whole lines of input at a time. Malicious users could cause problems such as
 * {@link OutOfMemoryError} by sending long streams of information without a new
 * line character.
 * 
 * @author Stephen G. Ware
 */
public class LimitedLineLengthReader extends Reader {
	
	/** The reader from which characters will be read */
	private final Reader wrapped;
	
	/** The number of characters which may be read an end of line */
	private final int limit;
	
	/**
	 * The number of non-end-of-line characters read since the start or since
	 * the last end-of-line was read
	 */
	private int read = 0;
	
	/**
	 * Wraps a new limited line length reader around a given reader and with
	 * the given limit on line length.
	 * 
	 * @param reader the reader from which characters will be read
	 * @param limit the number of characters which may be read before an end
	 * of line character
	 */
	public LimitedLineLengthReader(Reader reader, int limit) {
		this.wrapped = reader;
		Utilities.requireGreaterThan(limit, 0, "line length limit");
		this.limit = limit;
	}
	
	/**
	 * {@inheritDoc}
	 * @throws IOException if the reader reads more than its limit of characters
	 * without encountering a new line character
	 */
	@Override
	public int read(char[] cbuf, int off, int len) throws IOException {
		// Return immediately if asked to read nothing.
		if(len == 0)
			return 0;
		// Read up to the remaining number of characters.
		int result = wrapped.read(cbuf, off, Math.max(len, limit - read));
		// Return -1 if the stream was closed.
		if(result == -1)
			return -1;
		// Check every character read for new line characters.
		for(int i = 0; i < result; i++) {
			read++;
			if(cbuf[off + i] == '\n')
				read = 0;
			else if(read == limit)
				throw new IOException("Read " + limit + " characters without encountering an end of line.");
		}
		return result;
	}

	@Override
	public void close() throws IOException {
		wrapped.close();
	}
}