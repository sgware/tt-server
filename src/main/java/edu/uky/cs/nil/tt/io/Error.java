package edu.uky.cs.nil.tt.io;

import edu.uky.cs.nil.tt.Utilities;

/**
 * The error message is sent from the {edu.uky.cs.nil.tt.Server server} to an
 * {@link edu.uky.cs.nil.tt.Agent agent} if one of the agent's messages causes
 * a problem or is incorrectly formatted.
 * 
 * @author Stephen G. Ware
 */
public class Error extends Message {
	
	/** A message explaining the error */
	public final String message;
	
	/**
	 * Constructs a new error message.
	 * 
	 * @param message a string explaning the error
	 */
	public Error(String message) {
		this.message = message;
	}
	
	/**
	 * Constructs a new error message using {@link Exception#getMessage() an
	 * exception's message} as the message.
	 * 
	 * @param exception the exception that caused an error and whose message
	 * should be send to the agent
	 */
	public Error(Exception exception) {
		this(exception.getMessage());
	}
	
	@Override
	public String toString() {
		return "[Error Message: \"" + message + "\"]";
	}
	
	@Override
	public void verify() {
		Utilities.requireNonNull(message, "message");
	}
}