package edu.uky.cs.nil.tt.world;

import java.util.function.Function;

import edu.uky.cs.nil.tt.Named;
import edu.uky.cs.nil.tt.Utilities;

/**
 * An entity is an {@link Asset asset} that represents a character, object,
 * place, or idea in a {@link World story world}.
 * 
 * @author Stephen G. Ware
 */
public final class Entity extends Asset implements Comparable<Entity>, Named, Encoded, Value {
	
	/** The entity's {@link Encoded code} */
	private final String code;
	
	/**
	 * Constructs a new entity.
	 * 
	 * @param id the entity's unique ID number
	 * @param name the entity's unique name
	 * @param description the entity's description
	 * @param code the entity's code
	 */
	public Entity(int id, String name, String description, String code) {
		super(id, name, description);
		Utilities.requireName(name);
		this.code = code;
	}
	
	@Override
	public int compareTo(Entity other) {
		return this.getID() - other.getID();
	}
	
	@Override
	protected Entity setID(int id) {
		return new Entity(id, getName(), getDescription(), getCode());
	}
	
	@Override
	public Entity setDescription(String description) {
		return new Entity(getID(), getName(), description, getCode());
	}
	
	@Override
	public String getCode() {
		return code;
	}
	
	/**
	 * Returns a new entity that is the same as this entity, except this its
	 * {@link Encoded code} is the given value.
	 * 
	 * @param code the code the new entity should have
	 * @return an entity identical to this entity, except with the given code
	 */
	protected Entity setCode(String code) {
		return new Entity(getID(), getName(), getDescription(), code);
	}

	@Override
	public Entity substitute(Function<Object, Object> substitution) {
		return this;
	}
	
	@Override
	public Entity evaluate(State state) {
		return this;
	}
	
	@Override
	public Entity toEntity() {
		return this;
	}
	
	/**
	 * Returns true if this entity represents the player character in its story
	 * world.
	 * <p>
	 * By default, the entity with ID number 0 is assumed to be the player
	 * character.
	 * 
	 * @return true if this entity represents the player character, false
	 * otherwise
	 */
	public boolean isPlayer() {
		return id == 0;
	}
}