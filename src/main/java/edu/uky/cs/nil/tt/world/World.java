package edu.uky.cs.nil.tt.world;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;

import edu.uky.cs.nil.tt.Named;
import edu.uky.cs.nil.tt.Utilities;

/**
 * A story world is a collection of {@link Asset assets} needed for two partners
 * in a {@link edu.uky.cs.nil.tt.Session session} to tell a collaborative story.
 * 
 * @author Stephen G. Ware
 */
public class World implements Named {
	
	/** The unique name of this story world */
	public final String name;
	
	/** This world's entities, as an array */
	private final Entity[] entities;
	
	/** This world's entities, as a list */
	private transient List<Entity> entityList;
	
	/** This world's entities, mapped by name */
	private transient Map<String, Entity> entityMap;
	
	/** This world's variables, as an array */
	private final Variable[] variables;
	
	/** This world's variables, as a list */
	private transient List<Variable> variableList;
	
	/** This world's variables, mapped by name */
	private transient Map<String, Variable> variableMap;
	
	/** This world's actions, as an array */
	private final Action[] actions;
	
	/** This world's actions, as a list */
	private transient List<Action> actionList;
	
	/** This world's actions, mapped by name */
	private transient Map<String, Action> actionMap;
	
	/** This world's endings, as an array */
	private final Ending[] endings;
	
	/** This world's endings, as a list */
	private transient List<Ending> endingList;
	
	/** This world's endings, mapped by name */
	private transient Map<String, Ending> endingMap;
	
	/**
	 * Constructs a story world from a unique name and sets of assets. During
	 * the construction process, the assets will be remade and given appropriate
	 * sequential unique ID numbers.
	 * 
	 * @param name a unique name for the story world
	 * @param entities a collection of objects that exist in the world
	 * @param variables a collection of variables whose values can change and
	 * whose current values reflect the current state of the world
	 * @param actions a collection of ways the world state can change
	 * @param endings a collection of ways a story in this world can end
	 * @throws IllegalArgumentException if the provided assets do not meet the
	 * required format, such as two assets of the same type having the same name
	 */
	public World(
		String name,
		Collection<Entity> entities,
		Collection<Variable> variables,
		Collection<Action> actions,
		Collection<Ending> endings
	) {
		// Name
		Utilities.requireName(name);
		this.name = name;
		// Entities
		this.entities = entities.toArray(new Entity[entities.size()]);
		Utilities.requireAllNonNull(this.entities, "entity");
		Utilities.requireGreaterThan(this.entities.length, 0, "number of entities");
		number(this.entities, "entity");
		Encoding entityEncoding = Encoding.get("entity" + Utilities.bits(this.entities.length + 1));
		toEach(this.entities, entity -> entity.setCode(entityEncoding.encode(entity)));
		Function<Object, Object> substitution = original -> {
			if(original instanceof Entity entity)
				return requireEntity(entity.getName());
			else
				return original;
		};
		// Variables
		this.variables = variables.toArray(new Variable[variables.size()]);
		Utilities.requireAllNonNull(this.variables, "variable");
		Utilities.requireGreaterThan(this.variables.length, 0, "number of variables");
		number(this.variables, "variable");
		toEach(this.variables, variable -> variable.substitute(substitution));
		toEach(this.variables, variable -> {
			if(variable.encoding.startsWith("entity"))
				return variable.setEncoding(entityEncoding.getName());
			else
				return variable;
		});
		// Actions
		this.actions = actions.toArray(new Action[actions.size()]);
		Utilities.requireGreaterThan(this.actions.length, 0, "number of actions");
		number(this.actions, "action");
		toEach(this.actions, action -> action.substitute(substitution));
		toEach(this.actions, action -> {
			LinkedHashSet<Entity> consenting = new LinkedHashSet<>();
			for(Entity entity : action.getConsenting())
				consenting.add(getEntity(entity.getName()));
			return action.setConsenting(consenting);
		});
		Encoding actionEncoding = Encoding.get("action" + Utilities.bits(this.actions.length + 1));
		toEach(this.actions, action -> action.setCode(actionEncoding.encode(action)));
		// Endings
		this.endings = endings.toArray(new Ending[endings.size()]);
		Utilities.requireAllNonNull(this.endings, "ending");
		Utilities.requireGreaterThan(this.endings.length, 0, "number of endings");
		number(this.endings, "ending");
		toEach(this.endings, ending -> ending.substitute(substitution));
		Encoding endingEncoding = Encoding.get("ending" + Utilities.bits(this.endings.length + 1));
		toEach(this.endings, ending -> ending.setCode(endingEncoding.encode(ending)));
	}
	
