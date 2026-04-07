package edu.uky.cs.nil.tt.world;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import edu.uky.cs.nil.tt.Named;
import edu.uky.cs.nil.tt.Role;
import edu.uky.cs.nil.tt.Utilities;

/**
 * An encoding is a method for translating data types to and from Java {@code
 * String}s so they can serialized, saved to file, and transmitted between the
 * {@link edu.uky.cs.nil.tt.Server server} and its {@link
 * edu.uky.cs.nil.tt.Agent agents}. Encodings are primarily used to encode the
 * {@link Value values} of {@link Variable variables} in a {@link State state}
 * (see {@link Variable#encoding}).
 * <p>
 * An {@link Encoded encoded} object is one whose value can be encoded.
 * 
 * @author Stephen G. Ware
 */
public abstract class Encoding implements Named {
	
	/**
	 * The prefixed used by all {@link Encoding.UnsignedIntegerEncoding unsigned
	 * integer encodings}
	 */
	public static final String UNSIGNED_INTEGER = "uint";
	
	/** An encoding for Boolean true and false values */
	public static final Encoding BOOLEAN = new Encoding("boolean") {
		
		/**
		 * {@inheritDoc}
		 * <p>
		 * Boolean {@code true} is encoded as {@code "1"}. Boolean {@code false}
		 * is encoded as {@code "0"}.
		 */
		@Override
		public String encode(Object value) {
			if(Objects.equals(value, true))
				return "1";
			else
				return "0";
		}
		
		/**
		 * {@inheritDoc}
		 * <p>
		 * The Java {@code boolean} value {@code true}, the Java {@code String}s
		 * {@code "true"} (ignoring case) and {@code "1"}, and any Java {@link
		 * Number} with a value other than {@code 0} are decoded to {@code true}.
		 * All other values are decoded to {@code false}.
		 */
		@Override
		public Object decode(Object value) {
			if(value == null)
				return false;
			else if(Objects.equals(value, true))
				return true;
			else if(value instanceof String string && (string.equalsIgnoreCase("true") || string.equals("1")))
				return true;
			else if(value instanceof Number number && number.doubleValue() != 0.0)
				return true;
			else
				return false;
		}
	};
	
	/** And encoding for session {@link Role} values */
	public static final Encoding ROLE = new Encoding(Role.class.getSimpleName()) {
		
		@Override
		public String encode(Object value) {
			return switch(Utilities.requireType(value, Role.class, "value")) {
			case GAME_MASTER -> "0";
			case PLAYER -> "1";
			};
		}
		
		/**
		 * {@inheritDoc}
		 * <p>
		 * The constant {@link Role#PLAYER} and the Java {@code String "player"}
		 * (ignoring case) are decoded to {@link Role#PLAYER}. All other values
		 * are decoded to {@link Role#GAME_MASTER}.
		 */
		@Override
		public Object decode(Object value) {
			if(value instanceof Role role)
				return role;
			else if(value != null && value.toString().equalsIgnoreCase(Role.PLAYER.toString()))
				return Role.PLAYER;
			else
				return Role.GAME_MASTER;
		}
	};
	
