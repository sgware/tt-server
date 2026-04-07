package edu.uky.cs.nil.tt;

/**
 * A named object has a unique name that specifically distinguishes it from
 * other objects of the same type in the same context.
 * 
 * @author Stephen G. Ware
 */
@FunctionalInterface
public interface Named {
	
	/**
	 * Returns this object's unique name that distinguishes it from other
	 * objects of the same type in the same context.
	 * 
	 * @return the object's unique name
	 */
	public String getName();
}