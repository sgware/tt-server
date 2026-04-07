package edu.uky.cs.nil.tt.world;

/**
 * A unique object has a sequential integer ID that specifically distinguishes
 * it from other objects of the same type in the same {@link World story world}.
 * 
 * @author Stephen G. Ware
 */
@FunctionalInterface
public interface Unique {
	
	/**
	 * Returns this object's unique, sequential, integer ID number that
	 * distinguishes it from other objects of the same type in the same
	 * story world.
	 * 
	 * @return the object's ID number
	 */
	public int getID();
}