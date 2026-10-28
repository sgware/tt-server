package edu.uky.cs.nil.tt;

import java.time.Instant;

/**
 * A timestamped object records the moment it was created.
 * 
 * @author Stephen G. Ware
 */
public interface Timestamped {
	
	/**
	 * An implementation of the {@link Timestamped} interface.
	 * 
	 * @author Stephen G. Ware
	 */
	public static class Adapter implements Timestamped {
		
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
