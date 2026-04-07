package edu.uky.cs.nil.tt.world;

import java.util.Objects;
import java.util.function.Function;

/**
 * A constant is a {@link Value logical value} that always exists in all {@link
 * World story worlds}, such a the Boolean concepts of True and False, numbers,
 * and so on.
 * 
 * @author Stephen G. Ware
 */
public class Constant implements Value {
	
	/** A constant representing nothing or the absence of a value */
	public static final Constant NULL = new Constant(null);
	
	/** A constant representing Boolean true */
	public static final Constant TRUE = new Constant(true);
	
	/** A constant representing Boolean false */
	public static final Constant FALSE = new Constant(false);
	
	/** The Java object associated with this constant */
	public final Object value;
	
	/**
	 * Constructs a new constant equivalent to {@link #NULL}.
	 * 
	 * @param value no value
	 */
	public Constant(Void value) {
		this.value = null;
	}
	
	/**
	 * Constructs a new constant that represents a Boolean value.
	 * 
	 * @param value the boolean value
	 */
	public Constant(boolean value) {
		this.value = value;
	}
	
	/**
	 * Constructs a new constant that represents an integer.
	 * 
	 * @param value the integer value
	 */
	public Constant(int value) {
		this.value = value;
	}
	
	/**
	 * Constructs a new constant that represents a decimal number.
	 * 
	 * @param value the decimal value
	 */
	public Constant(float value) {
		this.value = value;
	}
	
	@Override
	public boolean equals(Object other) {
		return other instanceof Constant otherConstant && Objects.equals(this.value, otherConstant.value);
	}
	
	@Override
	public int hashCode() {
		return Objects.hashCode(value);
	}
	
	@Override
	public String toString() {
		return Objects.toString(value);
	}
	
	@Override
	public Constant substitute(Function<Object, Object> substitution) {
		return this;
	}
	
	@Override
	public boolean toBoolean() {
		return Objects.equals(value, true);
	}
	
	@Override
	public double toNumber() {
		if(value instanceof Number number)
			return number.doubleValue();
		else
			return Value.super.toNumber();
	}
}