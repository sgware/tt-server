package edu.uky.cs.nil.tt.world;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import edu.uky.cs.nil.tt.Role;
import edu.uky.cs.nil.tt.Utilities;

/**
 * A logical {@link WorldModel world model} implements the internal logic of a
 * story world using {@link Proposition logical propositions} and describes its
 * elements using a rule-based {@link Describer describer}.
 * 
 * @author Stephen G. Ware
 */
public class LogicalWorld extends WorldModel {
	
	/** Propositions that define when an entity can be seen by player */
	private final Proposition[] entityVisibility;
	
	/**
	 * The initial value of each of the story world's variables, where the value
	 * at index n corresponds to the value of variable n
	 */
	private final Value[] initial;
	
	/**
	 * Propositions that define when the value of a variable can be seen by the
	 * player (used to update the player's knowledge if they do not see the
	 * action which caused the change)
	 */
	private final Proposition[] variableVisibility;
	
	/** Propositions that define when an action is possible */
	private final Proposition[] actionPreconditions;
	
	/**
	 * Effects which describe how actions modify the world state when the occur,
	 * where the effects at index n correspond to action n
	 */
	private final Effect[][] actionEffects;
	
	/** Propositions that define when an action can be seen by the player */
	private final Proposition[] actionVisibility;
	
	/** Propositions that define when an ending should occur */
	private final Proposition[] endingConditions;
	
	/** The describe used to describe elements of this story world */
	private Describer describer = new Describer();
	
	/**
	 * Constructs a logical world model with the given assets.
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
	public LogicalWorld(
		String name,
		Collection<Entity> entities,
		Collection<Variable> variables,
		Collection<Action> actions,
		Collection<Ending> endings
	) {
		super(name, entities, variables, actions, endings);
		this.entityVisibility = new Proposition[getEntities().size()];
		this.initial = new Value[getVariables().size()];
		this.variableVisibility = new Proposition[getVariables().size()];
		this.actionPreconditions = new Proposition[getActions().size()];
		this.actionEffects = new Effect[getActions().size()][];
		this.actionVisibility = new Proposition[getActions().size()];
		this.endingConditions = new Proposition[getEndings().size()];
	}
	
	/**
	 * Constructs a logical world model from the assets defined in an asset
	 * builder.
	 * 
	 * @param builder the asset builder that defines the assets to be used in
	 * this story world
	 * @throws IllegalArgumentException if the provided assets do not meet the
	 * required format, such as two assets of the same type having the same name
	 */
	public LogicalWorld(AssetBuilder builder) {
		this(builder.name, builder.entities, builder.variables, builder.actions, builder.endings);
	}
	
	private static final <T> T get(T object, T defaultValue) {
		if(object == null)
			return defaultValue;
		else
			return object;
	}
	
	/**
	 * Substitutes all elements of a logical object with the corresponding
	 * objects from this story world. This method is used when a logical
	 * object may have been built using elements from other story worlds; it
	 * replaces those elements with corresponding elements from this world.
	 * 
	 * @param <L> the type of logical object
	 * @param logical a logical object
	 * @return the same type of logical object, but with any elements not from
	 * this story world replaced with elements from this story world
	 */
	@SuppressWarnings("unchecked")
	private final <L extends Logical> L substitute(L logical) {
		if(logical == null)
			return null;
		else {
			Function<Object, Object> substitution = new Function<>() {
				@Override
				public Object apply(Object original) {
					if(original instanceof Asset asset)
						return validate(asset);
					else if(original instanceof Logical logical)
						return logical.substitute(this);
					else
						return original;
				}
			};
			return (L) substitution.apply(logical);
		}
	}
	
	/**
	 * Checks whether the given role can see the given entity in the given
	 * state. The {@link Role#GAME_MASTER game master} can always see all
	 * entities. Whether the {@link Role#PLAYER player} can see an entity
	 * depends on {@link #getVisibility(Entity) the entity's visibility}.
	 * 
	 * @param role the role whose ability to see is being checked
	 * @param entity the entity whose viability is being checked
	 * @param state the state in which the check will occur
	 * @return true if the role can see the entity in the given state, false
	 * otherwise
	 */
	public boolean isVisible(Role role, Entity entity, State state) {
		return role == Role.GAME_MASTER || getVisibility(entity).test(state);
	}
	
