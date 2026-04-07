package edu.uky.cs.nil.tt.io;

import edu.uky.cs.nil.tt.Role;
import edu.uky.cs.nil.tt.Utilities;

/**
 * The join message is the first message sent from an {@link
 * edu.uky.cs.nil.tt.Agent agent} to the {edu.uky.cs.nil.tt.Server server} to
 * provide the agent's credentials and its preferences for what story world,
 * role, and partner they want for their session.
 * 
 * @author Stephen G. Ware
 */
public class Join extends Message {
	
	/** The new agent's name */
	public final String name;
	
	/**
	 * The new agent's password, which must be provided if the agent is using a
	 * {@link #name} that is reserved on this server, or which should be null if
	 * the agent is not using a reserved name
	 */
	public final String password;
	
	/**
	 * The name of the story world this new agent wants to play in, or null if
	 * they are willing to play in any story world
	 */
	public final String world;
	
	/**
	 * The role this new agent wants to have in their session, or null if they
	 * are willing to play either role
	 */
	public final Role role;
	
	/**
	 * The name of the partner this new agent wants to play with, or null if
	 * they are willing to play with any partner
	 */
	public final String partner;
	
	/**
	 * Constructs a new join message with an agent's credentials and
	 * preferences.
	 * 
	 * @param name the new agent's name
	 * @param password the new agent's password, or null if they are not using
	 * a reserved name
	 * @param world the world the new agent wants to play in, or null if they
	 * have no preference
	 * @param role the role the new agent wants to have, or null if they have no
	 * preference
	 * @param partner the name of the partner this new agent wants to play with,
	 * or null if they have no preference
	 */
	public Join(String name, String password, String world, Role role, String partner) {
		this.name = name;
		this.password = password;
		this.world = world;
		this.role = role;
		this.partner = partner;
	}
	
	@Override
	public String toString() {
		String string = "[Join Message: name=\"" + name + "\"";
		if(password != null)
			string += "; password";
		if(world != null)
			string += "; world=\"" + world + "\"";
		if(role != null)
			string += "; role=\"" + role + "\"";
		if(partner != null)
			string += "; partner=\"" + partner + "\"";
		return string + "]";
	}
	
	@Override
	public void verify() {
		Utilities.requireName(name);
		if(world != null)
			Utilities.requireName(world);
		if(partner != null)
			Utilities.requireName(partner);
	}
	
	/**
	 * Returns true if this join request is compatible with another, meaning a
	 * session could be created between the agents who sent the messages. This
	 * method will not match two requests which both specify no preference for a
	 * partner. In other words, at least one of the requests must specify a
	 * partner.
	 * 
	 * @param other the join message of another agent waiting for a session
	 * @return true if the join messages are compatible, or false if they are
	 * not compatible
	 */
	public boolean matches(Join other) {
		if(!matches(this.world, other.world))
			return false;
		else if(!matches(this.role, other.role == null ? null : other.role.getPartner()))
			return false;
		else if(!matches(this.name, other.partner) || !matches(this.partner, other.name) || (this.partner == null && other.partner == null))
			return false;
		else
			return true;
	}
	
	private static final boolean matches(Object o1, Object o2) {
		return o1 == null || o2 == null || o1.equals(o2);	
	}
}