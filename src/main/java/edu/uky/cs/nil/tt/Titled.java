package edu.uky.cs.nil.tt;

/**
 * A titled object is has a unique system {@link Named name}, a short natural
 * language title, and a longer natural language {@link Described description}.
 * 
 * @author Stephen G. Ware
 */
public interface Titled extends Named, Described {
	
	/**
	 * Returns a short human-readable natural language title for the object.
	 * 
	 * @return a short human-readable natural language title
	 */
	public String getTitle();
}