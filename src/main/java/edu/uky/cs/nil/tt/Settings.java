package edu.uky.cs.nil.tt;

import java.util.Set;

/**
 * Constants and other project-wide settings.
 * 
 * @author Stephen G. Ware
 */
public class Settings {
	
	/** The name of this project */
	public static final String NAME = "Tandem Tails Server";
	
	/** The current version of this project */
	public static final String VERSION = "0.9.0";
	
	/** The people who contributed significantly to this project */
	public static final String AUTHORS = "Stephen G. Ware";
	
	/** A full title, including name, version number, and authors */
	public static final String TITLE = NAME + " v" + VERSION + " by " + AUTHORS;
	
	/** The maximum number of characters allowed in a {@link Named name} */
	public static final int NAME_MAX_LENGTH = 20;
	
	/** The characters that can be legally used in a {@link Named name} */
	public static final Set<Character> NAME_ALLOWED_CHARACTERS = Set.of(
		'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z',
		'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z',
		'0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
		'_'
	);
	
	/**
	 * The amount of time in milliseconds between {@link ClockThread clock
	 * thread} {@link Server#tick() ticks}
	 */
	public static final long TICK_SPEED = 5000; // Every 5 seconds
	
	/**
	 * The amount of time in milliseconds an agent has to send an expected
	 * message before they will be disconnected for inactivity 
	 */
	public static final long AGENT_TIMEOUT = 180000; // 3 minutes
	
	/**
	 * The number of passed turn in a row which causes a session to be
	 * automatically stopped
	 */
	public static final int PASS_LIMIT = 6;
	
	/** The default port the server will listen on, if one is not specified */
	public static final int DEFAULT_PORT = 9005;
	
	private Settings() {
		// Do nothing.
	}
}