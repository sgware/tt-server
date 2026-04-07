package edu.uky.cs.nil.tt.world;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.function.Function;

import edu.uky.cs.nil.tt.Role;
import edu.uky.cs.nil.tt.Utilities;

/**
 * An action is a {@link SignedAsset signed asset} that causes a change in a
 * {@link World story world's} {@link State state}. The {@link
 * edu.uky.cs.nil.tt.Agent agents} in a session negotiate when actions happen
 * via {@link Turn turns}.
 * 
 * @author Stephen G. Ware
 */
public final class Action extends SignedAsset implements Comparable<Action>, Encoded {
	
	/**
	 * Entities representing characters in the story world who need to agree
	 * to take the action
	 */
	private final Entity[] consenting;
	
	/** The action's {@link #consenting consenting characters} as a set */
	private transient Set<Entity> consentingSet = null;
	
	/** The action's {@link Encoded code} */
	private final String code;
	
	/**
	 * Constructs a new action.
	 * 
	 * @param id the action's unique ID number
	 * @param signature the action's unique signature
	 * @param consenting an array of entities representing characters in the
	 * the story world who need to agree to take the action
	 * @param description the action's description
	 * @param code the action's code
	 */
	public Action(int id, Signature signature, Entity[] consenting, String description, String code) {
		super(id, signature, description);
		if(consenting == null)
			consenting = new Entity[0];
		Utilities.requireAllNonNull(consenting, "consenting characters");
		this.consenting = consenting;
		this.code = code;
	}
	
	@Override
	public int compareTo(Action other) {
		return this.getID() - other.getID();
	}
	
	@Override
	protected Action setID(int id) {
		return new Action(id, signature, getConsentingArray(), getDescription(), getCode());
	}
	
	@Override
	protected Action setSignature(Signature signature) {
		return new Action(getID(), signature, getConsentingArray(), getDescription(), getCode());
	}
	
	/**
	 * Returns an unmodifiable set of entities in the story world representing
	 * the characters who need to agree to take the action. An action with no
	 * consenting characters represents an accident or happening that can occur
	 * and time it is convenient for the story. An action with one consenting
	 * character means it is taken by a single character. An action with two or
	 * more consenting characters means it is a joint action taken by many
	 * characters who all need to have a reason to do it. Note that a character
	 * can be involved in an action without being a consenting character if the
	 * action represents something that happens to the character against their
	 * will.
	 * 
	 * @return the action's set of consenting characters
	 */
	public Set<Entity> getConsenting() {
		if(consentingSet == null) {
			LinkedHashSet<Entity> set = new LinkedHashSet<>(consenting.length);
			for(Entity entity : consenting)
				set.add(entity);
			consentingSet = Collections.unmodifiableSet(set);
		}
		return consentingSet;
	}
	
	private final Entity[] getConsentingArray() {
		Set<Entity> consenting = getConsenting();
		return consenting.toArray(new Entity[consenting.size()]);
	}
	
	/**
	 * Returns a new action that is the same as this action, except that its
	 * consenting characters is the given set.
	 * 
	 * @param consenting the consenting characters the new action should have
	 * @return an action identical to this action, except with the given
	 * consenting characters
	 */
	protected Action setConsenting(Set<Entity> consenting) {
		return new Action(getID(), signature, consenting.toArray(new Entity[consenting.size()]), getDescription(), getCode());
	}
	
	@Override
	public Action setDescription(String description) {
		return new Action(getID(), signature, getConsentingArray(), description, getCode());
	}
	
	@Override
	public String getCode() {
		return code;
	}
	
	/**
	 * Returns a new action that is the same as this action, except this its
	 * {@link Encoded code} is the given value.
	 * 
	 * @param code the code the new action should have
	 * @return an action identical to this action, except with the given code
	 */
	protected Action setCode(String code) {
		return new Action(getID(), signature, getConsentingArray(), getDescription(), code);
	}
	
	/**
	 * Returns true if the given story role controls one or more of the {@link
	 * #getConsenting() consenting characters} for this action. An action with
	 * no consenting character requires on the consent of the game master. An
	 * action whose only consenting character is the player character requires
	 * only the consent of the player. All other actions require the consent of
	 * both roles.
	 * 
	 * @param role the role in question
	 * @return true if the given role needs to consent to take this action
	 */
	public boolean consents(Role role) {
		Set<Entity> consenting = getConsenting();
		boolean gm = consenting.size() == 0;
		boolean player = false;
		for(Entity character : getConsenting()) {
			if(character.isPlayer())
				player = true;
			else
				gm = true;
		}
		if(role == Role.GAME_MASTER)
			return gm;
		else
			return player;
	}
	
	@Override
	public Action substitute(Function<Object, Object> substitution) {
		Signature signature = this.signature.substitute(substitution);
		boolean modified = signature != this.signature;
		Entity[] consenting = new Entity[this.consenting.length];
		for(int i = 0; i < consenting.length; i++) {
			consenting[i] = Utilities.requireType(substitution.apply(this.consenting[i]), Entity.class, "entity");
			modified = modified || consenting[i] != this.consenting[i];
		}
		if(modified)
			return new Action(getID(), signature, consenting, getDescription(), getCode());
		else
			return this;
	}
}