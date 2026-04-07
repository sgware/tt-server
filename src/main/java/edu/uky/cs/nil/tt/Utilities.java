package edu.uky.cs.nil.tt;

import java.util.Objects;
import java.util.Random;

/**
 * A collection of utility methods used throughout this project.
 * 
 * @author Stephen G. Ware
 */
public class Utilities {
	
	/**
	 * Throws an exception if the given object is null.
	 * 
	 * @param object the object which should not be null
	 * @param description a short description of the type of object, used in the
	 * message of the exception which is thrown if the object is null
	 * @throws NullPointerException if the object given is null
	 */
	public static final void requireNonNull(Object object, String description) {
		if(object == null)
			throw new NullPointerException(Utilities.capitalize(description) + " cannot be null.");
	}
	
	/**
	 * Throws an exception if any index in an array is null.
	 * 
	 * @param array the array of objects, none of which should be null
	 * @param description a short description of the type of object in the
	 * array, used in the message of the exception which is thrown if the object
	 * is null
	 * @throws NullPointerException if any index in the given array is null
	 */
	public static final void requireAllNonNull(Object[] array, String description) {
		for(int i = 0; i < array.length; i++)
			if(array[i] == null)
				requireNonNull(array[i], description + " " + (i + 1));
	}
	
	/**
	 * If a given object is of the given type, this method casts the object to
	 * that type; otherwise, it throws an exception.
	 * 
	 * @param <T> the type to which the object will be cast, if possible
	 * @param object the object to be cast
	 * @param type the class to which the object will be cast
	 * @param description a short description of the type of object to be cast,
	 * used in the message of the exception which is thrown if the object cannot
	 * be cast
	 * @return the object, cast to the given type
	 * @throws IllegalArgumentException if the given object is null or cannot be
	 * cast to the given type
	 */
	public static final <T> T requireType(Object object, Class<T> type, String description) {
		if(object == null)
			throw new IllegalArgumentException(Utilities.capitalize(description) + " is null, but it should be " + type + ".");
		else if(!type.isAssignableFrom(object.getClass()))
			throw new IllegalArgumentException(Utilities.capitalize(description) + " \"" + object + "\" is " + object.getClass() + ", but it should be " + type + ".");
		else
			return type.cast(object);
	}
	
	/**
	 * Returns the first object if both objects are {@link
	 * Objects#equals(Object, Object) equal}; otherwise, throws an exception.
	 * This method can accept null arguments. If only one argument is null, the
	 * exception will be thrown, but if both arguments are null, no exception
	 * will be thrown.
	 * 
	 * @param <T> the type of the first object
	 * @param object the first object
	 * @param other the second object
	 * @return the first object, if both objects are equal
	 * @throws IllegalArgumentException if the two objects are not equal
	 */
	public static final <T> T requireEquals(T object, Object other) {
		if(Objects.equals(object, other))
			return object;
		else
			throw new IllegalArgumentException("Encountered \"" + object + "\" but expected \"" + other + "\".");
	}
	
	/**
	 * Throws an exception if a given number is anything other than a specific
	 * value.
	 * 
	 * @param number the number which should be a specific value
	 * @param value the value that number should be
	 * @param description a short description of the purpose of the number, used
	 * in the message of the exception which is thrown if the number is not the
	 * correct value
	 * @throws IllegalArgumentException if the number is not the given value
	 */
	public static final void requireExactly(long number, long value, String description) {
		if(number != value)
			throw new IllegalArgumentException(Utilities.capitalize(description) + " must be exactly " + value + ", but it was " + number + ".");
	}
	
	/**
	 * Throws an exception if a number is negative.
	 * 
	 * @param number the number which should not be negative
	 * @param description a short description of the purpose of the number, used
	 * in the message of the exception which is thrown if the number is negative
	 */
	public static final void requireNonNegative(long number, String description) {
		if(number < 0)
			throw new IllegalArgumentException(Utilities.capitalize(description) + " cannot be negative.");
	}
	