	/**
	 * Returns a logical proposition that defines when the {@link Role#PLAYER
	 * player} can see an entity. If no proposition has been {@link
	 * #setVisibility(Entity, Proposition) set}, this method returns the
	 * constant {@link Proposition#FALSE false}.
	 * 
	 * @param entity an entity from this story world
	 * @return a proposition that will evaluate to true in states where the
	 * player can see the entity and false in states where the player cannot see
	 * the entity
	 */
	public Proposition getVisibility(Entity entity) {
		return get(entityVisibility[validate(entity).id], Proposition.FALSE);
	}
	
	/**
	 * Sets the logical proposition that defines when the {@link Role#PLAYER
	 * player} can see an entity.
	 * 
	 * @param entity an entity from this story world
	 * @param visibility a logical proposition that evaluates to true in states
	 * where the player can see the entity and false in states where the player
	 * cannot see the entity
	 */
	public void setVisibility(Entity entity, Proposition visibility) {
		entityVisibility[validate(entity).id] = substitute(visibility);
	}
	
	/**
	 * Returns the value a given variable has when the story first begins.
	 * 
	 * @param variable the variable whose value is desired
	 * @return the value that variable has when the story begins
	 */
	public Value getInitialValue(Variable variable) {
		return variable.decode(initial[validate(variable).id]);
	}
	
	/**
	 * Sets the value a given variable has when the story first begins.
	 * 
	 * @param variable the variable whose value will be set
	 * @param value the value that variable will have when the story begins
	 */
	public void setInitialValue(Variable variable, Value value) {
		initial[validate(variable).id] = substitute(value);
	}
	
	/**
	 * Checks whether the given role can see the value of the given variable in
	 * the given state. The {@link Role#GAME_MASTER game master} can always see
	 * all variable values. Whether the {@link Role#PLAYER player} can see a
	 * variable's value depends on {@link #getVisibility(Variable) the
	 * variable's visibility}.
	 * 
	 * @param role the role whose ability to see is being checked
	 * @param variable the variable whose visibility is being checked
	 * @param state the state in which the check will occur
	 * @return true if the role can see the variable's value in the given state,
	 * false otherwise
	 */
	public boolean isVisible(Role role, Variable variable, State state) {
		return role == Role.GAME_MASTER || getVisibility(variable).test(state);
	}
	
	/**
	 * Returns a logical proposition that defines when the {@link Role#PLAYER
	 * player} can see a varaiable's value. If no proposition has been {@link
	 * #setVisibility(Variable, Proposition) set}, this method returns the
	 * constant {@link Proposition#TRUE true}.
	 * <p>
	 * Note that if a player {@link #getVisibility(Action) sees an action} that
	 * updates the value of a variable, they will observe the update, even if
	 * the variable is not visible. Variable visibility is intended for
	 * situations where a variable changed because of an action that the player
	 * did not observe, but the player should (either immediately or later) see
	 * this change.
	 * <p>
	 * For example, suppose there are three rooms: A, B, and C. The player is in
	 * room A and a second non-player character is in room C. Suppose the
	 * location of the second character is visible to the player when the player
	 * and that second character are in the same room together. Now suppose the
	 * second character walks to room B. The player does not see this action
	 * happen, so they do not know about the change to that character's
	 * location, and the player's status continues to report that the second
	 * character is in room C. Now suppose the player walks into room B. The
	 * player should notice the other character. This is accomplished via
	 * variable visibility. The player's status will now report that the second
	 * character is in room B, because the variable for that character's
	 * location is visible.
	 * <p>
	 * When a player cannot see a variable's value, the player's {@link Status
	 * status} should report the last known value of that variable.
	 * 
	 * @param variable a variable from this story world
	 * @return a proposition that will evaluate to true in states where the
	 * player can see the variable's value and false in states where the player
	 * cannot see the variable's value
	 */
	public Proposition getVisibility(Variable variable) {
		return get(variableVisibility[validate(variable).id], Proposition.TRUE);
	}
	
