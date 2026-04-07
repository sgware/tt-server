package edu.uky.cs.nil.tt.world;

import java.util.function.Function;

/**
 * A logical object is anything that is itself a predicate logic formula or
 * contains a predicate logic formula.
 * 
 * @author Stephen G. Ware
 */
public interface Logical {
	
	/**
	 * Returns a logical object identical to this one, except that its logical
	 * elements have been replaced according to the given substitution. A
	 * substitution defines how some logical formula (the input to the function)
	 * should be replaced with a different formula (the output of the function).
	 * 
	 * @param substitution a function which maps logical formula that should be
	 * replaced to the formula they should be replaced with
	 * @return an identical object, except that its logical formula have been
	 * replaced according to the substitution
	 */
	public Logical substitute(Function<Object, Object> substitution);
}