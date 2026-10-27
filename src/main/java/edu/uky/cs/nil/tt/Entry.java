package edu.uky.cs.nil.tt;

import java.util.Objects;

/**
 * An entry is a publicly available object that has a {@link Titled name, title,
 * and description}.
 * 
 * @author Stephen G. Ware
 */
public class Entry implements Titled {
	
	/**
	 * The unique system {@link Named name} for this element, which is
	 * primarily used internally by the server
	 */
	private final String name;
	
	/**
	 * A short natural language phrase for this element that will be displayed
	 * to users
	 */
	private String title;
	
	/**
	 * A short natural language statement explaining the details of this element
	 * that will be displayed to users
	 */
	private String description;
	
	/**
	 * Constructs an entry from its name, title, and description.
	 * 
	 * @param name the unique system {@link Named name} for this element, which
	 * is primarily used internally by the server
	 * @param title a short natural language phrase for this element that will
	 * be displayed to users
	 * @param description a short natural language statement explaining the
	 * details of this element that will be displayed to users
	 * @throws NullPointerException if the name is null
	 */
	public Entry(String name, String title, String description) {
		Utilities.requireNonNull(name, "name");
		this.name = name;
		this.title = title;
		this.description = description;
	}
	
	/**
	 * Constructs an entry using the meta-data of another object.
	 * 
	 * @param titled the object whose name, title, and description will be
	 * copied
	 * @throws NullPointerException if the object's name is null
	 */
	public Entry(Titled titled) {
		this(titled.getName(), titled.getTitle(), titled.getDescription());
	}
	
	/**
	 * Constructs an entry from only a name.
	 * 
	 * @param name the unique system {@link Named name} for this element, which
	 * is primarily used internally by the server
	 */
	public Entry(String name) {
		this(name, null, null);
	}
	
	@Override
	public boolean equals(Object other) {
		return other instanceof Entry otherEntry &&
			this.name.equals(otherEntry.name) &&
			Objects.equals(this.title, otherEntry.title) &&
			Objects.equals(this.description, otherEntry.description);
	}
	
	@Override
	public int hashCode() {
		return name.hashCode();
	}
	
	@Override
	public String toString() {
		return name;
	}
	
	@Override
	public final String getName() {
		return name;
	}
	
	@Override
	public String getTitle() {
		if(title == null)
			return name;
		else
			return title;
	}
	
	/**
	 * Sets the {@link Titled title} of this object.
	 * 
	 * @param title a short natural language phrase for this element that will
	 * be displayed to users
	 */
	public void setTitle(String title) {
		this.title = title;
	}
	
	@Override
	public String getDescription() {
		return description;
	}
	
	/**
	 * Sets the {@link Described description} of this object.
	 * 
	 * @param description a short natural language statement explaining the
	 * details of this element that will be displayed to users
	 */
	public void setDescription(String description) {
		this.description = description;
	}
}