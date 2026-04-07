package edu.uky.cs.nil.tt;

/**
 * A list of command line arguments and various utility methods to parse keys
 * and values.
 * <p>
 * This list assumes that keys begin with a {@code "-"} (dash) character and the
 * values to keys do not. For example, if the command line arguments are:
 * <pre>
 * -k1 value -k2 -k3
 * </pre>
 * Then they contain three keys (starting with dashed). The first key has one
 * value, {@code "value"}, and the second two keys do not have values.
 * 
 * @author Stephen G. Ware
 */
public class Arguments {
	
	/** The array of command line arguments passed to the main method */
	private final String[] args;
	
	/** Marks which arguments have been used in a query */
	private final boolean[] used;
	
	/**
	 * Constructs a new command line argument lists.
	 * 
	 * @param args the array of string passed to the Java main method
	 */
	public Arguments(String[] args) {
		this.args = args;
		this.used = new boolean[args.length];
	}
	
	/**
	 * Tests whether a given key appears in the list of arguments. The key
	 * should be given without its preceding dash. Case is ignored. For example,
	 * if this method is called with {@code "key"}, it will return true if
	 * {@code "-KEY"} appears in the list of arguments. If the key is found, it
	 * is marked as used.
	 * 
	 * @param arg the key, without a dash as its first character
	 * @return true if the key appears in the list of arguments, ignoring case
	 * and with a dash prepended as its first character
	 */
	public boolean contains(String arg) {
		arg = "-" + arg;
		for(int i = 0; i < args.length; i++) {
			if(args[i].equalsIgnoreCase(arg)) {
				used[i] = true;
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Returns the value immediately after a given key, or a default value if
	 * the key has no value, or null if the key does not appear. The key should
	 * be given without its preceding dash, and case is ignored when searching
	 * for the key. For example, if this method is called with {@code "key"} and
	 * {@code "value"}, it will return {@code "value"} if {@code "-KEY"} appears
	 * in the list of arguments. If the key is found, it (and its value, if
	 * found) are marked as used.
	 * 
	 * @param key the key whose value is desired, without a dash as its first
	 * character
	 * @param def a default value to return if the key appears but has no value
	 * @return the value immediately after the key, or the default value if
	 * there is no value after the key, or null if the key does not appear
	 */
	public String getValue(String key, String def) {
		key = "-" + key;
		for(int i = 0; i < args.length; i++) {
			if(args[i].equalsIgnoreCase(key)) {
				used[i] = true;
				if(i < args.length - 1 && !args[i + 1].startsWith("-")) {
					used[i + 1] = true;
					return args[i + 1];
				}
				else
					return def;
			}
		}
		return null;
	}
	
	/**
	 * Throws an exception if any command line arguments have not yet been
	 * marked as used by the other methods provided by this list.
	 * 
	 * @throws IllegalArgumentException if any arguments have not yet been
	 * marked as used
	 */
	public void checkUnused() {
		for(int i = 0; i < args.length; i++)
			if(!used[i])
				throw new IllegalArgumentException("The command line argument \"" + args[i] + "\" is not recognized.");
	}
}