	@SuppressWarnings("unchecked")
	private static final <A extends Asset> void number(A[] array, String description) {
		Set<String> names = new HashSet<>();
		for(int id = 0; id < array.length; id++) {
			if(!names.add(array[id].getName()))
				throw new IllegalArgumentException("The " + description + " \"" + array[id].getName() + "\" is already defined.");
			array[id] = (A) array[id].setID(id);
		}
	}
	
	private static final <T> void toEach(T[] array, Function<T, T> function) {
		for(int i = 0; i < array.length; i++)
			array[i] = function.apply(array[i]);
	}
	
	/**
	 * Constructs a story world from the assets defined in an asset builder.
	 * 
	 * @param builder the asset builder that defines the assets to be used in
	 * this story world
	 * @throws IllegalArgumentException if the provided assets do not meet the
	 * required format, such as two assets of the same type having the same name
	 */
	public World(AssetBuilder builder) {
		this(
			builder.name,
			builder.entities,
			builder.variables,
			builder.actions,
			builder.endings
		);
	}
	
	@Override
	public String toString() {
		String string = "[" + name + ":";
		string += " " + getEntities().size() + " entities";
		string += "; " + getVariables().size() + " variables";
		string += "; " + getActions().size() + " actions";
		string += "; " + getEndings().size() + " endings";
		return string + "]";
	}
	
	@Override
	public String getName() {
		return name;
	}
	
	/**
	 * Returns the entity the represents the {@link
	 * edu.uky.cs.nil.tt.Role#PLAYER player} character in this story world.
	 * 
	 * @return the player character entity
	 */
	public Entity getPlayer() {
		return getEntity(0);
	}
	
	/**
	 * Returns true if the given entity is defined in this story world.
	 * 
	 * @param entity an entity
	 * @return true if this entity is defined in this story world, false
	 * otherwise
	 */
	public boolean contains(Entity entity) {
		return entity != null && Objects.equals(getEntity(entity.getName()), entity);
	}
	
	/**
	 * If this story world contains an entity matching the name of the given
	 * entity, the entity from this story world world with that name is
	 * returned; otherwise, an exception is thrown.
	 * 
	 * @param entity an entity
	 * @return the entity from this story world with the same name as the given
	 * entity
	 * @throws IllegalArgumentException if this story world does not define an
	 * entity with that name
	 */
	public Entity validate(Entity entity) {
		if(contains(entity))
			return getEntity(entity.getName());
		else
			return require(null, "entity", entity == null ? null : entity.getName());
	}
	
	/**
	 * Returns an unmodifiable list of all entities defined in this story world
	 * ordered by the ID numbers.
	 * 
	 * @return a list of entities defined in this story world
	 */
	public List<Entity> getEntities() {
		if(entityList == null)
			entityList = List.of(entities);
		return entityList;
	}
	
	/**
	 * Returns the entity defined in this story world which has the given {@link
	 * Unique#getID() ID number}.
	 * 
	 * @param id the ID number of the desired entity
	 * @return the entity with that ID number
	 * @throws IndexOutOfBoundsException if there is no entity in this story
	 * world with that ID number
	 */
	public Entity getEntity(int id) {
		return getEntities().get(id);
	}
	
	/**
	 * Returns the entity defined in this story world which has the given {@link
	 * Named#getName() name}.
	 * 
	 * @param name the name of the desired entity
	 * @return the entity with that name, or null if no entity has that name
	 */
	public Entity getEntity(String name) {
		if(entityMap == null)
			entityMap = map(entities);
		return entityMap.get(name);
	}
	
