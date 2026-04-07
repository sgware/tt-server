package edu.uky.cs.nil.tt.world;

import java.util.function.Function;

/**
 * An ending is a {@link SignedAsset signed asset} that represents one of
 * several possible ways a story can end.
 * 
 * @author Stephen G. Ware
 */
public final class Ending extends SignedAsset implements Comparable<Ending>, Encoded {
	
	/** The action's {@link Encoded code} */
	private final String code;
	
	/**
	 * Constructs a new ending.
	 * 
	 * @param id the ending's unique ID number
	 * @param signature the ending's unique signature
	 * @param description the ending's description
	 * @param code the ending's code
	 */
	public Ending(int id, Signature signature, String description, String code) {
		super(id, signature, description);
		this.code = code;
	}
	
	@Override
	public int compareTo(Ending other) {
		return this.getID() - other.getID();
	}
	
	@Override
	protected Ending setID(int id) {
		return new Ending(id, signature, getDescription(), getCode());
	}
	
	@Override
	protected Ending setSignature(Signature signature) {
		return new Ending(getID(), signature, getDescription(), getCode());
	}
	
	@Override
	public Ending setDescription(String description) {
		return new Ending(getID(), signature, description, getCode());
	}
	
	@Override
	public String getCode() {
		return code;
	}
	
	/**
	 * Returns a new ending that is the same as this ending, except this its
	 * {@link Encoded code} is the given value.
	 * 
	 * @param code the code the new ending should have
	 * @return an ending identical to this ending, except with the given code
	 */
	protected Ending setCode(String code) {
		return new Ending(getID(), signature, getDescription(), code);
	}
	
	@Override
	public Ending substitute(Function<Object, Object> substitution) {
		Signature signature = this.signature.substitute(substitution);
		if(signature != this.signature)
			return new Ending(getID(), signature, getDescription(), getCode());
		else
			return this;
	}
}