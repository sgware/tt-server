package edu.uky.cs.nil.tt;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * A log is used to record system messages and session data.
 * <p>
 * A server log has two basic functions:
 * <ul>
 * <li>Write system message to standard output and (optionally) a file.</li>
 * <li>Record completed storytelling {@link Session sessions}, each in its own
 * file in a directory.</li>
 * </ul>
 * 
 * @author Stephen G. Ware
 */
public class Log implements AutoCloseable {
	
	/**
	 * Writes a system message to {@link System#out standard output} and to the
	 * given server log, if it exists.
	 * 
	 * @param message the message to write
	 * @param log the server log to which the message will be written, or null
	 * if the message should only be written to standard output
	 */
	public static void append(String message, Log log) {
		append(message, log, System.out);
	}
	
	/**
	 * Writes a system message and the details of a {@link Throwable throwable}
	 * to {@link System#err standard error output} and to the given server log,
	 * if it exists.
	 * 
	 * @param message the message explaining the context of the throwable
	 * @param throwable the throwable whose details and stack trace will be
	 * written
	 * @param log the server log to which the message and details will be
	 * written, or null if they should only be written to standard error output
	 */
	public static void append(String message, Throwable throwable, Log log) {
		StringWriter string = new StringWriter();
		string.append(message);
		if(throwable != null) {
			string.append("\n\t");
			throwable.printStackTrace(new PrintWriter(string));
		}
		append(string.toString().trim(), log, System.err);
	}
	
	/** The data format prepended to all log messages */
	private static DateFormat date = null;
	
	/**
	 * Writes a system message to a print stream (typically {@link System#out
	 * standard output} or {@link System#err standard error output}) and to a
	 * server log, if it exists.
	 * 
	 * @param message the message to write
	 * @param log the server log to write to, or null if the message should only
	 * be written to the print stream
	 * @param system the print stream to write to
	 */
	private static final void append(String message, Log log, PrintStream system) {
		if(date == null) {
			date = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
			date.setTimeZone(TimeZone.getTimeZone("UTC"));
		}
		message = "[" + date.format(new Date()) + "] " + message + "\n";
		system.append(message);
		if(log != null && log.writer != null) {
			try {
				log.writer.append(message);
				log.writer.flush();
			}
			catch(IOException exception) {
				exception.printStackTrace();
			}
		}
	}
	
	/** The file writer for the system message log */
	private final FileWriter file;
	
	/** The (possibly buffered) writer for system messages */
	private final Writer writer;
	
	/** The directory to which completed session files will be written */
	private final File sessions;
	
	/**
	 * Constructs a new server log.
	 * 
	 * @param log the file to which system message will be written, or null if
	 * system message should only be written to standard output
	 * @param sessions the directory to which completed session files should be
	 * written, or null if sessions should not be recorded
	 * @throws IOException if a problem occurs when creating or opening the log
	 * file or session directory
	 */
	public Log(File log, File sessions) throws IOException {
		if(log == null) {
			append("Server log will not be written to file.");
			this.file = null;
			this.writer = null;
		}
		else {
			this.file = new FileWriter(log, true);
			this.writer = new BufferedWriter(file);
			append("Server log will be written to \"" + log.getPath() + "\".");
		}
		if(sessions == null) {
			append("Sessions will not be written to file.");
			this.sessions = null;
		}
		else if(sessions.exists() && !sessions.isDirectory())
			throw new IOException("The path \"" + sessions.getPath() + "\" is not a directory.");
		else {
			append("Sessions will be written to \"" + sessions.getPath() + "\".");
			this.sessions = sessions;
			if(!sessions.exists())
				sessions.mkdir();
		}
	}
	
	/**
	 * Writes a system message to {@link System#out standard output} and to the
	 * log file (if a log file is being used).
	 * 
	 * @param message the message to write
	 */
	public void append(String message) {
		append(message, this);
	}
	
	/**
	 * Writes a system message and the details of a {@link Throwable throwable}
	 * to {@link System#err standard error output} and to the log file (if a log
	 * file is being used)
	 * 
	 * @param message the message that gives context for the throwable
	 * @param throwable the throwable whose details and stack trace will be
	 * written
	 */
	public void append(String message, Throwable throwable) {
		append(message, throwable, this);
	}
	
	/**
	 * Generates a {@link Utilities#getRandomName() random name} and writes the
	 * details of a completed storytelling {@link Session session} to a file
	 * with that name in the session file directory (if session are being
	 * recorded). If sessions are not being recorded, this method does nothing
	 * and returns null.
	 * 
	 * @param session a completed session
	 * @return the randomly generated session name
	 * @throws IOException if a problem occurs while writing the session to file
	 */
	public String append(Session session) throws IOException {
		if(sessions == null)
			return null;
		String name = Utilities.getRandomName();
		String url = sessions.getPath() + "/" + name + ".json";
		File file = new File(url);
		GsonBuilder builder = new GsonBuilder();
		Session.configure(builder);
		Gson gson = builder.setPrettyPrinting().create();
		try(BufferedWriter out = new BufferedWriter(new FileWriter(file))) {
			gson.toJson(session, out);
		}
		append("Session \"" + name + "\" written to \"" + url + "\".");
		return name;
	}
	
	@Override
	public void close() throws IOException {
		if(file != null)
			file.close();
	}
}