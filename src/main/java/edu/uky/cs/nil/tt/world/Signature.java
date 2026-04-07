package edu.uky.cs.nil.tt.world;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

import edu.uky.cs.nil.tt.Utilities;

/**
 * A signature is a {@link Logical logical object} that identifies a
 * parameterized {@link Asset asset} in a {@link World story world}. A signature
 * is defined by a {@link #name name} and an ordered sequence of 0 to many
 * {@link Value values}, called its arguments. Signatures make it possible to
 * tell how different assets of the same type are similar and different from one
 * another. 
 * 
 * @author Stephen G. Ware
 */
public final class Signature implements Logical {
	
	/** The signature's name */
	public final String name;
	
	/** The signature's arguments */
	private final Value[] arguments;
	
	/** A list of the signature's arguments */
	private transient List<Value> argumentList = null;
	
	/**
	 * Constructs a new signature from a name and an array of arguments.
	 * 
	 * @param name the signature's name
	 * @param arguments an ordered sequence of 0 to many values 
	 */
	public Signature(String name, Value...arguments) {
		Utilities.requireName(name);
		this.name = name;
		if(arguments == null)
			arguments = new Value[0];
		Utilities.requireAllNonNull(arguments, "argument");
		this.arguments = arguments;
	}
	
	@Override
	public boolean equals(Object other) {
		if(other instanceof Signature otherSignature)
			return this.name.equals(otherSignature.name) && Arrays.equals(this.arguments, otherSignature.arguments);
		else
			return false;
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(name, getArguments());
	}
	
	@Override
	public String toString() {
		String string = name + "(";
		for(int i = 0; i < arguments.length; i++)
			string += (i == 0 ? "" : ", ") + arguments[i];
		return string + ")";
	}
	
	@Override
	public Signature substitute(Function<Object, Object> substitution) {
		boolean modified = false;
		Value[] arguments = new Value[this.arguments.length];
		for(int i = 0; i < arguments.length; i++) {
			arguments[i] = Utilities.requireType(substitution.apply(this.arguments[i]), Value.class, "argument");
			modified = modified || arguments[i] != this.arguments[i];
		}
		if(modified)
			return new Signature(name, arguments);
		else
			return this;
	}
	
	/**
	 * Returns an unmodifiable list of this signature's arguments.
	 * 
	 * @return a list of arguments
	 */
	public List<Value> getArguments() {
		if(argumentList == null)
			argumentList = List.of(arguments);
		return argumentList;
	}
}