	/**
	 * Returns the entity defined in this story world which has the given {@link
	 * Unique#getID() ID number} or throws an exception if it does not exist.
	 * 
	 * @param id the ID number of the desired entity
	 * @return the entity with that ID number
	 * @throws IllegalArgumentException if no entity exists with that ID number
	 */
	public Entity requireEntity(int id) {
		return require(getEntity(id), "entity", id);
	}
	
	/**
	 * Returns the entity defined in this story world which has the given {@link
	 * Named#getName() name} or throws an exception if it does not exist.
	 * 
	 * @param name the name of the desired entity
	 * @return the entity with that name
	 * @throws IllegalArgumentException if no entity exists with that name
	 */
	public Entity requireEntity(String name) {
		return require(getEntity(name), "entity", name);
	}
	
	/**
	 * Returns true if the given variable is defined in this story world.
	 * 
	 * @param variable a variable
	 * @return true if this variable is defined in this story world, false
	 * otherwise
	 */
	public boolean contains(Variable variable) {
		return variable != null && Objects.equals(getVariable(variable.getName()), variable);
	}
	
	/**
	 * If this story world contains a variable matching the name of the given
	 * variable, the variable from this story world world with that name is
	 * returned; otherwise, an exception is thrown.
	 * 
	 * @param variable a variable
	 * @return the variable from this story world with the same name as the
	 * given variable
	 * @throws IllegalArgumentException if this story world does not define a
	 * variable with that name
	 */
	public Variable validate(Variable variable) {
		if(contains(variable))
			return getVariable(variable.getName());
		else
			return require(null, "variable", variable == null ? null : variable.getName());
	}
	
	/**
	 * Returns an unmodifiable list of all variables defined in this story
	 * world ordered by the ID numbers.
	 * 
	 * @return a list of variables defined in this story world
	 */
	public List<Variable> getVariables() {
		if(variableList == null)
			variableList = List.of(variables);
		return variableList;
	}
	
	/**
	 * Returns the variable defined in this story world which has the given
	 * {@link Unique#getID() ID number}.
	 * 
	 * @param id the ID number of the desired variable
	 * @return the variable with that ID number
	 * @throws IndexOutOfBoundsException if there is no variables in this story
	 * world with that ID number
	 */
	public Variable getVariable(int id) {
		return getVariables().get(id);
	}
	
	/**
	 * Returns the variable defined in this story world which has the given
	 * {@link Named#getName() name}.
	 * 
	 * @param name the name of the desired variable
	 * @return the variable with that name, or null if no variable has that name
	 */
	public Variable getVariable(String name) {
		if(variableMap == null)
			variableMap = map(variables);
		return variableMap.get(name);
	}
	
	/**
	 * Returns the variable defined in this story world which has the given
	 * signature.
	 * 
	 * @param signature the signature of the desired variable
	 * @return the variable with that signature, or null if no variable has that
	 * signature
	 */
	public Variable getVariable(Signature signature) {
		return getVariable(signature.toString());
	}
	
	/**
	 * Returns the variable defined in this story world which has the given
	 * {@link Unique#getID() ID number} or throws an exception if it does not
	 * exist.
	 * 
	 * @param id the ID number of the desired variable
	 * @return the variable with that ID number
	 * @throws IllegalArgumentException if no variable exists with that ID
	 * number
	 */
	public Variable requireVariable(int id) {
		return require(getVariable(id), "variable", id);
	}
	
	/**
	 * Returns the variable defined in this story world which has the given
	 * {@link Named#getName() name} or throws an exception if it does not exist.
	 * 
	 * @param name the name of the desired variable
	 * @return the variable with that name
	 * @throws IllegalArgumentException if no variable exists with that name
	 */
	public Variable requireVariable(String name) {
		return require(getVariable(name), "variable", name);
	}
	
	/**
	 * Returns the variable defined in this story world which has the given
	 * signature or throws an exception if it does not exist.
	 * 
	 * @param signature the signature of the desired variable
	 * @return the variable with that signature
	 * @throws IllegalArgumentException if no variable exists with that
	 * signature
	 */
	public Variable requireVariable(Signature signature) {
		return requireVariable(signature.toString());
	}
	
