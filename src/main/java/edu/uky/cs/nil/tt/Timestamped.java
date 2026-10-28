package edu.uky.cs.nil.tt;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

/**
 * A timestamped object records the moment it was created.
 * 
 * @author Stephen G. Ware
 */
public interface Timestamped {
	
	/** The format used for printing timestamps as text */
	public static final DateTimeFormatter FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'").withZone(ZoneOffset.UTC);
	
	/**
	 * Formats an instant in time as concise, human-readable text.
	 * 
	 * @param instant an instant in time
	 * @return a concise, human-readable text representation of that instant
	 */
	public static String toString(Instant instant) {
		return FORMAT.format(instant);
	}
	
	/**
	 * Formats an instant in time as concise, human-readable text.
	 * 
	 * @param time an instant in time as the number of milliseconds from the
	 * epoch (1970-01-01T00:00:00Z) in Coordinated Universal Time (UTC)
	 * @return a concise, human-readable text representation of that instant
	 */
	public static String toString(long time) {
		return toString(Instant.ofEpochMilli(time));
	}
	
	/**
	 * An implementation of the {@link Timestamped} interface.
	 * 
	 * @author Stephen G. Ware
	 */
	public static class Adapter implements Timestamped, Comparable<Timestamped> {
		
		/**
		 * The time this object as the number of milliseconds from the epoch
		 * (1970-01-01T00:00:00Z) in Coordinated Universal Time (UTC)
		 */
		public final long time;
		
		/**
		 * Creates a new timestamped object with a given timestamp.
		 * 
		 * @param time a timestamp as the number of milliseconds from the epoch
		 * (1970-01-01T00:00:00Z) in Coordinated Universal Time (UTC)
		 */
		public Adapter(long time) {
			this.time = time;
		}
		
		/**
		 * Creates a new timestamped object whose timestamp is {@link
		 * Instant#now()}.
		 */
		public Adapter() {
			this(Instant.now().toEpochMilli());
		}
		
		@Override
		public int compareTo(Timestamped other) {
			return Long.compare(this.getTimestamp(), other.getTimestamp());
		}
		
		@Override
		public long getTimestamp() {
			return time;
		}
	}
	
	/**
	 * Returns the time this object as the number of milliseconds from the epoch
	 * (1970-01-01T00:00:00Z) in Coordinated Universal Time (UTC)
	 * 
	 * @return the objects timestamp in milliseconds from the epoch in UTC
	 */
	public long getTimestamp();
}
