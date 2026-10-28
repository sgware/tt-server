package edu.uky.cs.nil.tt;

import java.io.File;

import com.sgware.serialsoc.Clock;

/**
 * The main entry point for the Tandem Tales server. This class includes the
 * {@link #main(String[]) main method} to launch the server and its components.
 * 
 * @author Stephen G. Ware
 */
public class ServerMain {
	
	/** The key used to print the usage instructions */
	public static final String HELP_KEY = "help";
	
	/** The key used to specify that server messages should be logged to file */
	public static final String LOGS_KEY = "logs";
	
	/** The default location of the log files, if one is not specified */
	public static final String DEFAULT_LOGS_DIRECTORY = "logs/";
	
	/**
	 * The key used to specify that the current server status should be written
	 * to file
	 */
	public static final String STATUS_KEY = "status";
	
	/**
	 * The default file where current server status will be written if one is
	 * not specified
	 */
	public static final String DEFAULT_STATUS_FILE = "status.json";
	
	/** The key used to specify that sessions should be logged to file */
	public static final String SESSIONS_KEY = "sessions";
	
	/**
	 * The default location of the session log directory, if one is not
	 * specified
	 */
	public static final String DEFAULT_SESSIONS_DIRECTORY = "sessions/";
	
	/**
	 * The key used to specify that the server should read or save a database
	 * of story worlds and agents
	 */
	public static final String DATABASE_KEY = "database";
	
	/** The default location of the database file, if one is not specified */
	public static final String DEFAULT_DATABASE_FILE = "database.json";
	
	/** The key used to specify on which port the server should listen */
	public static final String PORT_KEY = "p";
	
	/** The default port the server will listen on, if one is not specified */
	public static final int DEFAULT_PORT = Settings.DEFAULT_PORT;
	
	/** Usage instructions for the server executable */
	public static final String USAGE =
		"Command line arguments:\n" +
		rpad("  -" + HELP_KEY) + "Print usage information.\n" +
		rpad("  -" + LOGS_KEY + " <directory>") + "Log server events to directory (if no value given, defaults to \"" + DEFAULT_LOGS_DIRECTORY + "\").\n" +
		rpad("  -" + STATUS_KEY + " <file>") + "Write current server status to file (if no value given, defaults to \"" + DEFAULT_STATUS_FILE + "\").\n" +
		rpad("  -" + SESSIONS_KEY + " <directory>") + "Log sessions to directory (if no value given, defaults to \"" + DEFAULT_SESSIONS_DIRECTORY + "\").\n" +
		rpad("  -" + DATABASE_KEY + " <file>") + "Use a database of worlds and agents (if no value given, defaults to \"" + DEFAULT_DATABASE_FILE + "\").\n" +
		rpad("  -" + PORT_KEY + " <number>") + "Listen on this network port (defaults to \"" + DEFAULT_PORT + "\").";
	
	private static final String rpad(String string) {
		return String.format("%-25s", string);
	}
	
	/**
	 * Configure and run a server based on Java arguments passed from the
	 * terminal.
	 * 
	 * @param args the arguments passed to this executable
	 * @throws Exception if a problem occurs during the server setup or while
	 * it is running
	 */
	public static void main(String[] args) throws Exception {
		// Title and Usage
		System.out.println(Settings.TITLE);
		Arguments arguments = new Arguments(args);
		if(arguments.contains(HELP_KEY)) {
			System.out.println(USAGE);
			System.out.println(new CommandParser(null).getUsage());
			return;
		}
		// Arguments
		File log = null;
		if(arguments.contains(LOGS_KEY))
			log = new File(arguments.getValue(LOGS_KEY, DEFAULT_LOGS_DIRECTORY));
		File status = null;
		if(arguments.contains(STATUS_KEY))
			status = new File(arguments.getValue(STATUS_KEY, DEFAULT_STATUS_FILE));
		File sessions = null;
		if(arguments.contains(SESSIONS_KEY))
			sessions = new File(arguments.getValue(SESSIONS_KEY, DEFAULT_SESSIONS_DIRECTORY));
		File database = null;
		if(arguments.contains(DATABASE_KEY))
			database = new File(arguments.getValue(DATABASE_KEY, DEFAULT_DATABASE_FILE));
		int port = DEFAULT_PORT;
		if(arguments.contains(PORT_KEY))
			port = Utilities.toInteger(arguments.getValue(PORT_KEY, Integer.toString(DEFAULT_PORT)));
		arguments.checkUnused();
		// Server and Command Parser
		Server server = new Server(log, status, sessions, database, port);
		CommandParser parser = new CommandParser(server);
		// Run
		Clock clock = new Clock(server, Settings.TICK_SPEED);
		clock.start();
		parser.start();
		try {
			server.run();
		}
		finally {
			clock.interrupt();
			parser.interrupt();
		}
	}
	
	private ServerMain() {
		// Do nothing.
	}
}