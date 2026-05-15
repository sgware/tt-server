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
	
	/**
	 * Whether or not this assignment can currently be observed. When an
	 * assignment can be observed, it is known to be the case. When it cannot
	 * be observed, it represents an assumed or last known value of the
	 * variable.
	 */
	public final boolean visible;
	
	/** A natural language description of this assignments */
	private final String description;
	
	/** The {@link Encoded code} for this assignment */
	private final String code;
	
	/**
	 * Constructs a new assignment from a variable, value, and description.
	 * 
	 * @param variable the variable whose value is being assigned
	 * @param value the value being assigned to the variable
	 * @param visible whether this assignment is visible and is known (true) or
	 * whether it is invisible and represents an assumed or last known value
	 * (false)
	 * @param description a natural language description of what it means for
	 * this variable to have this value
	 */
	public Assignment(Variable variable, Value value, boolean visible, String description) {
		Utilities.requireNonNull(variable, "variable");
		this.variable = variable;
		this.value = variable.decode(value);
		this.visible = visible;
		this.description = description;
		this.code = variable.encode(this.value);
	}
	
	/**
	 * Constructs a new assignment from a variable and value which is visible
	 * and with a blank description.
	 * 
	 * @param variable the variable whose value is being assigned
	 * @param value the value being assigned to the variable
	 */
	public Assignment(Variable variable, Value value) {
		this(variable, value, true, null);
	}
	
	@Override
	public boolean equals(Object other) {
		if(other instanceof Assignment otherAssignment)
			return this.variable.equals(otherAssignment.variable) && Objects.equals(this.value, otherAssignment.value) && this.visible == otherAssignment.visible;
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

	/**
	 * Returns a new assignment that is the same as this assignment, except that
	 * its {@link #value value} is the given value.
	 * 
	 * @param value the new value to be assigned to this assignment's variable
	 * @return an assignment identical to this assignment, except with the given
	 * value
	 */
	public Assignment setValue(Value value) {
		return new Assignment(variable, value, visible, description);
	}
	
	/**
	 * Returns a new assignment that is the same as this assignment, except that
	 * its {@link #visible visibility} is the given value.
	 * 
	 * @param visible true if this assignment is visible and therefore known or
	 * false if this assignment is not visible and represents an assumed or last
	 * known value
	 * @return an assignment identical to this assignment, except with the given
	 * visibility
	 */
	public Assignment setVisible(boolean visible) {
		return new Assignment(variable, value, visible, description);
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
		return new Assignment(variable, value, visible, description);
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
			return new Assignment(variable, value, visible, description);
		else
			return this;
	}
}