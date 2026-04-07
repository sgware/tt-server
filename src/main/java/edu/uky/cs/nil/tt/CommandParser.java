package edu.uky.cs.nil.tt;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A command parser listens (on {@link System#in standard input}) for and
 * executes simple text commands, like modifying the server's database and
 * shutting the server down.
 * 
 * @author Stephen G. Ware
 */
public class CommandParser extends Thread {
	
	/** A placeholder used in a command that includes a path to a file */
	private static final String PATH = "<path>";
	
	/** A placeholder used in a command that includes a name */
	private static final String NAME = "<name>";
	
	/** A placeholder used in a command that includes a string */
	private static final String STRING = "<string>";
	
	/** A placeholder used in a command that includes a number */
	private static final String NUMBER = "<number>";
	
	/** The command used to add new entries to the database */
	private static final String ADD = "add";
	
	/** The command used to remove entries from the database */
	private static final String REMOVE = "remove";
	
	/** The command used to set values in a database entry */
	private static final String SET = "set";
	
	/** The command used to mark a database entry as publicly available */
	private static final String LIST = "list";
	
	/** The command used to mark a database entry as not publicly available */
	private static final String DELIST = "delist";
	
	/** The command used to shut down the server */
	private static final String STOP = "stop";
	
	/**
	 * A keyword used to indicate that a database operation should be done to a
	 * world entry
	 */
	private static final String WORLD = "world";
	
	/**
	 * A keyword used to indicate that a database operation should be done to an
	 * agent entry
	 */
	private static final String AGENT = "agent";
	
	/** A keyword used to refer to the title of a database entry */
	private static final String TITLE = "title";
	
	/** A keyword used to refer to the description of a database entry */
	private static final String DESCRIPTION = "description";
	
	/**
	 * A keyword used to refer to the limit on the number of agents with the
	 * same name that may be in session simultaneously
	 */
	private static final String LIMIT = "limit";
	
	/** A keyword used to refer to the password of a database entry */
	private static final String PASSWORD = "password";
	
	/**
	 * An operation represents a single command that can be executed on a server
	 * based on a series of arguments that define how it behaves.
	 * 
	 * @author Stephen G. Ware
	 */
	@FunctionalInterface
	public interface Operation {
		
		/**
		 * Executes the command on the given server and configured by the given
		 * arguments.
		 * 
		 * @param server the server on which the command will be executed
		 * @param arguments the arguments that define what the command will do
		 * @throws Exception if a problem occurs when executing the command
		 */
		public void execute(Server server, String[] arguments) throws Exception;
	}
	
	/**
	 * A command is a type of operation that can be parsed by a {@link
	 * CommandParser command parser} and executed on a server.
	 * 
	 * @author Stephen G. Ware
	 */
	public static class Command {
		
		/** A human-readable description of the command's format */
		public final String format;
		
		/**
		 * A regular expression used to detect if this command is being invoked
		 */
		public final Pattern pattern;
		
		/** The operation to execute if this command is parsed */
		public final Operation operation;
		
		/**
		 * Constructs a new command from a description, regular expression, and
		 * operation to be executed when the command is parsed.
		 * 
		 * @param format a human-readable description of the command's format
		 * @param pattern a regular expression used to detect if the command is
		 * being invoked
		 * @param operation the operation to execute when this command is parsed
		 */
		public Command(String format, Pattern pattern, Operation operation) {
			this.format = format;
			this.pattern = pattern;
			this.operation = operation;
		}
		
		/**
		 * Constructs a new command (including its human-readable description
		 * and its regular expression) from a sequence of keywords.
		 * 
		 * @param words a sequence of key words used to invoke the command
		 * @param operation the operation to execute when this command is parsed
		 */
		public Command(String[] words, Operation operation) {
			String format = "";
			String pattern = "^";
			for(String word : words) {
				if(!format.isEmpty()) {
					format += " ";
					pattern += "\\s";
				}
				format += word;
				if(word.startsWith("<") && word.endsWith(">"))
					pattern += "(.+)";
				else
					pattern += word;
			}
			pattern += "$";
			this.format = format;
			this.pattern = Pattern.compile(pattern);
			this.operation = operation;
		}
	}
	
	/** The server on which commands will run */
	private final Server server;
	
	/** A list of types of commands that can be parsed */
	private final List<Command> commands = new ArrayList<>();
	
	/** A documentation string showing how to invoke all commands */
	private String usage = "";
	
	/**
	 * Constructs a new command parser for a server.
	 * 
	 * @param server the server on which commands will be executed
	 */
	public CommandParser(Server server) {
		this.server = server;
		// Add
		usage += "To add a new world or agent, type:";
		Command command = new Command(
			new String[] { ADD, WORLD, PATH },
			(svr, args) -> svr.database.addWorld(new File(args[0]))
		);
		usage += "\n  " + command.format;
		add(command);
		command = new Command(
			new String[] { ADD, AGENT, NAME },
			(svr, args) -> svr.database.addAgent(args[0])
		);
		usage += "\n  " + command.format;
		add(command);
		// Set
		usage += "\nTo set the details of a world or agent, type:";
		command = new Command(
			new String[] { SET, WORLD, NAME, TITLE, STRING },
			(svr, args) -> svr.database.setWorldTitle(args[0], args[1])
		);
		usage += "\n  " + command.format;
		add(command);
		command = new Command(
			new String[] { SET, WORLD, NAME, DESCRIPTION, STRING },
			(svr, args) -> svr.database.setWorldDescription(args[0], args[1])
		);
		usage += "\n  " + command.format;
		add(command);
		command = new Command(
			new String[] { SET, AGENT, NAME, TITLE, STRING },
			(svr, args) -> svr.database.setAgentTitle(args[0], args[1])
		);
		usage += "\n  " + command.format;
		add(command);
		command = new Command(
			new String[] { SET, AGENT, NAME, DESCRIPTION, STRING },
			(svr, args) -> svr.database.setAgentDescription(args[0], args[1])
		);
		usage += "\n  " + command.format;
		add(command);
		command = new Command(
			new String[] { SET, AGENT, NAME, PASSWORD, STRING },
			(svr, args) -> svr.database.setAgentPassword(args[0], args[1])
		);
		usage += "\n  " + command.format;
		add(command);
		command = new Command(
			new String[] { SET, AGENT, NAME, LIMIT, NUMBER },
			(svr, args) -> svr.database.setAgentLimit(args[0], Utilities.toInteger(args[1]))
		);
		usage += "\n  " + command.format;
		add(command);
		// List
		usage += "\nTo list a world or agent publically, type:";
		command = new Command(
			new String[] { LIST, WORLD, NAME },
			(svr, args) -> svr.database.setWorldListed(args[0], true)
		);
		usage += "\n  " + command.format;
		add(command);
		command = new Command(
			new String[] { DELIST, WORLD, NAME },
			(svr, args) -> svr.database.setWorldListed(args[0], false)
		);
		usage += "\n  " + command.format;
		add(command);
		command = new Command(
			new String[] { LIST, AGENT, NAME },
			(svr, args) -> svr.database.setAgentListed(args[0], true)
		);
		usage += "\n  " + command.format;
		add(command);
		command = new Command(
			new String[] { DELIST, AGENT, NAME },
			(svr, args) -> svr.database.setAgentListed(args[0], false)
		);
		usage += "\n  " + command.format;
		add(command);
		// Remove
		usage += "\nTo remove a world or agent, type:";
		command = new Command(
			new String[] { REMOVE, WORLD, NAME },
			(svr, args) -> svr.database.removeWorld(args[0])
		);
		usage += "\n  " + command.format;
		add(command);
		command = new Command(
			new String[] { REMOVE, AGENT, NAME },
			(svr, args) -> svr.database.removeAgent(args[0])
		);
		usage += "\n  " + command.format;
		add(command);
		// Stop
		usage += "\nTo stop the server, type:";
		command = new Command(
			new String[] { STOP },
			(svr, args) -> svr.close()
		);
		usage += "\n  " + command.format;
		add(command);
	}
	
	@Override
	public void run() {
		String line = null;
		do {
			try {
				line = readLine();
			}
			catch(InterruptedException | IOException exception) {
				break;
			}
			try {
				parse(line);
			}
			catch(Exception exception) {
				exception.printStackTrace();
			}
		} while(line != null);
	}
	
	/**
	 * Returns a list of all of the command this parser can recognize.
	 * 
	 * @return a list of commands this parser can recognize
	 */
	public List<Command> getCommands() {
		return Collections.unmodifiableList(commands);
	}
	
	/**
	 * Returns a documentation string showing how to use the commands this
	 * parser can recognize.
	 * 
	 * @return a documentation string
	 */
	public String getUsage() {
		return usage;
	}
	
	/**
	 * Adds a new command to the list of command this parser can recognize.
	 * 
	 * @param command the new command that will be recognized
	 */
	public void add(Command command) {
		commands.add(command);
	}
	
	private final String readLine() throws InterruptedException, IOException {
		String line = null;
		while(true) {
			if(System.in.available() == 0)
				Thread.sleep(200);
			else {
				int input = System.in.read();
				if(input == -1)
					return line;
				char character = (char) input;
				if(line == null)
					line = "";
				if(character == '\n' || character == '\r') {
					if(!line.isBlank())
						return line;
					else
						line = "";
				}
				else
					line += character;
			}
		}
	}
	
	/**
	 * Attempts to parse and execute a command from a string, typically one
	 * typed into {@link System#out standard input}.
	 * 
	 * @param command the string to attempt to parse as a command
	 * @throws Exception if the command cannot be parsed or if a problem
	 * occurs when executing the command
	 */
	public void parse(String command) throws Exception {
		for(Command c : getCommands()) {
			Matcher matcher = c.pattern.matcher(command);
			if(matcher.matches()) {
				String[] arguments = new String[matcher.groupCount()];
				for(int i = 0; i < arguments.length; i++)
					arguments[i] = matcher.group(i + 1);
				server.execute(() -> c.operation.execute(server, arguments));
				return;
			}
		}
		throw new IllegalArgumentException("The command \"" + command + "\" could not be parsed.");
	}
}