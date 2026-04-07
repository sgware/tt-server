package edu.uky.cs.nil.tt.world;

/**
 * A described object is associated with a human-readable natural language
 * description.
 * 
 * @author Stephen G. Ware
 */
@FunctionalInterface
public interface Described {
	
	/**
	 * Returns a human-readable natural language description of the object.
	 * 
	 * @return a human-readable natural language description of the object
	 */
	public String getDescription();
}