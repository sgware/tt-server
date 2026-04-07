package edu.uky.cs.nil.tt.io;

import edu.uky.cs.nil.tt.Utilities;
import edu.uky.cs.nil.tt.world.Status;

/**
 * The updates message is sent from the {edu.uky.cs.nil.tt.Server server} to an
 * {@link edu.uky.cs.nil.tt.Agent agent} each time a turn is taken and the
 * state of the story world changes. An update is also sent immediately after
 * a session {@link Start starts} to give the story world's initial state.
 * 
 * @author Stephen G. Ware
 */
public class Update extends Message {
	
	/**
	 * An object describing the history of the story so far, the current state
	 * of the story world, and what turns are available for the agent to take,
	 * if any
	 */
	public final Status status;
	
	/**
	 * Constructs a new update message with the story world's current status.
	 * 
	 * @param status the history, current state, and available turns in the
	 * session's story world
	 */
	public Update(Status status) {
		this.status = status;
	}
	
	@Override
	public String toString() {
		return "[Update Message: " + status.getChoices().size() + " choices]";
	}
	
	@Override
	public void verify() {
		Utilities.requireNonNull(status, "status");
	}
}