	/**
	 * Sets the logical proposition that defines when the {@link Role#PLAYER
	 * player} can see the value of a variable.
	 * 
	 * @param variable a variable from this story world
	 * @param visibility a logical proposition that evaluates to true in states
	 * where the player can see the variable's value and false in states where
	 * the player cannot see the variable's value
	 */
	public void setVisibility(Variable variable, Proposition visibility) {
		variableVisibility[validate(variable).id] = substitute(visibility);
	}
	
	/**
	 * Checks whether the given action is available is available to the given
	 * role in the given state. An action is available to a role when {@link
	 * #getPrecondition(Action) its precondition} is satisfied in the current
	 * state and the role {@link Action#consents(Role) consents} to the action
	 * (i.e. has to approve taking the action).
	 * 
	 * @param role the role whose consent is being checked
	 * @param action the action whose precondition is being checked
	 * @param state the state in which the check will occur
	 * @return true if the action is possible and the role consents to it, false
	 * otherwise
	 */
	public boolean isAvailable(Role role, Action action, State state) {
		return action.consents(role) && getPrecondition(action).test(state);
	}
	
	/**
	 * Returns a logical proposition that defines when an action can be taken.
	 * If no proposition has been {@link #setPrecondition(Action, Proposition)
	 * set}, this method returns the constant {@link Proposition#TRUE true}.
	 * 
	 * @param action an action from this story world
	 * @return a proposition that will evaluate to true in states where the
	 * action can be taken and false in states where the action cannot be taken
	 */
	public Proposition getPrecondition(Action action) {
		return get(actionPreconditions[validate(action).id], Proposition.TRUE);
	}
	
	/**
	 * Sets the logical proposition that defines when an action can be taken.
	 * 
	 * @param action an action from this story world
	 * @param precondition a logical proposition that evaluates to true in
	 * states where the action can be taken and false in states where the action
	 * cannot be taken
	 */
	public void setPrecondition(Action action, Proposition precondition) {
		actionPreconditions[validate(action).id] = substitute(precondition);
	}
	
	/**
	 * Returns an array of effects which express how an action modifies the
	 * state of a story world when it happens. If no effects have been {@link
	 * #setEffects(Action, Effect...) set}, this method returns an empty array.
	 * 
	 * @param action an action from this story world
	 * @return an (possibly empty) array of effects that express how the action
	 * modifies a state
	 */
	public Effect[] getEffects(Action action) {
		Effect[] effects = actionEffects[validate(action).id];
		if(effects == null)
			effects = new Effect[0];
		else
			effects = effects.clone();
		return effects;
	}
	
	/**
	 * Sets the arrays of effects which express how an action modifies the state
	 * of a story world when it happens.
	 * 
	 * @param action an action from this story world
	 * @param effects the effects which express how the action modifies the
	 * state
	 */
	public void setEffects(Action action, Effect...effects) {
		if(effects != null) {
			Utilities.requireAllNonNull(effects, "effect");
			effects = effects.clone();
			for(int i = 0; i < effects.length; i++)
				effects[i] = substitute(effects[i]);
		}
		actionEffects[validate(action).id] = effects;
	}
	
	/**
	 * Checks whether the given action would be seen by the given role if it
	 * happened in the given state. The {@link Role#GAME_MASTER game master}
	 * can always see all actions. Whether the {@link Role#PLAYER player} can
	 * see a an action occur depends on {@link #getVisibility(Action) the
	 * action's visibility}.
	 * 
	 * @param role the role whose ability to see is being checked
	 * @param action the action whose visibility is being checked
	 * @param state the state in which the check will occur
	 * @return true if the role would see the action occurs in the given state,
	 * false otherwise
	 */
	public boolean isVisible(Role role, Action action, State state) {
		return role == Role.GAME_MASTER || getVisibility(action).test(state);
	}
	
	/**
	 * Returns a logical proposition that defines when the {@link Role#PLAYER
	 * player} would see an action happen. If no proposition has been {@link
	 * #setVisibility(Action, Proposition) set}, this method returns the
	 * constant {@link Proposition#TRUE true}.
	 * <p>
	 * When the player sees an action, it means the action will appear in the
	 * {@link Status#getHistory() history} of the story world and the changes
	 * to the state caused by the action's {@link #getEffects(Action) effects}
	 * are reflected in the state. When the player does not see an action, their
	 * {@link Status status} will appear as if the action did not happen,
	 * meaning it will not appear in the history and the variables changed by
	 * the action's effects will not be reflected in the current state.
	 * 
	 * @param action an action from this story world
	 * @return a proposition that will evaluate to true in states where the
	 * player would see the action occur and false in states where the player
	 * would not see the action occur
	 */
	public Proposition getVisibility(Action action) {
		return get(actionVisibility[validate(action).id], Proposition.TRUE);
	}
	
