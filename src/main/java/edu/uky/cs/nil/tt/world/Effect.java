package edu.uky.cs.nil.tt.world;

import java.util.Objects;
import java.util.function.Function;

import edu.uky.cs.nil.tt.Utilities;

/**
 * An effect is a {@link Logical logical formula} that describes one way that an
 * {@link Action action} modifies one {@link State state} {@link Variable
 * variable}.
 * 
 * @author Stephen G. Ware
 */
public class Effect implements Logical {
	
	/**
	 * Converts an array of effects to a string, similar in format to a {@link
	 * Proposition.Operator#AND conjunction}.
	 * 
	 * @param effects the effects to convert to a string
	 * @return a string representation of the effects
	 */
	public static String toString(Effect[] effects) {
		if(effects.length == 0)
			return Proposition.TRUE.toString();
		else if(effects.length == 1)
			return effects[0].toString();
		else {
			String string = "(";
			for(int i = 0; i < effects.length; i++) {
				if(i > 0)
					string += " " + Proposition.Operator.AND + " ";
				string += effects[i];
			}
			return string + ")";
		}
	}
	
	/** A condition which, if true, means this effect will occur */
	public final Proposition condition;
	
	/** The variable whose value will be modified */
	public final Variable variable;
	
	/** The value or expression whose value will be assigned to the variable */
	public final Expression value;
	
	/**
	 * Constructs a new effect with a condition, variable, and value.
	 * 
	 * @param condition a condition which, if true, means the effect will occur,
	 * and if false, means the condition will not occur, and if null, will be
	 * ignored (treated like it is true)
	 * @param variable the variables whose value will be changed
	 * @param value an expression whose value will be assigned to the variable
	 */
	public Effect(Proposition condition, Variable variable, Expression value) {
		this.condition = condition;
		Utilities.requireNonNull(variable, "variable");
		this.variable = variable;
		if(value == null)
			value = variable.decode(null);
		this.value = value;
	}
	
	/**
	 * Constructs a new effect with a variable and value and whose condition is
	 * {@code null}.
	 * 
	 * @param variable the variables whose value will be changed
	 * @param value an expression whose value will be assigned to the variable
	 */
	public Effect(Variable variable, Expression value) {
		this(null, variable, value);
	}
	
	@Override
	public boolean equals(Object other) {
		if(other instanceof Effect otherEffect)
			return Objects.equals(this.condition, otherEffect.condition) && this.variable.equals(otherEffect.variable) && this.value.equals(otherEffect.value);
		else
			return false;
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(condition, variable, value);
	}
	
	@Override
	public String toString() {
		String string = "(";
		if(condition != null)
			string += "when " + condition + " ";
		string += variable + " = " + value;
		return string + ")";
	}
	
	@Override
	public Effect substitute(Function<Object, Object> substitution) {
		Proposition condition = this.condition == null ? null : Utilities.requireType(substitution.apply(this.condition), Proposition.class, "condition");
		Variable variable = Utilities.requireType(substitution.apply(this.variable), Variable.class, "variable");
		Expression value = Utilities.requireType(substitution.apply(this.value), Expression.class, "value");
		if(condition != this.condition || variable != this.variable || value != this.value)
			return new Effect(condition, variable, value);
		else
			return this;
	}
	
	/**
	 * Evaluates the {@link #condition condition} (if it not not null), and if
	 * it is true (or was null), returns a new state with {@link #variable this
	 * effect's variable} set to the value that results from evaluating {@link
	 * #value this effect's value expression} in the given state.
	 * 
	 * @param state the state in which the condition and value expression will
	 * be evaluated
	 * @return the given state, if the condition was false, or a new state with
	 * the variable set to the value, if the condition was null or true
	 */
	public State apply(State state) {
		if(condition == null || condition.evaluate(state).toBoolean())
			state = state.set(new Assignment(variable, value.evaluate(state)));
		return state;
	}
}