package edu.uky.cs.nil.tt.world;

import java.util.ArrayList;
import java.util.List;

/**
 * A tool for defining the {@link Asset assets} used in a {@link World story
 * world}. Assets can be defined using this object's various {@code add}
 * methods, and then this object can be passed to {@link
 * World#World(AssetBuilder) World's constructor} to define the name and assets
 * the world will have.
 * <p>
 * Note that this object does not check the correctness of the defined assets.
 * For example, it is possible to define multiple assets of the same type with
 * the same name. An exception will be thrown when constructing the World
 * object, which checks the correctness of all asset definitions.
 * 
 * @author Stephen G. Ware
 */
public class AssetBuilder {
	
	/** The name of the story world */
	String name = null;
	
	/** A list of defined entities */
	final List<Entity> entities = new ArrayList<>();
	
	/** A list of defined variables */
	final List<Variable> variables = new ArrayList<>();
	
	/** A list of defined actions */
	final List<Action> actions = new ArrayList<>();
	
	/** A list of defined endings */
	final List<Ending> endings = new ArrayList<>();
	
	/**
	 * Constructs an empty asset builder.
	 */
	public AssetBuilder() {
		// Do nothing.
	}
	
	/**
	 * Constructs a new asset builder with a given world name.
	 * 
	 * @param name the name the world containing these assets will have
	 */
	public void setName(String name) {
		this.name = name;
	}
	
	/**
	 * Defines a new {@link Entity entity}.
	 * 
	 * @param name the unique name of the entity
	 * @param description a natural language description of the entity
	 * @return an entity object with those properties
	 */
	public Entity addEntity(String name, String description) {
		Entity entity = new Entity(entities.size(), name, description, "");
		entities.add(entity);
		return entity;
	}
	
	/**
	 * Defines a new {@link Variable variable}.
	 * 
	 * @param signature the unique signature of the variable
	 * @param encoding the name of the {@link Encoding encoding type} of the
	 * variable
	 * @param description a natural language description of the variable
	 * @return a variable object with those properties
	 */
	public Variable addVariable(Signature signature, String encoding, String description) {
		Variable variable = new Variable(variables.size(), signature, encoding, description);
		variables.add(variable);
		return variable;
	}
	
	/**
	 * Defines a new {@link Action action}.
	 * 
	 * @param signature the unique signature of the action
	 * @param consenting the characters in the story world who need a reason to
	 * take the action
	 * @param description a natural language description of the action
	 * @return an action object with those properties
	 */
	public Action addAction(Signature signature, Entity[] consenting, String description) {
		Action action = new Action(actions.size(), signature, consenting, description, "");
		actions.add(action);
		return action;
	}
	
	/**
	 * Defines a new {@link Ending ending}.
	 * 
	 * @param signature the unique signature of the ending
	 * @param description a natural language description of the ending
	 * @return an ending object with those properties
	 */
	public Ending addEnding(Signature signature, String description) {
		Ending ending = new Ending(endings.size(), signature, description, "");
		endings.add(ending);
		return ending;
	}
}