	/** And encoding for {@link Turn.Type} values */
	public static final Encoding TURN_TYPE = new Encoding("turntype") {
		
		/** The encoding for {@link Turn.Type#PROPOSE} */
		private static final String PROPOSE_ENCODING = "00";
		
		/** The encoding for {@link Turn.Type#SUCCEED} */
		private static final String SUCCEED_ENCODING = "01";
		
		/** The encoding for {@link Turn.Type#FAIL} */
		private static final String FAIL_ENCODING = "10";
		
		/** The encoding for {@link Turn.Type#PASS} */
		private static final String PASS_ENCODING = "11";
		
		@Override
		public String encode(Object value) {
			return switch(Utilities.requireType(value, Turn.Type.class, "value")) {
			case PROPOSE -> PROPOSE_ENCODING;
			case SUCCEED -> SUCCEED_ENCODING;
			case FAIL -> FAIL_ENCODING;
			case PASS -> PASS_ENCODING;
			};
		}
		
		/**
		 * {@inheritDoc}
		 * <p>
		 * Unrecognized objects are decoded as {@link Turn.Type#SUCCEED}.
		 */
		@Override
		public Object decode(Object value) {
			if(value instanceof Turn.Type type)
				return type;
			else if(value != null) {
				String string = value.toString();
				if(string.equalsIgnoreCase(Turn.Type.PROPOSE.toString()) || string.equalsIgnoreCase(PROPOSE_ENCODING))
					return Turn.Type.PROPOSE;
				else if(string.equalsIgnoreCase(Turn.Type.FAIL.toString()) || string.equalsIgnoreCase(FAIL_ENCODING))
					return Turn.Type.FAIL;
				else if(string.equalsIgnoreCase(Turn.Type.PASS.toString()) || string.equalsIgnoreCase(PASS_ENCODING))
					return Turn.Type.PASS;
			}
			return Turn.Type.SUCCEED;
		}
	};
	
	/** Tracks all known encodings by name */
	private static final Map<String, Encoding> instances = new HashMap<>();
	
	static {
		instances.put(BOOLEAN.name, BOOLEAN);
		instances.put(ROLE.name, ROLE);
		instances.put(TURN_TYPE.name, TURN_TYPE);
	}
	
	/**
	 * Returns the encoding method with a given name.
	 * 
	 * @param name the name of the desired encoding
	 * @return the encoding objects with the given name
	 * @throws IllegalArgumentException if the given names does not match a
	 * known encoding
	 */
	public static Encoding get(String name) {
		name = name.toLowerCase();
		Encoding encoding = instances.get(name);
		if(encoding == null) {
			if(name.startsWith(UNSIGNED_INTEGER))
				encoding = new UnsignedIntegerEncoding(name);
			else if(name.startsWith("entity"))
				encoding = new EntityEncoding(name);
			else if(name.startsWith("action"))
				encoding = new ActionEncoding(name);
			else if(name.startsWith("ending"))
				encoding = new EndingEncoding(name);
			else
				throw new IllegalArgumentException("The encoding \"" + name + "\" is not defined.");
			instances.put(name, encoding);
		}
		return encoding;
	}
	
	/** The unique name of this encoding method */
	public final String name;
	
	/**
	 * Constructs a new encoding method with the given name.
	 * 
	 * @param name the encoding method's unique name
	 */
	private Encoding(String name) {
		Utilities.requireNonNull(name, "name");
		this.name = name;
	}
	
	@Override
	public String toString() {
		return getName();
	}
	
	@Override
	public String getName() {
		return name;
	}
	
	/**
	 * Returns a string of {@code 1}'s and {@code 0}'s that uniquely represents
	 * this object among other objects of the same type in the same story world.
	 * <p>
	 * Encodings are returned as a string, rather than a number, to make it
	 * clear how many bits are required to represent other objects of the same
	 * type in the same {@link World story world}. For example, {@link Entity}
	 * is an {@link Encoded encoded} object. A story world with 3 entities would
	 * require 2 bits to represent an etity ({@code 00} for {@code null}, {@code
	 * 01} for the first entity, {@code 10} for the second entity, and {@code 11}
	 * for the third entity). All entities in that story world should return a
	 * string of length 2, even if they must be padded with {@code 0}'s, to make
	 * it clear how many bits are used in the encoding.
	 * 
	 * @param value the object to be encoded
	 * @return a string of {@code 1}'s and {@code 0}'s that uniquely represents
	 * this object
	 * @throws IllegalArgumentException if the given object cannot be encoded
	 * using this method
	 */
	public abstract String encode(Object value);
	