	/**
	 * Sets the logical proposition that defines when the {@link Role#PLAYER
	 * player} can see an action happen.
	 * 
	 * @param action an action from this story world
	 * @param visibility a logical proposition that evaluates to true in states
	 * where the player would see the action occur and false in states where the
	 * player would not see the action occur
	 */
	public void setVisibility(Action action, Proposition visibility) {
		actionVisibility[validate(action).id] = visibility;
	}
	
	/**
	 * Returns a logical proposition that defines when an ending should occur.
	 * If no proposition has been {@link #setCondition(Ending, Proposition)
	 * set}, this method returns the constant {@link Proposition#FALSE false}.
	 * These conditions are checked an action changes the story state, in order
	 * by {@link #getEndings() ending}. If any conditions evaluates to true,
	 * the story immediately ends with the corresponding ending.
	 * 
	 * @param ending an ending from this story world
	 * @return a proposition that will evaluate to true in states where the
	 * ending occurs and false in states where the ending does not occur
	 */
	public Proposition getCondition(Ending ending) {
		return get(endingConditions[validate(ending).id], Proposition.FALSE);
	}
	
	/**
	 * Sets the logical proposition that defines when an ending should occur.
	 * 
	 * @param ending an ending from this story world
	 * @param condition a logical proposition that evaluates to true in states
	 * where the ending should occur and false in states where the ending should
	 * not occur
	 */
	public void setCondition(Ending ending, Proposition condition) {
		endingConditions[validate(ending).id] = substitute(condition);
	}
	
	/**
	 * Returns a phrase or sentence to describe an object to the given role
	 * using this world models' {@link #getDescriber() describer}.
	 * <p>
	 * If the object given is an {@link Assignment} or {@link Turn}, the
	 * description will be a {@link Describer#getSentence(Object) sentence}.
	 * For all other objects, the description will be a {@link
	 * Describer#getPhrase(Object) phrase}.
	 * 
	 * @param object the object to describer
	 * @param to the role to whom the object will be described
	 * @return a description of the object as a phrase or sentence
	 */
	public String getDescription(Object object, Role to) {
		if(object instanceof Assignment || object instanceof Turn)
			return getDescriber().getSentence(object, to);
		else
			return getDescriber().getPhrase(object, to);
	}
	
	/**
	 * Returns the describer this world model uses to describe its objects.
	 * 
	 * @return the describer used by this world model
	 */
	public Describer getDescriber() {
		return describer;
	}
	
	/**
	 * Sets the describer used by this world model to describe its objects.
	 * 
	 * @param describer the new describer this world model should use to
	 * describe its objects
	 */
	public void setDescriber(Describer describer) {
		if(describer == null)
			describer = new Describer();
		this.describer = describer;
	}

	@Override
	public Status start(Role role) {
		State initial = new State(this);
		String description = "";
		for(Variable variable : getVariables()) {
			Assignment assignment = new Assignment(variable, getInitialValue(variable));
			assignment = assignment.setDescription(getDescription(assignment, role));
			initial = initial.set(assignment);
			description += (description.isEmpty() ? "" : " ") + assignment.getDescription();
		}
		initial = initial.setDescription(description);
		return getStatus(role, new Turn[0], initial, initial);
	}

