package edu.uky.cs.nil.tt.io;

import edu.uky.cs.nil.tt.Role;
import edu.uky.cs.nil.tt.Utilities;
import edu.uky.cs.nil.tt.world.World;

/**
 * The start message is sent from the {edu.uky.cs.nil.tt.Server server} to an
 * {@link edu.uky.cs.nil.tt.Agent agent} when a partner has been found and their
 * session begins.
 * 
 * @author Stephen G. Ware
 */
public class Start extends Message {
	
	/** The story world in which the session will take place */
	public final World world;
	
	/** The role the recipient of this message will have in the session */
	public final Role role;
	
	/**
	 * Constructs a new start message with the given story world the session
	 * will take place in.
	 * 
	 * @param world the story world the session will take place in
	 * @param role the role the recipient of this message will play in the
	 * session
	 */
	public Start(World world, Role role) {
		this.role = role;
		this.world = world;
	}
	
	@Override
	public String toString() {
		return "[Start Message: world=\"" + world.getName() + "\"; role=\"" + role + "\"]";
	}
	
	@Override
	public void verify() {
		Utilities.requireNonNull(world, "world");
		Utilities.requireNonNull(role, "role");
	}
}