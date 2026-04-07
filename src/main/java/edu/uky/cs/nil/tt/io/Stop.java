package edu.uky.cs.nil.tt.io;

import edu.uky.cs.nil.tt.Role;
import edu.uky.cs.nil.tt.world.Ending;

/**
 * The stop message can be sent from either the {edu.uky.cs.nil.tt.Server
 * server} or the {@link edu.uky.cs.nil.tt.Agent agent} to indicate that no more
 * turns will happen in the session (though {@link Report reports} may still
 * happen) and that the session should end soon.
 * <p>
 * An agent can send the stop message (1) if they want to stop a session early
 * before the story had reached one of its defined endings, or (2) to indicate
 * they are finished and have no more {@link Report reports} to send. After an
 * agent sends the stop message, they may not send any more messages (including
 * reports), though they may stay connected to wait for the {@link End end}
 * message.
 * <p>
 * The server will send the stop message to notify both agents that no more
 * turns can be taken. This will happen when (1) the story reaches one of its
 * defined endings, (2) one of the agents in the session stops it early, (3) one
 * of the agents in the session disconnects, or (4) the server shuts down.
 * <p>
 * Once an agent receives the stop message, it cannot take any more turns, even
 * if the story has not reached one of its defined endings. However, an agent
 * may still send {@link Report reports}. When an agent is done sending reports,
 * it should send a stop message to indicate this.
 * <p>
 * After both agents have either sent stop messages or disconnected, the {@link
 * End end} message will be sent to both agents.
 * 
 * @author Stephen G. Ware
 */
public class Stop extends Message {
	
	/**
	 * One of the {@link edu.uky.cs.nil.tt.world.World#getEndings() pre-defined
	 * endings} from the story world, or null if this session did not reach one
	 * of those endings
	 */
	public final Ending ending;
	
	/**
	 * The participant who ended the session, or null if the session reached a
	 * pre-defined ending or was ended by the server
	 */
	public final Role role;
	
	/** A message explaining how the session ended */
	public final String message;
	
	/**
	 * Constructs a stop message from a pre-defined story ending.
	 * 
	 * @param ending the pre-defined ending that occurred in the story
	 */
	public Stop(Ending ending) {
		this.ending = ending;
		this.role = null;
		if(ending != null)
			this.message = "The story has ended.";
		else
			this.message = null;
	}
	
	/**
	 * Constructs a stop message for the role who stopped the session before
	 * it reached a pre-defined ending.
	 * 
	 * @param role the role who stopped the story
	 */
	public Stop(Role role) {
		this.ending = null;
		this.role = role;
		if(role == Role.GAME_MASTER)
			this.message = "The game master ended the session early.";
		else if(role == Role.PLAYER)
			this.message = "The player ended the session early.";
		else
			this.message = null;
	}
	
	/**
	 * Constructs a stop message with a string explaining how the session
	 * ended before it reached a pre-definded ending.
	 * 
	 * @param message a string explaining why the session ended
	 */
	public Stop(String message) {
		this.ending = null;
		this.role = null;
		this.message = message;
	}
	
	@Override
	public String toString() {
		return "[Stop Message" + (message == null ? "" : ": \"" + message + "\"") + "]";
	}
	
	@Override
	public void verify() {
		// Nothing to verify.
	}
}