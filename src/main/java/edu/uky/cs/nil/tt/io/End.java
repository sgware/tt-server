package edu.uky.cs.nil.tt.io;

/**
 * The end message is sent from the {edu.uky.cs.nil.tt.Server server} to an
 * {@link edu.uky.cs.nil.tt.Agent agent} to indicate that the session had ended
 * and that they should now disconnect. If the server is logging sessions, this
 * message contains ID of the logged session.
 * 
 * @author Stephen G. Ware
 */
public class End extends Message {
	
	/** The ID of the session, or null if the session was not logged */
	public final String session;
	
	/**
	 * Constructs a new end message with the given session ID.
	 * 
	 * @param session the ID of the session that ended, or null if the session
	 * was not logged
	 */
	public End(String session) {
		this.session = session;
	}
	
	@Override
	public String toString() {
		return "[End Message" + (session == null ? "" : ": session=\"" + session + "\"") + "]";
	}
	
	@Override
	public void verify() {
		// Nothing to verify.
	}
}