	/**
	 * Returns true if the given action is defined in this story world.
	 * 
	 * @param action an action
	 * @return true if this action is defined in this story world, false
	 * otherwise
	 */
	public boolean contains(Action action) {
		return action != null && Objects.equals(getAction(action.getName()), action);
	}
	
	/**
	 * If this story world contains an action matching the name of the given
	 * action, the action from this story world world with that name is
	 * returned; otherwise, an exception is thrown.
	 * 
	 * @param action an action
	 * @return the action from this story world with the same name as the given
	 * action
	 * @throws IllegalArgumentException if this story world does not define an
	 * action with that name
	 */
	public Action validate(Action action) {
		if(contains(action))
			return getAction(action.getName());
		else
			return require(null, "action", action == null ? null : action.getName());
	}
	
	/**
	 * Returns an unmodifiable list of all actions defined in this story world
	 * ordered by the ID numbers.
	 * 
	 * @return a list of actions defined in this story world
	 */
	public List<Action> getActions() {
		if(actionList == null)
			actionList = List.of(actions);
		return actionList;
	}
	
	/**
	 * Returns the action defined in this story world which has the given {@link
	 * Unique#getID() ID number}.
	 * 
	 * @param id the ID number of the desired action
	 * @return the action with that ID number
	 * @throws IndexOutOfBoundsException if there is no action in this story
	 * world with that ID number
	 */
	public Action getAction(int id) {
		return getActions().get(id);
	}
	
	/**
	 * Returns the action defined in this story world which has the given {@link
	 * Named#getName() name}.
	 * 
	 * @param name the name of the desired action
	 * @return the action with that name, or null if no action has that name
	 */
	public Action getAction(String name) {
		if(actionMap == null)
			actionMap = map(actions);
		return actionMap.get(name);
	}
	
	/**
	 * Returns the action defined in this story world which has the given
	 * signature.
	 * 
	 * @param signature the signature of the desired action
	 * @return the action with that signature, or null if no action has that
	 * signature
	 */
	public Action getAction(Signature signature) {
		return getAction(signature.toString());
	}
		
	/**
	 * Returns the action defined in this story world which has the given {@link
	 * Unique#getID() ID number} or throws an exception if it does not exist.
	 * 
	 * @param id the ID number of the desired action
	 * @return the action with that ID number
	 * @throws IllegalArgumentException if no action exists with that ID number
	 */
	public Action requireAction(int id) {
		return require(getAction(id), "action", id);
	}
	
	/**
	 * Returns the action defined in this story world which has the given {@link
	 * Named#getName() name} or throws an exception if it does not exist.
	 * 
	 * @param name the name of the desired action
	 * @return the action with that name
	 * @throws IllegalArgumentException if no action exists with that name
	 */
	public Action requireAction(String name) {
		return require(getAction(name), "action", name);
	}
	
	/**
	 * Returns the action defined in this story world which has the given
	 * signature or throws an exception if it does not exist.
	 * 
	 * @param signature the signature of the desired action
	 * @return the action with that signature
	 * @throws IllegalArgumentException if no action exists with that signature
	 */
	public Action requireAction(Signature signature) {
		return requireAction(signature.toString());
	}
		
	/**
	 * Returns true if the given ending is defined in this story world.
	 * 
	 * @param ending an ending
	 * @return true if this ending is defined in this story world, false
	 * otherwise
	 */
	public boolean contains(Ending ending) {
		return ending != null && Objects.equals(getEnding(ending.getName()), ending);
	}
	
	/**
	 * If this story world contains an ending matching the name of the given
	 * ending, the ending from this story world world with that name is
	 * returned; otherwise, an exception is thrown.
	 * 
	 * @param ending an ending
	 * @return the ending from this story world with the same name as the given
	 * ending
	 * @throws IllegalArgumentException if this story world does not define an
	 * ending with that name
	 */
	public Ending validate(Ending ending) {
		if(contains(ending))
			return getEnding(ending.getName());
		else
			return require(null, "ending", ending == null ? null : ending.getName());
	}
	