	@Override
	public Status transition(Status status, State actual, Turn turn) {
		List<Turn> history = new ArrayList<>();
		history.addAll(status.getHistory());
		// Represents the observed state, which is the same as the true state
		// if this is the GM, but is only what is known if this is the player.
		State observed = status.getState();
		// If this role observes the turn...
		if(observes(status.role, turn, actual)) {
			// Add it to the history.
			turn = turn.setDescription(getDescription(turn, status.role));
			history.add(turn);
			// If an action succeeds...
			if(turn.type == Turn.Type.SUCCEED) {
				// Apply the action's effects to the actual and observed states.
				for(Effect effect : getEffects(turn.action)) {
					actual = effect.apply(actual);
					observed = effect.apply(observed);
				}
				// Describe all assignments in the observed state.
				for(Assignment assignment : observed.getAssignments()) {
					assignment = assignment.setDescription(getDescription(assignment, status.role));
					observed = observed.set(assignment);
				}
				// Update the observed state with any information that is
				// visible but not already covered by the action's effects.
				String description = turn.getDescription();
				for(Assignment assignment : actual.getAssignments()) {
					if(isVisible(status.role, assignment.variable, actual) && !assignment.value.equals(observed.get(assignment.variable))) {
						observed = observed.set(assignment);
						description += (description.isEmpty() ? "" : " ") + getDescription(assignment, status.role);
					}
				}
				observed = observed.setDescription(description);
			}
		}
		return getStatus(status.role, history.toArray(new Turn[history.size()]), actual, observed);
	}
	
	private final boolean observes(Role role, Turn turn, State state) {
		return turn.type == Turn.Type.PROPOSE || turn.type == Turn.Type.PASS || turn.action.consents(Role.PLAYER) || isVisible(role, turn.action, state);
	}
	
	/**
	 * Calculates the ending (if any), entity descriptions, and turns available
	 * to include in a status.
	 * 
	 * @param role the role for the status
	 * @param history the history of turns so far in the session
	 * @param actual the actual current state
	 * @param observed the perceived current state
	 * @return the story world status implied by those inputs
	 */
	protected Status getStatus(Role role, Turn[] history, State actual, State observed) {
		Ending ending = getEnding(role, history, actual);
		Entity[] descriptions = getEntityDescriptions(role, history, observed);
		Turn[] choices = getChoices(role, history, actual, ending);
		return new Status(getName(), role, history, observed, ending, descriptions, choices);
	}
	
	/**
	 * Determines whether the story has ended and, if so, returns the ending.
	 * 
	 * @param role the role to whom the ending (if any) will be described
	 * @param history the history of turns so far in the session
	 * @param state the current state of the story world
	 * @return an ending, if one has been reached, or null
	 */
	protected Ending getEnding(Role role, Turn[] history, State state) {
		for(Ending ending : getEndings()) {
			if(getCondition(ending).test(state)) {
				ending = ending.setDescription(getDescription(ending, role));
				return ending;
			}
		}
		return null;
	}
	
	/**
	 * Returns an array of entities {@link #isVisible(Role, Entity, State)
	 * visible} to a given role which have {@link
	 * #getEntityDescription(Role, Turn[], State, Entity) useful descriptions}.
	 * 
	 * @param role the role for whom visibility will be checked and to whom the
	 * entities will be described
	 * @param history the history of turns so far in the session
	 * @param state the current state as perceived by the given role
	 * @return an array of entities that are currently visible to that role with
	 * helpful descriptions
	 */
	protected Entity[] getEntityDescriptions(Role role, Turn[] history, State state) {
		List<Entity> descriptions = new ArrayList<>();
		for(Entity entity : getEntities()) {
			if(isVisible(role, entity, state)) {
				entity = entity.setDescription(getEntityDescription(role, history, state, entity));
				descriptions.add(entity);
			}
		}
		return descriptions.toArray(new Entity[descriptions.size()]);
	}
	
	/**
	 * Returns a longer, more helpful description of an entity based on the
	 * current state of a story world.
	 * 
	 * @param role the role to whom the entity is being described
	 * @param history the history of turns so far in the session
	 * @param state the current state of the story world
	 * @param entity the entity being described
	 * @return a description of the entity that includes relevant details from
	 * the current state
	 */
	protected String getEntityDescription(Role role, Turn[] history, State state, Entity entity) {
		String description = getDescription(entity, role) + ":";
		for(Assignment assignment : state.getAssignments())
			if(assignment.variable.signature.getArguments().contains(entity) || assignment.value.equals(entity))
				description += " " + assignment.getDescription();
		return description;
	}
	
