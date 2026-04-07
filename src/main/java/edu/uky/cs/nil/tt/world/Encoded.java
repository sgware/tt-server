package edu.uky.cs.nil.tt.world;

/**
 * An encoded object has a unique string of {@code 1}'s and {@code 0}'s that
 * uniquely distinguishes it object from others of the same type in the same
 * {@link World story world}. Encodings are typically produced by the {@link
 * Encoding#encode(Object)} method.
 * 
 * @author Stephen G. Ware
 */
@FunctionalInterface
public interface Encoded {
	
	/**
	 * Returns a string of {@code 1}'s and {@code 0}'s that uniquely represents
	 * this object among other objects of the same type in the same story world.
	 * Every encoded object of the same type should return a string of the same
	 * length, even if it must be padded with {@code 0}'s.
	 * 
	 * @return a string of 1's and 0's
	 */
	public String getCode();
}