	/**
	 * Returns an unmodifiable list of all endings defined in this story world
	 * ordered by the ID numbers.
	 * 
	 * @return a list of endings defined in this story world
	 */
	public List<Ending> getEndings() {
		if(endingList == null)
			endingList = List.of(endings);
		return endingList;
	}
	
	/**
	 * Returns the ending defined in this story world which has the given {@link
	 * Unique#getID() ID number}.
	 * 
	 * @param id the ID number of the desired ending
	 * @return the ending with that ID number
	 * @throws IndexOutOfBoundsException if there is no ending in this story
	 * world with that ID number
	 */
	public Ending getEnding(int id) {
		return getEndings().get(id);
	}
	
	/**
	 * Returns the ending defined in this story world which has the given {@link
	 * Named#getName() name}.
	 * 
	 * @param name the name of the desired ending
	 * @return the ending with that name, or null if no ending has that name
	 */
	public Ending getEnding(String name) {
		if(endingMap == null)
			endingMap = map(endings);
		return endingMap.get(name);
	}
	
	/**
	 * Returns the ending defined in this story world which has the given
	 * signature.
	 * 
	 * @param signature the signature of the desired ending
	 * @return the ending with that signature, or null if no ending has that
	 * signature
	 */
	public Ending getEnding(Signature signature) {
		return getEnding(signature.toString());
	}
		
	/**
	 * Returns the ending defined in this story world which has the given {@link
	 * Unique#getID() ID number} or throws an exception if it does not exist.
	 * 
	 * @param id the ID number of the desired ending
	 * @return the ending with that ID number
	 * @throws IllegalArgumentException if no ending exists with that ID number
	 */
	public Ending requireEnding(int id) {
		return require(getEnding(id), "ending", id);
	}
	
	/**
	 * Returns the ending defined in this story world which has the given {@link
	 * Named#getName() name} or throws an exception if it does not exist.
	 * 
	 * @param name the name of the desired ending
	 * @return the ending with that name
	 * @throws IllegalArgumentException if no ending exists with that name
	 */
	public Ending requireEnding(String name) {
		return require(getEnding(name), "ending", name);
	}
	
	/**
	 * Returns the ending defined in this story world which has the given
	 * signature or throws an exception if it does not exist.
	 * 
	 * @param signature the signature of the desired ending
	 * @return the ending with that signature
	 * @throws IllegalArgumentException if no ending exists with that signature
	 */
	public Ending requireEnding(Signature signature) {
		return requireEnding(signature.toString());
	}
	
	private static final <A extends Asset> Map<String, A> map(A[] assets) {
		Map<String, A> map = new HashMap<>();
		for(A asset : assets)
			map.put(asset.getName(), asset);
		return map;
	}
	
	private static final <T> T require(T object, String description, int id) {
		if(object == null)
			throw new IllegalArgumentException(Utilities.capitalize(description) + " " + id + " is not defined.");
		else
			return object;
	}
	
	private static final <T> T require(T object, String description, String name) {
		if(object == null)
			throw new IllegalArgumentException(Utilities.capitalize(description) + " \"" + name + "\" is not defined.");
		else
			return object;
	}
	
	/**
	 * If this story world contains an asset matching the name of the given
	 * asset, the asset from this story world world with that name is returned;
	 * otherwise, an exception is thrown.
	 * 
	 * @param <A> the type of asset being validated
	 * @param asset the asset
	 * @return the asset from this story world of the given type with the same
	 * name
	 * @throws IllegalArgumentException if this story world does not define an
	 * asset with that name
	 */
	@SuppressWarnings("unchecked")
	public <A extends Asset> A validate(A asset) {
		if(asset instanceof Entity entity)
			return (A) validate(entity);
		else if(asset instanceof Variable variable)
			return (A) validate(variable);
		else if(asset instanceof Action action)
			return (A) validate(action);
		else if(asset instanceof Ending ending)
			return (A) validate(ending);
		else
			throw new IllegalArgumentException("Asset \"" + asset + "\" is not defined.");
	}
}