	/**
	 * Returns the object that produced the given encoding, or converts an
	 * easily recognizable object into the kind of object this encoding uses.
	 * This method can be used as the reverse of {@link #encode(Object)}, but
	 * it allows arbitrary objects as input in case other types of objects are
	 * already the right type or can be converted into the right type. This
	 * method can be used both as a decoder and also as a means of checking that
	 * an already decoded value is correctly formatted.
	 * <p>
	 * For example, if this encoding represent an unsigned integer with a
	 * certain number of bits, this method can be used to decode a string of
	 * {@code 1}'s and {@code 0}'s into the original integer; however, if it is
	 * passed a number object, this method can check that the number is of the
	 * correct format (i.e. that it is an integer and that it is small enough
	 * that it could be encoded using this encoding's number of bits).
	 * 
	 * @param value an encoding string or a decided object which needs to be
	 * checked for correctness
	 * @return the object that produced the encoding, or the original object
	 * (or something eqivalent) if this method is being used to check the
	 * correctness of a decoded object
	 * @throws IllegalArgumentException if the given object cannot be decoded or
	 * is not properly formatted
	 */
	public abstract Object decode(Object value);
	
	/**
	 * An encoding for unsigned integers that uses an arbitrary number of bits.
	 * 
	 * @author Stephen G. Ware
	 */
	private static class UnsignedIntegerEncoding extends Encoding {
		
		/** The number of bits used in the encoding */
		private final int bits;
		
		/** The maximum value an integer can have in this encoding */
		private final long max;
		
		/**
		 * Constructs a new unsigned integer encoding whose name starts with a
		 * given prefix followed by the number of bits used.
		 * 
		 * @param name the first part of the name
		 * @param bits the second part of the name, which expresses how many
		 * bits are used in the encoding
		 */
		protected UnsignedIntegerEncoding(String name, int bits) {
			super(name + bits);
			this.bits = bits;
			this.max = Math.max(0, (long) Math.pow(2, this.bits) - 1);
		}
		
		/**
		 * Constructs a new unsigned integer encoding from its name, which must
		 * be {@link Encoding#UNSIGNED_INTEGER} followed by the number of bits
		 * used.
		 * 
		 * @param name the name of this unsigned integer encoding
		 * @throws IllegalArgumentException if the name does not follow the
		 * required format
		 */
		public UnsignedIntegerEncoding(String name) {
			this(UNSIGNED_INTEGER, toBits(UNSIGNED_INTEGER, name));
		}
		
		/**
		 * Checks that a name follows the format of a prefix followed by the
		 * number of bits to use in the encoding, then returns the number of
		 * bits.
		 * 
		 * @param prefix the prefix that the name must start with
		 * @param name the name whose formatting will be checked
		 * @return the number of bits at the end of the name
		 * @throws IllegalArgumentException if the name does not follow the
		 * correct format
		 */
		protected static final int toBits(String prefix, String name) {
			if(!name.startsWith(prefix))
				throw new IllegalArgumentException("Name must start with \"" + prefix + "\".");
			name = name.substring(prefix.length());
			int value = Utilities.toInteger(name);
			Utilities.requireNonNegative(value, "number of bits");
			Utilities.requireLessThanOrEqualTo(value, 32, "number of bits");
			return value;
		}
		
		@Override
		public String encode(Object value) {
			if(bits == 0)
				return "";
			else {
				Integer integer = Utilities.requireType(value, Integer.class, "value");
				Utilities.requireNonNegative(integer, "value");
				Utilities.requireLessThanOrEqualTo(integer, max, "value");
				String string = Integer.toBinaryString(integer);
				while(string.length() < bits)
					string = "0" + string;
				return string;
			}
		}
		
		/**
		 * {@inheritDoc}
		 * <p>
		 * If the given object is a number, it must be a positive integer and
		 * it must be possible to encode it in {@link #bits this encoding's
		 * number of bits}.
		 * <p>
		 * The value {@code null} is decoded as 0.
		 * 
		 * @throws NumberFormatException if the given value is a string but
		 * cannot be decoded
		 */
		@Override
		public Object decode(Object value) {
			Integer integer = null;
			if(value instanceof Number n && n.doubleValue() == n.intValue())
				integer = n.intValue();
			else if(value instanceof String s) {
				try {
					integer = Integer.parseInt(s, 2);
				}
				catch(NumberFormatException exception) {/* do nothing */}
			}
			if(integer == null)
				integer = 0;
			if(integer >= 0 && integer <= max)
				return integer.intValue();
			else
				return 0;
		}
	}
	
