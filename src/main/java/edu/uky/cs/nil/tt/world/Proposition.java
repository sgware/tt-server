package edu.uky.cs.nil.tt.world;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;

import edu.uky.cs.nil.tt.Utilities;

/**
 * A proposition is a {@link Expression logical expression} that can be either
 * true or false. A proposition is an {@link Operator operator} applied to zero
 * or more other expressions, called its {@link #arguments arguments}.
 * 
 * @author Stephen G. Ware
 */
public class Proposition implements Predicate<State>, Expression {
	
	/**
	 * An operator defines the behavior of a {@link Proposition proposition}.
	 */
	public enum Operator {
		
		/**
		 * An operator that requires at least 1 argument and is true just when
		 * all of its arguments are the same
		 */
		EQUALS {
			@Override
			public String toString() {
				return "==";
			}
			
			@Override
			protected void validate(Expression[] arguments) {
				Utilities.requireGreaterThan(arguments.length, 0, "number of arguments");
			}
			
			@Override
			protected Value evaluate(Expression[] arguments, State state) {
				Value previous = arguments[0].evaluate(state);
				for(int i = 1; i < arguments.length; i++)
					if(!previous.equals(arguments[i].evaluate(state)))
						return Constant.FALSE;
				return Constant.TRUE;
			}
		},
		
		/**
		 * An operator that requires exactly 1 argument and is true just when
		 * its argument is false and false just when its argument is true
		 */
		NOT {
			@Override
			protected void validate(Expression[] arguments) {
				Utilities.requireExactly(arguments.length, 1, "number of arguments");
			}
			
			@Override
			protected Value evaluate(Expression[] arguments, State state) {
				return new Constant(!arguments[0].evaluate(state).toBoolean());
			}
		},
		
		/**
		 * An operator that is true just when all of its arguments are true
		 * (and is true if it has no arguments)
		 */
		AND {
			@Override
			protected Value evaluate(Expression[] arguments, State state) {
				for(Expression argument : arguments)
					if(!argument.evaluate(state).toBoolean())
						return Constant.FALSE;
				return Constant.TRUE;
			}
		},
		
		/**
		 * An operator that is true just when one of its arguments is true (and
		 * is false if it has no arguments)
		 */
		OR {
			@Override
			protected Value evaluate(Expression[] arguments, State state) {
				for(Expression argument : arguments)
					if(argument.evaluate(state).toBoolean())
						return Constant.TRUE;
				return Constant.FALSE;
			}
		};
		
		@Override
		public String toString() {
			return super.toString().toLowerCase();
		}
		
		/**
		 * Checks whether this operator can be applied to the given arguments.
		 * Some operators require a certain number or type of arguments. This
		 * method is used to check whether a proposition is valid according to
		 * those rules. This method should throw an exception if this operator
		 * cannot be applied to the given arguments.
		 * 
		 * @param arguments the potential arguments this operator may be applied
		 * to
		 * @throws IllegalArgumentException if the arguments do not meet the
		 * requirements of this operator
		 */
		protected void validate(Expression[] arguments) {
			// do nothing
		}
		
		/**
		 * Evaluates the truth value of a proposition whose operator is this
		 * operator and whose arguments are the given arguments in a given
		 * state.
		 * 
		 * @param arguments the proposition's arguments
		 * @param state the state in which the proposition is evaluated
		 * @return the value of the proposition ({@link Constant#TRUE true} or
		 * {@link Constant#FALSE false})
		 */
		protected abstract Value evaluate(Expression[] arguments, State state);
	}
	
	/** A proposition that is always {@link Constant#TRUE true} */
	public static final Proposition TRUE = new Proposition(Operator.AND);
	
	/** A proposition that is always {@link Constant#FALSE false} */
	public static final Proposition FALSE = new Proposition(Operator.OR);
	
	/** An operator defines the type and behavior of a proposition */
	public final Operator operator;
	
	/** The arguments to the proposition as an array */
	private final Expression[] arguments;
	
	/** The arguments to this proposition as a list */
	private transient List<Expression> argumentList;
	
	/**
	 * Constructs a new proposition from an operator and arguments.
	 * 
	 * @param operator the operator which defines the type and behavior of the
	 * proposition
	 * @param arguments the arguments which will be evaluate by the operator in
	 * a given state
	 */
	public Proposition(Operator operator, Expression...arguments) {
		Utilities.requireNonNull(operator, "operator");
		this.operator = operator;
		Utilities.requireAllNonNull(arguments, "argument");
		this.operator.validate(arguments);
		this.arguments = arguments;
	}
	
	@Override
	public boolean equals(Object other) {
		if(other instanceof Proposition otherProposition)
			return this.operator == otherProposition.operator && Arrays.equals(this.arguments, otherProposition.arguments);
		else
			return false;
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(operator, Arrays.hashCode(arguments));
	}
	
	@Override
	public String toString() {
		if(operator == Operator.AND && arguments.length == 0)
			return "true";
		else if(operator == Operator.OR && arguments.length == 0)
			return "false";
		String string = "(";
		if(arguments.length == 0)
			string += operator;
		else if(arguments.length == 1)
			string += operator + " " + arguments[0];
		else
			for(int i = 0; i < arguments.length; i++)
				string += (i == 0 ? "" : " " + operator + " ") + arguments[i];
		return string + ")";
	}
	
	@Override
	public boolean test(State state) {
		return evaluate(state).toBoolean();
	}
	
	@Override
	public Proposition substitute(Function<Object, Object> substitution) {
		boolean modified = false;
		Expression[] arguments = new Expression[this.arguments.length];
		for(int i = 0; i < arguments.length; i++) {
			arguments[i] = Utilities.requireType(substitution.apply(this.arguments[i]), Expression.class, "expression");
			modified = modified || arguments[i] != this.arguments[i];
		}
		if(modified)
			return new Proposition(operator, arguments);
		else
			return this;
	}
	
	/**
	 * {@inheritDoc}
	 * <p>
	 * A proposition always returns either {@link Constant#TRUE true} or {@link
	 * Constant#FALSE false} when evaluated.
	 */
	@Override
	public Value evaluate(State state) {
		return operator.evaluate(arguments, state);
	}
	
	/**
	 * Returns an unmodifiable list of this proposition's arguments, the other
	 * expressions that make up this proposition and which are considered when
	 * evaluating the truth value of this proposition.
	 * 
	 * @return an unmodifiable list of arguments
	 */
	public List<Expression> getArguments() {
		if(argumentList == null)
			argumentList = List.of(arguments);
		return argumentList;
	}
}