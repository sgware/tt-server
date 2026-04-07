package edu.uky.cs.nil.tt.world;

import java.util.function.Function;

import edu.uky.cs.nil.tt.Utilities;

/**
 * An asset is any named object or concept in a {@link World story world}. Every
 * asset of the same type has a unique, sequential {@link #id ID number}
 * starting at 0 and a unique {@link #name name}. This means no two entities of
 * the same type share an ID number. For example, the first {@link
 * Entity} in a world has ID 0, and no other entity has ID 0, but the first
 * {@link Variable} in a world also has ID 0.
 * 
 * @author Stephen G. Ware
 */
public abstract class Asset implements Unique, Described, Logical {
	
	/**
	 * This asset's ID number, which is unique among other assets of the same
	 * type
	 */
	public final int id;
	
	/**
	 * This asset's name, which is unique among other assets of the same type
	 */
	public final String name;
	
	/** A human-readable {@link Described description} of the asset */
	private final String description;
	
	/**
	 * Constructs a new asset with the given ID number, name, and description.
	 * 
	 * @param id the asset's unique ID number
	 * @param name the asset's unique name
	 * @param description the asset's description
	 */
	public Asset(int id, String name, String description) {
		Utilities.requireNonNegative(id, "ID");
		this.id = id;
		Utilities.requireNonNull(name, "name");
		this.name = name;
		this.description = description;
	}
	
	@Override
	public boolean equals(Object other) {
		if(other instanceof Asset otherAsset)
			return this.id == otherAsset.id && this.name.equals(otherAsset.name);
		else
			return false;
	}
	
	@Override
	public int hashCode() {
		return id;
	}
	
	@Override
	public String toString() {
		return name;
	}
	
	@Override
	public int getID() {
		return id;
	}
	
	/**
	 * Returns a new asset that is the same as this asset, except that its ID
	 * number is the given value.
	 * 
	 * @param id the ID number the new asset should have
	 * @return an asset identical to this asset, except with the given ID number
	 */
	protected abstract Asset setID(int id);
	
	/**
	 * Returns the asset's name, which is unique among other assets of the same
	 * type in the story world.
	 * 
	 * @return the asset's unique name
	 */
	public String getName() {
		return name;
	}
	
	@Override
	public String getDescription() {
		return description;
	}
	
	/**
	 * Returns a new asset that is the same as this asset, except that its
	 * {@link Described description} is the given value.
	 * 
	 * @param description the description the new asset should have
	 * @return an asset identical to this asset, except with the given
	 * description
	 */
	public abstract Asset setDescription(String description);
	
	@Override
	public abstract Asset substitute(Function<Object, Object> substitution);
}