	/**
	 * An encoding for {@link Unique unique} objects that encodes their {@link
	 * Unique#getID() ID numbers} as {@link UnsignedIntegerEncoding unsigned
	 * integers}.
	 * 
	 * @author Stephen G. Ware
	 */
	private static class UniqueObjectEncoding extends UnsignedIntegerEncoding {
		
		/** The type of object this encoding encodes and decodes */
		public final Class<? extends Unique> type;
		
		/**
		 * A short natural language phrase describing of the type of object this
		 * encoding encodes and decodes
		 */
		public final String description;
		
		/**
		 * Constructs a new unique object encoding for a given type of object.
		 * 
		 * @param name this encoding's name
		 * @param bits the number of bits used by this encoding
		 * @param type the type of object encoded and decoded
		 * @param description a short natural language phrase describing of the
		 * type of object this encoding will encode and decode
		 */
		protected UniqueObjectEncoding(String name, int bits, Class<? extends Unique> type, String description) {
			super(name, bits);
			this.type = type;
			this.description = description;
		}
		
		/**
		 * {@inheritDoc}
		 * <p>
		 * The {@code null} value is encoded as all {@code 0}s.
		 * <p>
		 * A {@link Unique unique} object is encoded by adding 1 to its ID
		 * number and then encoding that value as an {@link
		 * UnsignedIntegerEncoding unsigned integer}.
		 */
		@Override
		public String encode(Object value) {
			if(value == null || Objects.equals(value, 0))
				return super.encode(0);
			else
				return super.encode(Utilities.requireType(value, type, description).getID() + 1);
		}
		
		/**
		 * {@inheritDoc}
		 * <p>
		 * Because this encoding is a general encoding and not coupled to any
		 * specific {@link World world} object, this method cannot recover a
		 * specific object from an encoding. If the object given to this method
		 * is of this encoding's type, it is returned; otherwise, this method
		 * returns {@code null}.
		 */
		@Override
		public Object decode(Object value) {
			if(value != null && type.isAssignableFrom(value.getClass()))
				return type.cast(value);
			else
				return null;
		}
	}
	
	/**
	 * An encoding for {@link Entity entities}.
	 * 
	 * @author Stephen G. Ware
	 */
	private static class EntityEncoding extends UniqueObjectEncoding {
		
		/**
		 * Creates an entity encoding that uses a given number of bits.
		 * 
		 * @param name the name should always begin with string {@code "entity"}
		 * followed by the number of bits used in the encoding
		 */
		public EntityEncoding(String name) {
			super("entity", toBits("entity", name), Entity.class, "entity");
		}
	}
	
	/**
	 * An encoding for {@link Action actions}.
	 * 
	 * @author Stephen G. Ware
	 */
	private static class ActionEncoding extends UniqueObjectEncoding {
		
		/**
		 * Creates an action encoding that uses a given number of bits.
		 * 
		 * @param name the name should always begin with string {@code "action"}
		 * followed by the number of bits used in the encoding
		 */
		public ActionEncoding(String name) {
			super("action", toBits("action", name), Action.class, "action");
		}
	}
	
	/**
	 * An encoding for {@link Ending endings}.
	 * 
	 * @author Stephen G. Ware
	 */
	private static class EndingEncoding extends UniqueObjectEncoding {
		
		/**
		 * Creates an ending encoding that uses a given number of bits.
		 * 
		 * @param name the name should always begin with string {@code "ending"}
		 * followed by the number of bits used in the encoding
		 */
		public EndingEncoding(String name) {
			super("ending", toBits("ending", name), Ending.class, "ending");
		}
	}
}