	/**
	 * Throws an exception if a number is not greater than a given lower bound.
	 * 
	 * @param number the number which must be greater than the lower bound
	 * @param lower the lower bound
	 * @param description a short description of the purpose of the number, used
	 * in the message of the exception which is thrown if the number is not
	 * greater than the lower bound
	 */
	public static final void requireGreaterThan(long number, long lower, String description) {
		if(!(number > lower))
			throw new IllegalArgumentException(Utilities.capitalize(description) + " must be greater than " + lower + ", but it was " + number + ".");
	}
	
	/**
	 * Throws an exception if a number is not less than or equal to a given
	 * upper bound.
	 * 
	 * @param number the number which must be less than or equal to the upper
	 * bound
	 * @param max the upper bound
	 * @param description a short description of the purpose of the number, used
	 * in the message of the exception which is thrown if the number is not
	 * less than or equal to the upper bound
	 * @throws IllegalArgumentException if the number if not less than or equal
	 * to the given upper bound
	 */
	public static final void requireLessThanOrEqualTo(long number, long max, String description) {
		if(!(number <= max))
			throw new IllegalArgumentException(Utilities.capitalize(description) + " must be less than or equal to " + max + ", but it was " + number + ".");
	}
	
	/**
	 * Throws an exception if a string does not meet the requirements for
	 * a {@link Named name}. Requirements may include limits on length and
	 * the number of characters.
	 * 
	 * @param string the string which must be a valid name
	 * @throws IllegalArgumentException if the string is not a valid name
	 */
	public static final void requireName(String string) {
		requireNonNull(string, "name");
		if(string.length() > Settings.NAME_MAX_LENGTH)
			throw new IllegalArgumentException("The name \"" + string + "\" is too long. It may only contain up to " + Settings.NAME_MAX_LENGTH + " characters.");
		for(int i = 0; i < string.length(); i++)
			if(!Settings.NAME_ALLOWED_CHARACTERS.contains(string.charAt(i)))
				throw new IllegalArgumentException("The character '" + string.charAt(i) + "' is not allowed in a name.");
	}
	
	/**
	 * Returns the number of bits needed to assign unique ID numbers to each
	 * of a given number of objects. If the given number of objects is 0 or 1,
	 * then 0 bits are needed. Two objects require 1 bit. Three or four objects
	 * require 2 bits, etc.
	 * 
	 * @param count the number of objects which must each be assigned a unique
	 * ID number
	 * @return the minimum number of bits needed to assign a unique ID number to
	 * each of the given number of objects
	 */
	public static final int bits(int count) {
		requireNonNegative(count, "count");
		if(count <= 1)
			return 0;
		else
			return (int) Math.ceil(Math.log(count) / Math.log(2));
	}
	
	/**
	 * Capitalizes the first letter of a string.
	 * 
	 * @param string the string whose first letter should be a capital letter
	 * @return an identical string, except that the first letter has been
	 * capitalized
	 */
	public static final String capitalize(String string) {
		if(string != null && string.length() > 1)
			string = string.substring(0, 1).toUpperCase() + string.substring(1);
		return string;
	}
	
	/**
	 * {@link Integer#parseInt(String) Parses} a string into an integer or
	 * throws an exception if the string cannot be parsed.
	 * 
	 * @param string the string to be parsed as an integer
	 * @return the integer
	 * @throws NumberFormatException if the string cannot be parsed as an
	 * integer
	 */
	public static final int toInteger(String string) {
		try {
			return Integer.parseInt(string);
		}
		catch(NumberFormatException cause) {
			throw new IllegalArgumentException("The string \"" + string + "\" cannot be parsed as an integer.");
		}
	}
	
	/**
	 * A string of all the characters that can be used when generating {@link
	 * #getRandomName() random names}
	 */
	private static final String RANDOM_NAME_CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
	
	/**
	 * Randomly generates a valid {@link Named name}.
	 * 
	 * @return a random valid name
	 */
	public static final String getRandomName() {
		String name = "";
		Random random = new Random();
		for(int i = 0; i < 10; i++)
			name += RANDOM_NAME_CHARACTERS.charAt(random.nextInt(RANDOM_NAME_CHARACTERS.length()));
		return name;
	}
	
	private Utilities() {
		// Do nothing.
	}
}