	/**
	 * Returns an array of choices available to a given role based on the
	 * current state of a story world, or null if no choices are available.
	 * 
	 * @param role the role to whom the choices are available and to whom the
	 * choices will be described
	 * @param history the history of turns so far in the session
	 * @param state the current actual state of the story world
	 * @param ending the ending of the story, if any
	 * @return an array of choices available to the given role, or null if no
	 * choices are available
	 */
	protected Turn[] getChoices(Role role, Turn[] history, State state, Ending ending) {
		// If it is not this role's turn, or if the game is over, there are no choices.
		if(role != current(history) || ending != null)
			return null;
		List<Turn> choices = new ArrayList<>();
		// If the last turn was a proposal, offer responses.
		Turn last = history.length > 0 ? history[history.length - 1] : null;
		if(last != null && last.type == Turn.Type.PROPOSE) {
			choices.add(new Turn(role, Turn.Type.SUCCEED, last.action));
			choices.add(new Turn(role, Turn.Type.FAIL, last.action));
		}
		// Otherwise...
		else {
			// Offer all available actions.
			for(Action action : getActions()) {
				if(isAvailable(role, action, state)) {
					// If the action requires only the GM's consent, offer succeed and fail.
					if(role == Role.GAME_MASTER && !action.consents(Role.PLAYER)) {
						choices.add(new Turn(role, Turn.Type.SUCCEED, action));
						choices.add(new Turn(role, Turn.Type.FAIL, action));
					}
					// If the action requires player consent, offer propose.
					else
						choices.add(new Turn(role, Turn.Type.PROPOSE, action));
				}
			}
			// Offer pass.
			choices.add(new Turn(role, this));
		}
		// Describe choices.
		Turn[] array = choices.toArray(new Turn[choices.size()]);
		for(int i = 0; i < array.length; i++)
			array[i] = array[i].setDescription(getDescription(array[i], role));
		return array;
	}
	
	/**
	 * Determines whose turn it is based on the history of a session.
	 * 
	 * @param history the history of turns so far in the session
	 * @return the role whose turn it is to act
	 */
	protected static final Role current(Turn[] history) {
		Role current = Role.GAME_MASTER;
		for(Turn turn : history) {
			if(turn.type == Turn.Type.PROPOSE || turn.type == Turn.Type.PASS)
				current = current.getPartner();
			else if(turn.role == Role.PLAYER)
				current = Role.GAME_MASTER;
		}
		return current;
	}
	
	/**
	 * Returns a long string describing every element of a story world model for
	 * use in debugging.
	 * 
	 * @return a long string describing every element of this story world model
	 */
	public String describe() {
		StringWriter string = new StringWriter();
		string.append("World " + getName() + ":");
		// Entities
		string.append("\n  " + getEntities().size() + " entities:");
		for(Entity entity : getEntities()) {
			string.append("\n    " + entity.getID() + " " + entity + ": \"" + entity.getDescription() + "\"");
			string.append("\n        visible to " + Role.PLAYER + ": " + getVisibility(entity));
			for(Role to : Role.values())
				string.append("\n        describe to " + to + ": \"" + getDescription(entity, to) + "\"");
		}
		// Values
		List<Value> values = new ArrayList<>();
		values.add(Constant.NULL);
		values.add(Constant.TRUE);
		values.add(Constant.FALSE);
		values.add(new Constant(0));
		values.add(new Constant(1));
		values.add(new Constant(2));
		for(Entity entity : getEntities())
			values.add(entity);
		// Variables
		string.append("\n  " + getVariables().size() + " variables:");
		for(Variable variable : getVariables()) {
			string.append("\n    " + variable.getID() + " " + variable + ": \"" + variable.getDescription() + "\"");
			string.append("\n        initial value: " + getInitialValue(variable));
			string.append("\n        visible to " + Role.PLAYER + ": " + getVisibility(variable));
			for(Role to : Role.values())
				string.append("\n        describe to " + to + ": \"" + getDescription(variable, to) + "\"");
			for(Value value : values) {
				if(value == variable.decode(value)) {
					Assignment assignment = new Assignment(variable, value);
					for(Role to : Role.values())
						string.append("\n        describe " + assignment + " to " + to + ": \"" + getDescription(assignment, to) + "\"");
				}
			}
		}
		// Actions
		string.append("\n  " + getActions().size() + " actions:");
		for(Action action : getActions()) {
			string.append("\n    " + action.getID() + " " + action + ": \"" + action.getDescription() + "\"");
			string.append("\n        precondition: " + getPrecondition(action));
			string.append("\n        effect: " + Effect.toString(getEffects(action)));
			string.append("\n        visible to " + Role.PLAYER + ": " + getVisibility(action));
			for(Role role : Role.values()) {
				for(Turn.Type type : new Turn.Type[] { Turn.Type.PROPOSE, Turn.Type.SUCCEED, Turn.Type.FAIL }) {
					// A role cannot propose an action they do not consent to.
					if(type == Turn.Type.PROPOSE && !action.consents(role))
						continue;
					// The GM only needs to propose actions that require player consent.
					else if(role == Role.GAME_MASTER && type == Turn.Type.PROPOSE && !action.consents(Role.PLAYER))
						continue;
					// The player can only succeed or fail actions proposed by the GM and consented to by both roles.
					else if(role == Role.PLAYER && (type == Turn.Type.SUCCEED || type == Turn.Type.FAIL) && !(action.consents(Role.GAME_MASTER) && action.consents(Role.PLAYER)))
						continue;
					Turn turn = new Turn(role, type, action);
					for(Role to : Role.values())
						string.append("\n        describe " + role + " " + type + " " + action + " to " + to + ": \"" + getDescription(turn, to) + "\"");
				}
			}
		}
		string.append("\n    " + Turn.Type.PASS);
		for(Role role : Role.values()) {
			Turn turn = new Turn(role, this);
			for(Role to : Role.values())
				string.append("\n        describe " + role + " " + Turn.Type.PASS + " to " + to + ": \"" + getDescription(turn, to) + "\"");
		}
		// Endings
		string.append("\n  " + getEndings().size() + " endings:");
		for(Ending ending : getEndings()) {
			string.append("\n    " + ending.getID() + " " + ending + ": \"" + ending.getDescription() + "\"");
			string.append("\n        condition: " + getCondition(ending));
			for(Role to : Role.values())
				string.append("\n        describe " + ending + " to " + to + ": \"" + getDescription(ending, to) + "\"");
		}
		return string.toString();
	}
	
