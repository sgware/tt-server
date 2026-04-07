package edu.uky.cs.nil.tt.world;

import java.util.function.Function;

import com.google.gson.GsonBuilder;

import edu.uky.cs.nil.tt.io.AbstractAdapter;

/**
 * A logical expression is a {@link Logical logical formula} that can be {@link
 * #evaluate(State) evaluated} to return a {@link Value value} in a {@link State
 * state}.
 * 
 * @author Stephen G. Ware
 */
public interface Expression extends Logical {
	
	/**
	 * Configures a {@link GsonBuilder} to encode and decode {@link Expression}
	 * objects as JSON.
	 * 
	 * @param builder the GSON builder to configure
	 */
	public static void configure(GsonBuilder builder) {
		AbstractAdapter<Expression> adapter = new AbstractAdapter<>(Expression.class);
		builder.registerTypeAdapterFactory(adapter);
		builder.registerTypeAdapter(Expression.class, adapter);
		Value.configure(builder);
	}
	
	@Override
	public Expression substitute(Function<Object, Object> substitution);
	
	/**
	 * Returns the value of this logical expression in the given state.
	 * 
	 * @param state a mapping of values to all of a story world's variables
	 * @return the value of the expression in the given state
	 */
	public Value evaluate(State state);
}