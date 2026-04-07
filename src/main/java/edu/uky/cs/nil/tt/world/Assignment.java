package edu.uky.cs.nil.tt.world;

import java.util.Objects;
import java.util.function.Function;

import edu.uky.cs.nil.tt.Utilities;

/**
 * An assignment is a {@link Logical logical formula} that assets that some
 * {@link Variable variable} in a {@link World story world} has a {@link
 * Value value}.
 * 
 * @author Stephen G. Ware
 */
public class Assignment implements Described, Encoded, Logical {
	
	/** The variable that is being assigned a value */
	public final Variable variable;
	
	/** The value assigned to the variable */
	public final Value value;
	
	/** A natural language description of this assignments */
	private final String description;
	
	/** The {@link Encoded code} for this assignment */
	private final String code;
	
	/**
	 * Constructs a new assignment from a variable, value, and description.
	 * 
	 * @param variable the variable whose value is being assigned
	 * @param value the value being assigned to the variable
	 * @param description a natural language description of what it means for
	 * this variable to have this value
	 */
	public Assignment(Variable variable, Value value, String description) {
		Utilities.requireNonNull(variable, "variable");
		this.variable = variable;
		this.value = variable.decode(value);
		this.description = description;
		this.code = variable.encode(this.value);
	}
	
	/**
	 * Constructs a new assignment from a variable and value with a blank
	 * description.
	 * 
	 * @param variable the variable whose value is being assigned
	 * @param value the value being assigned to the variable
	 */
	public Assignment(Variable variable, Value value) {
		this(variable, value, null);
	}
	
	@Override
	public boolean equals(Object other) {
		if(other instanceof Assignment otherAssignment)
			return this.variable.equals(otherAssignment.variable) && Objects.equals(this.value, otherAssignment.value);
		else
			return false;
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(variable, value);
	}
	
	@Override
	public String toString() {
		return variable + " = " + value;
	}
	
	@Override
	public String getDescription() {
		return description;
	}
	
	/**
	 * Returns a new assignment that is the same as this assignment, except that
	 * its {@link Described description} is the given value.
	 * 
	 * @param description the description the new assignment should have
	 * @return an assignment identical to this assignment, except with the given
	 * description
	 */
	public Assignment setDescription(String description) {
		return new Assignment(variable, value, description);
	}
	
	@Override
	public String getCode() {
		return code;
	}

	@Override
	public Assignment substitute(Function<Object, Object> substitution) {
		Variable variable = Utilities.requireType(substitution.apply(this.variable), Variable.class, "variable");
		Value value = Utilities.requireType(substitution.apply(this.value), Value.class, "value");
		if(variable != this.variable || value != this.value)
			return new Assignment(variable, value, description);
		else
			return this;
	}
}