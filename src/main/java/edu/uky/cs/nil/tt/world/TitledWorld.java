package edu.uky.cs.nil.tt.world;

import java.util.Collection;

import edu.uky.cs.nil.tt.Database;
import edu.uky.cs.nil.tt.Titled;

/**
 * A {@link World story world} that includes a name, title, and description.
 * 
 * @author Stephen G. Ware
 */
public class TitledWorld extends World implements Titled {
	
	/** The short natural language title of the world */
	public final String title;
	
	/** The longer natural language description of the world */
	public final String description;
	
	/**
	 * Constructs a titled world from its name, title, description, and
	 * elements.
	 * 
	 * @param name the unique system name for the world
	 * @param title the short natural language title of the world
	 * @param description the short natural language description of the
	 * world
	 * @param entities a collection of objects that exist in the world
	 * @param variables a collection of variables whose values can change
	 * and whose current values reflect the current state of the world
	 * @param actions a collection of ways the world state can change
	 * @param endings a collection of ways a story in this world can end
	 */
	public TitledWorld(
		String name,
		String title,
		String description,
		Collection<Entity> entities,
		Collection<Variable> variables,
		Collection<Action> actions,
		Collection<Ending> endings
	) {
		super(
			name,
			entities,
			variables,
			actions,
			endings
		);
		this.title = title;
		this.description = description;
	}
	
	/**
	 * Constructs a titled world from its database entry.
	 * 
	 * @param entry the world's entry in the database
	 */
	public TitledWorld(Database.WorldEntry entry) {
		this(
			entry.getName(),
			entry.getTitle(),
			entry.getDescription(),
			entry.getWorld().getEntities(),
			entry.getWorld().getVariables(),
			entry.getWorld().getActions(),
			entry.getWorld().getEndings()
		);
	}
	
	@Override
	public String getTitle() {
		return title;
	}
	
	@Override
	public String getDescription() {
		return description;
	}
}