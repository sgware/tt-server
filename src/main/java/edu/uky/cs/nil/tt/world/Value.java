package edu.uky.cs.nil.tt.world;

import java.util.function.Function;

import com.google.gson.GsonBuilder;

import edu.uky.cs.nil.tt.io.AbstractAdapter;

/**
 * A value is a {@link Expression logical expression} which {@link
 * #evaluate(State) evaluates} to itself.
 * 
 * @author Stephen G. Ware
 */
public interface Value extends Expression {
	
	/**
	 * Configures a {@link GsonBuilder} to encode and decode {@link Value}
	 * objects as JSON.
	 * 
	 * @param builder the GSON builder to configure
	 */
	public static void configure(GsonBuilder builder) {
		AbstractAdapter<Value> adapter = new AbstractAdapter<>(Value.class);
		builder.registerTypeAdapterFactory(adapter);
		builder.registerTypeAdapter(Value.class, adapter);
	}
	
	@Override
	public default Value substitute(Function<Object, Object> substitution) {
		return this;
	}
	
	/**
	 * {@inheritDoc}
	 * <p>
	 * A logical value evaluates to itself.
	 */
	@Override
	public default Value evaluate(State state) {
		return this;
	}
	
	/**
	 * If this value represents a Boolean, this method convert it into a Java
	 * {@code boolean}; all other values are converted to {@code false}.
	 * 
	 * @return a Java {@code boolean}, or {@code false} if this value does not
	 * represent a Boolean value
	 */
	public default boolean toBoolean() {
		return false;
	}
	
	/**
	 * If this value represents a number, this method convert it into a Java
	 * {@code double}; all other values are converted to {@link Double#NaN}.
	 * 
	 * @return a Java {@code double}, or NaN if this value represents NaN or is
	 * not a number
	 */
	public default double toNumber() {
		return Double.NaN;
	}
	
	/**
	 * If this value represents an {@link Entity}, this method returns that
	 * entity; all other values are converted to {code null}.
	 * 
	 * @return an entity, or {@code null} if this value is not an entity
	 */
	public default Entity toEntity() {
		return null;
	}
}