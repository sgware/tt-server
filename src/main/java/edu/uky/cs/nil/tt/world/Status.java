package edu.uky.cs.nil.tt.world;

import java.util.List;

import edu.uky.cs.nil.tt.Role;
import edu.uky.cs.nil.tt.Utilities;

/**
 * A status contains the full history of a {@link edu.uky.cs.nil.tt.Session
 * session} in a {@link World story world}, a description of its current {@link
 * State state}, and a list of {@link Turn turns} that can be taken next (if
 * any).
 * 
 * @author Stephen G. Ware
 */
public class Status {
	
	/** The role of the participant to whom this status is being described */
	public final Role role;
	
	/** All turns that have happened so far in the session */
	private final Turn[] history;
	
	/** The current values of all variables the story world */
	private final State state;
	
	/**
	 * The ending the session's story has reached, or null if the story has not
	 * yet ended
	 */
	private final Ending ending;
	
	/** Descriptions of the entities that are currently visible to the role */
	private final Entity[] descriptions;
	
	/** A list of turns the role can take next (which may be none) */
	private final Turn[] choices;
	
	/**
	 * Constructs a status.
	 * 
	 * @param world the name of the story world being used in this session
	 * @param role the role to whom this status is being sent and for whom the
	 * descriptions are being generated
	 * @param history all turns that have happened so far in the session
	 * @param state the current value of all of the story world's variables
	 * @param ending the ending this session's story has reached, or null if no
	 * ending has been reached
	 * @param descriptions the entities that are currently visible to the role
	 * @param choices the turns this role can take next (which may be none)
	 */
	public Status(String world, Role role, Turn[] history, State state, Ending ending, Entity[] descriptions, Turn[] choices) {
		Utilities.requireNonNull(role, "role");
		this.role = role;
		if(history == null)
			history = new Turn[0];
		Utilities.requireAllNonNull(history, "turn");
		this.history = history;
		Utilities.requireNonNull(state, "state");
		this.state = state;
		this.ending = ending;
		if(descriptions == null)
			descriptions = new Entity[0];
		Utilities.requireAllNonNull(descriptions, "description");
		this.descriptions = descriptions;
		if(choices == null)
			choices = new Turn[0];
		Utilities.requireAllNonNull(choices, "choice");
		this.choices = choices;
	}
	
	@Override
	public String toString() {
		String string = "[" + role + " status:";
		string += " " + history.length + " turns";
		string += "; " + choices.length + " choices";
		return string + "]";
	}
	
	/**
	 * Returns an unmodifiable list of all turns that have happened so far in
	 * the session.
	 * 
	 * @return a list of turns
	 */
	public List<Turn> getHistory() {
		return List.of(history);
	}
	
	/**
	 * Returns a state that gives the current value of all the story world's
	 * {@link Variable variables}.
	 * 
	 * @return the current state of the story world
	 */
	public State getState() {
		return state;
	}
	
	/**
	 * Returns the ending the session's story has reached, or null if the story
	 * has not yet ended.
	 * 
	 * @return the story's ending, or null if it has not ended
	 */
	public Ending getEnding() {
		return ending;
	}
	
	/**
	 * Returns an unmodifiable list of all the {@link Entity entities} that
	 * {@link #role this role} can current see. Each one will have a {@link
	 * Entity#getDescription() natural language description} that gives some
	 * helpful context about it. Note that not all entities in the story world
	 * will be in this list; if an entity cannot be seen it is excluded.
	 * 
	 * @return a list of all visible entities and their descriptions
	 */
	public List<Entity> getDescriptions() {
		return List.of(descriptions);
	}
	
	/**
	 * Returns an unmodifiable list of next {@link Turn turns} that {@link #role
	 * this role} can take next in the session. This list will be empty if the
	 * story had ended or if it is not this role's turn to act.
	 * 
	 * @return a list of available next turns
	 */
	public List<Turn> getChoices() {
		return List.of(choices);
	}
}