	/**
	 * Reads a serialized logical world model from some input source.
	 * 
	 * @param reader the input source
	 * @return a logical world model
	 * @throws IOException if a problem occurs when reading from the input
	 * source
	 * @throws com.google.gson.JsonIOException if there was a problem reading
	 * JSON data from the Reader
	 * @throws com.google.gson.JsonSyntaxException if the JSON read from the
	 * source cannot be parsed as a logical world model
	 */
	public static LogicalWorld read(Reader reader) throws IOException {
		GsonBuilder builder = new GsonBuilder();
		Expression.configure(builder);
		Gson gson = builder.setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES).create();
		return gson.fromJson(reader, LogicalWorld.class);
	}
	
	/**
	 * Reads a serialized logical world model from a file.
	 * 
	 * @param file the file to which a logical world model has been serialized
	 * @return a logical world model
	 * @throws IOException if a problem occurs when reading from the file
	 * @throws com.google.gson.JsonIOException if there was a problem reading
	 * JSON data from the Reader
	 * @throws com.google.gson.JsonSyntaxException if the JSON read from the
	 * source cannot be parsed as a logical world model
	 */
	public static LogicalWorld read(File file) throws IOException {
		try(BufferedReader in = new BufferedReader(new FileReader(file))) {
			return read(in);
		}
	}
	
	/**
	 * Serializes this logical world model to an output stream.
	 * 
	 * @param writer the output to which the logical world model will be
	 * serialized
	 * @throws IOException if a problem occurs when writing to the output source
	 * @throws com.google.gson.JsonIOException if a problem occurs when writing
	 * JSON data to the output source
	 */
	public void write(Writer writer) throws IOException {
		GsonBuilder builder = new GsonBuilder();
		Expression.configure(builder);
		Gson gson = builder.setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES).setPrettyPrinting().create();
		gson.toJson(this, writer);
	}
	
	/**
	 * Serializes this logical world model to a file.
	 * 
	 * @param file the file to which the logical world model will be serialized
	 * @throws IOException if a problem occurs when writing to the output source
	 * @throws com.google.gson.JsonIOException if a problem occurs when writing
	 * JSON data to the output source
	 */
	public void write(File file) throws IOException {
		try(BufferedWriter out = new BufferedWriter(new FileWriter(file))) {
			write(out);
		}
	}
}