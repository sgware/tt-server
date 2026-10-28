package edu.uky.cs.nil.tt;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

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
 * <p>
 * To avoid creating huge files, a new system log file is created for each day
 * the server is running. The files are named according to the date they were
 * created so they can be easily located, searched, and archived.
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
		message = "[" + Timestamped.toString(Instant.now()) + "] " + message + "\n";
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
	
	/** The date format used to name log files */
	private static DateTimeFormatter date = DateTimeFormatter.ofPattern("yyyy-MM-dd'Z'").withZone(ZoneOffset.UTC);
	
	/** The directory to which system logs will be written */
	private final File logs;
	
	/** The date the current system log files was created */
	private LocalDate today = null;
	
	/** The file writer for the current system log */
	private FileWriter log = null;
	
	/** The (possibly buffered) writer for system log messages */
	private Writer writer = null;
	
	/** The directory to which completed session files will be written */
	private final File sessions;
	
	/**
	 * Constructs a new server log.
	 * 
	 * @param logs the directory to which system log files should be written,
	 * or null if system logs messages should not be recorded
	 * @param sessions the directory to which completed session files should be
	 * written, or null if sessions should not be recorded
	 * @throws IOException if a problem occurs when creating or opening the log
	 * files or session directory
	 */
	public Log(File logs, File sessions) throws IOException {
		if(logs == null) {
			append("Server logs will not be written to file.");
			this.logs = null;
		}
		else if(logs.exists() && !logs.isDirectory())
			throw new IOException("The path \"" + sessions.getPath() + "\" is not a directory.");
		else {
			if(!logs.exists())
				logs.mkdir();
			this.logs = logs;
			checkDate();
			append("Server logs will be written to \"" + logs.getPath() + "\".");
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
		checkDate();
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
		checkDate();
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
	
	/**
	 * If a system log file is being used, and if the date has changed since
	 * the current system log was created, create a new system log file.
	 * 
	 * @throws IOException if a problem occurs when closing an old log or
	 * opening a new log
	 */
	private final void checkDate() {
		if(logs == null)
			return;
		LocalDate today = LocalDate.now();
		if(this.today == null || !today.equals(this.today)) {
			if(writer != null) {
				try {
					writer.flush();
					writer.close();
				}
				catch(IOException exception) {
					exception.printStackTrace();
				}
			}
			String url = logs.getPath() + "/" + date.format(today) + ".txt";
			this.today = today;
			try {
				log = new FileWriter(url, true);
				writer = new BufferedWriter(log);
			}
			catch(IOException exception) {
				exception.printStackTrace();
			}
		}
	}
	
	@Override
	public void close() throws IOException {
		if(log != null) {
			writer.flush();
			log.close();
		}
	}
}