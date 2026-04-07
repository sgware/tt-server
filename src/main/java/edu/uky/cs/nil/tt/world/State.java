package edu.uky.cs.nil.tt.world;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A state is an {@link Assignment assignment} of {@link Value values} to each
 * {@link Variable variable} in a {@link World story world}.
 * <p>
 * State objects are unmodifiable. Calls to {@link #set(Assignment)} create and
 * return a new state and do not modify the object on which they are called.
 * 
 * @author Stephen G. Ware
 */
public class State implements Described, Encoded {
	
	/** An array of assignments for each variable in the story world */
	private final Assignment[] assignments;
	
	/** A list of assignments for each variable in the story world */
	private transient List<Assignment> assignmentList;
	
	/** A map of variables to their assigned values */
	private transient Map<Variable, Value> assignmentMap;
	
	/** A natural language description of the state */
	private final String description;
	
	/** The {@link Encoded encoding} of the state */
	private final String code;
	
	/**
	 * Constructs a state from an array of variable assignments.
	 * 
	 * @param assignments an assignment of a value to each variable in a story
	 * world
	 * @param description a natural language description of the state
	 */
	private State(Assignment[] assignments, String description) {
		this.assignments = assignments;
		this.description = description;
		String code = "";
		for(Assignment assignment : assignments)
			code += assignment.getCode();
		this.code = code;
	}
	
	/**
	 * Constructs a state that assigns default values to every variable in a
	 * story world.
	 * 
	 * @param world the world whose variables will be assigned default values
	 */
	public State(World world) {
		this(getDefaultValues(world.getVariables()), null);
	}
	
	private static final Assignment[] getDefaultValues(List<Variable> variables) {
		Assignment[] assignments = new Assignment[variables.size()];
		for(int i = 0; i < variables.size(); i++)
			assignments[i] = new Assignment(variables.get(i), variables.get(i).decode(null));
		return assignments;
	}
	
	@Override
	public boolean equals(Object other) {
		if(other instanceof State otherState)
			return Arrays.equals(this.assignments, otherState.assignments);
		else
			return false;
	}
	
	@Override
	public int hashCode() {
		return Arrays.hashCode(assignments);
	}
	
	@Override
	public String toString() {
		return getAssignments().toString();
	}
	
	@Override
	public String getDescription() {
		return description;
	}
	
	/**
	 * Returns a new state that is the same as this state, except this its
	 * {@link Described description} is the given value.
	 * 
	 * @param description the description the new state should have
	 * @return a state identical to this state, except with the given
	 * description
	 */
	protected State setDescription(String description) {
		return new State(assignments, description);
	}
	
	@Override
	public String getCode() {
		return code;
	}
	
	/**
	 * Returns an unmodifiable list of assignments of a value to each variable
	 * in the state.
	 * 
	 * @return an unmodifiable list of variable assignments
	 */
	public List<Assignment> getAssignments() {
		if(assignmentList == null) {
			List<Assignment> list = new ArrayList<>();
			for(Assignment assignment : assignments)
				list.add(assignment);
			this.assignmentList = Collections.unmodifiableList(list);
		}
		return assignmentList;
	}
	
	/**
	 * Returns the value assigned to the given variable, or null if the variable
	 * does not exist in this state or is assigned a null value.
	 * 
	 * @param variable a variable whose assigned value is desired
	 * @return the value assigned to the given variable in this state
	 */
	public Value get(Variable variable) {
		if(assignmentMap == null) {
			assignmentMap = new HashMap<>();
			for(Assignment assignment : assignments)
				if(assignment != null)
					assignmentMap.put(assignment.variable, assignment.value);
		}
		return assignmentMap.get(variable);
	}
	
	/**
	 * Returns a new state object that is identical to this one, except with the
	 * variable in the given assignment set to the given value.
	 * 
	 * @param assignment an assignment the new state should contain
	 * @return a new state the reflects the given assignment
	 */
	public State set(Assignment assignment) {
		Assignment[] assignments = this.assignments.clone();
		assignments[assignment.variable.id] = assignment;
		return new State(assignments, getDescription());
	}
}