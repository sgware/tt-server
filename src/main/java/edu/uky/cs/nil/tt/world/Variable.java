package edu.uky.cs.nil.tt.world;

import java.util.function.Function;

import edu.uky.cs.nil.tt.Utilities;

/**
 * A variable is a {@link SignedAsset signed asset} that represents a feature of
 * the {@link World story world} {@link State state} that can change. A {@link
 * State state} assign a {@link Value value} to each of a story world's
 * variables.
 * 
 * @author Stephen G. Ware
 */
public final class Variable extends SignedAsset implements Comparable<Variable>, Expression {
	
	/**
	 * The name of the {@link Encoding encoding} this variable uses to encode
	 * its values
	 */
	public final String encoding;
	
	/**
	 * Constructs a new variable.
	 * 
	 * @param id the variable's unique ID number
	 * @param signature the variable's unique signature
	 * @param encoding the name of the {@link Encoding encoding} used to encode
	 * the variable's value
	 * @param description the variable's description
	 */
	public Variable(int id, Signature signature, String encoding, String description) {
		super(id, signature, description);
		Utilities.requireNonNull(encoding, "encoding");
		this.encoding = encoding;
	}
	
	@Override
	public int compareTo(Variable other) {
		return this.getID() - other.getID();
	}
	
	@Override
	protected Variable setID(int id) {
		return new Variable(id, signature, encoding, getDescription());
	}
	
	@Override
	protected Variable setSignature(Signature signature) {
		return new Variable(getID(), signature, encoding, getDescription());
	}
	
	/**
	 * Returns a new variable that is the same as this variable, except that its
	 * value encoding is the given value.
	 * 
	 * @param encoding the name of the value encoding the new variable should
	 * have
	 * @return a variable identical to this variable, except with the given
	 * value encoding
	 */
	protected Variable setEncoding(String encoding) {
		return new Variable(getID(), signature, encoding, getDescription());
	}
	
	@Override
	public Variable setDescription(String description) {
		return new Variable(getID(), signature, encoding, description);
	}
	
	@Override
	public Variable substitute(Function<Object, Object> substitution) {
		Signature signature = this.signature.substitute(substitution);
		if(signature != this.signature)
			return new Variable(getID(), signature, encoding, getDescription());
		else
			return this;
	}
	
	@Override
	public Value evaluate(State state) {
		return state.get(this);
	}
	
	/**
	 * Uses the variable's {@link #encoding value encoding} to {@link
	 * Encoding#encode(Object) encode} the given object.
	 * 
	 * @param value the object to be encoded
	 * @return the encoding of the given value
	 * @throws RuntimeException if the object cannot be encoded
	 */
	public String encode(Object value) {
		if(value instanceof Constant constant)
			return encode(constant.value);
		else
			return Encoding.get(encoding).encode(value);
	}
	
	/**
	 * Uses the variable's {@link #encoding value encoding} to {@link
	 * Encoding#decode(Object) decode} the given object and return it as a
	 * {@link Value value}.
	 * 
	 * @param value the object to be decoded
	 * @return the object that results from decoding the given object, as a
	 * logical value
	 * @throws RuntimeException if the object cannot be decoded
	 */
	public Value decode(Object value) {
		if(value instanceof Constant constant)
			return decode(constant.value);
		value = Encoding.get(encoding).decode(value);
		if(value == null)
			return Constant.NULL;
		else if(value instanceof Value)
			return (Value) value;
		else if(value instanceof Boolean bool) {
			if(bool == true)
				return Constant.TRUE;
			else
				return Constant.FALSE;
		}
		else if(value instanceof Integer integer)
			return new Constant(integer);
		else if(value instanceof Float nfloat)
			return new Constant(nfloat);
		else
			throw new IllegalArgumentException("The value \"" + value + "\" cannot be decoded.");
	}
}