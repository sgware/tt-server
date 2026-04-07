package edu.uky.cs.nil.tt.world;

import edu.uky.cs.nil.tt.Role;
import edu.uky.cs.nil.tt.Utilities;

/**
 * A turn is one participant in a {@link  edu.uky.cs.nil.tt.Session session}
 * communicating their intentions for an {@link Action action} to the other
 * participant.
 * <p>
 * There are four types of turns:
 * <ul>
 * <li>{@link Type#PROPOSE Propose}</li>
 * <li>{@link Type#SUCCEED Succeed}</li>
 * <li>{@link Type#FAIL Fail}</li>
 * <li>{@link Type#PASS Pass}</li>
 * </ul>
 * 
 * @author Stephen G. Ware
 */
public class Turn implements Described, Encoded {
	
	/** The kind of turn being taken */
	public enum Type {
		
		/**
		 * Used when one participant wants to suggest taking an action that
		 * requires the approval of their partner
		 */
		PROPOSE,
		
		/**
		 * Used when one participant agrees to take a proposed action, or when
		 * a participant can take an action that does not require approval
		 */
		SUCCEED,
		
		/**
		 * Used to reject a proposed action
		 */
		FAIL,
		
		/**
		 * Used when one participant does not want to act and wants to allow
		 * their partner to act instead
		 */
		PASS
	}
	
	/** The role of the participant taking this turn */
	public final Role role;
	
	/** The kind of turn being taken */
	public final Type type;
	
	/** The action that this turn is relevant to */
	public final Action action;
	
	/** A natural language description of this turn */
	private final String description;
	
	/** The turn's {@link Encoded code} */
	private final String code;
	
	/**
	 * Constructs a new turn.
	 * 
	 * @param role the role of the participant taking this turn
	 * @param type the kind of turn being taken
	 * @param action the action relevant to this turn
	 * @param description a natural language description of this turn
	 * @param code the turn's {@link Encoded code}
	 */
	private Turn(Role role, Type type, Action action, String description, String code) {
		Utilities.requireNonNull(role, "role");
		this.role = role;
		Utilities.requireNonNull(type, "turn type");
		this.type = type;
		if(type != Type.PASS) {
			Utilities.requireNonNull(action, "action");
			this.action = action;
		}
		else
			this.action = null;
		this.description = description;
		Utilities.requireNonNull(code, "code");
		this.code = code;
	}
	
	/**
	 * Constructs a turn and automatically computes its {@link #getCode() code}.
	 * 
	 * @param role the role of the participant taking this turn
	 * @param type the kind of turn being taken
	 * @param action the action relevant to this turn
	 * @param description a natural language description of this turn
	 */
	public Turn(Role role, Type type, Action action, String description) {
		this(role, type, action, description, encode(role, type, action));
	}
	
	/**
	 * Constructs a turn with no description and automatically computes its
	 * {@link #getCode() code}.
	 * 
	 * @param role the role of the participant taking this turn
	 * @param type the kind of turn being taken
	 * @param action the action relevant to this turn
	 */
	public Turn(Role role, Type type, Action action) {
		this(role, type, action, null);
	}
	
	/**
	 * Constructs a {@link Type#PASS pass} turn for a given story world.
	 * 
	 * @param role the role who is passing
	 * @param world the story world the session takes place in
	 */
	public Turn(Role role, World world) {
		this(role, Turn.Type.PASS, null, null, encode(role, world));
	}
	
	private static final String encode(Role role, Type type, String action) {
		return Encoding.ROLE.encode(role) + Encoding.TURN_TYPE.encode(type) + action;
	}
	
	private static final String encode(Role role, Type type, Action action) {
		return encode(role, type, action.getCode());
	}
	
	private static final String encode(Role role, World world) {
		Encoding encoding = Encoding.get("action" + Utilities.bits(world.getActions().size() + 1));
		return encode(role, Turn.Type.PASS, encoding.encode(null));
	}
	
	@Override
	public String toString() {
		String string = role + " " + type;
		if(action != null)
			string += " " + action;
		return string;
	}
	
	@Override
	public String getDescription() {
		return description;
	}
	
	/**
	 * Returns a new turn that is the same as this turn, except this its {@link
	 * Described description} is the given value.
	 * 
	 * @param description the description the new turn should have
	 * @return a turn identical to this turn, except with the given description
	 */
	public Turn setDescription(String description) {
		return new Turn(role, type, action, description, getCode());
	}
	
	@Override
	public String getCode() {
		return code;
	}
}