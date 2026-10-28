package edu.uky.cs.nil.tt;

import java.time.Instant;

/**
 * A duration is something that has both a {@link #getStartTime() start time}
 * and an {@link #getEndTime() end time}.
 * 
 * @author Stephen G. Ware
 */
public interface Duration {
	
	/**
	 * Converts a length of time into concise, human-readable text expresses
	 * as a number of days, hours, minutes, and seconds.
	 * 
	 * @param milliseconds the length of time in milliseconds
	 * @return the number of days, hours, minutes, and seconds in the given
	 * length of time
	 */
	public static String toString(long milliseconds) {
		if(milliseconds == 0)
			return "0s";
		else if(milliseconds < 1000)
			return "1s";
		java.time.Duration duration = java.time.Duration.ofMillis(milliseconds);
		long days = duration.toDaysPart();
		int hours = duration.toHoursPart();
		int minutes = duration.toMinutesPart();
		int seconds = duration.toSecondsPart();
		String string = "";
		boolean append = days > 0;
		if(append)
			string += days + "d";
		append = append || hours > 0;
		if(append)
			string += hours + "h";
		append = append || minutes > 0;
		if(append)
			string += minutes + "m";
		append = true;
		if(append)
			string += seconds + "s";
		return string;
	}
	
	/**
	 * An implementation of the {@link Duration} interface.
	 * 
	 * @author Stephen G. Ware
	 */
	public static class Adapter implements Duration, Comparable<Duration> {
		
		/**
		 * The time the duration started as the number of milliseconds from the
		 * epoch (1970-01-01T00:00:00Z) in Coordinated Universal Time (UTC)
		 */
		private final long start;
		
		/**
		 * The time the duration ended as the number of milliseconds from the
		 * epoch (1970-01-01T00:00:00Z) in Coordinated Universal Time (UTC)
		 */
		private final long end;
		
		/**
		 * Constructs a duration from its start and end times.
		 * 
		 * @param start the start time as the number of milliseconds from the
		 * epoch (1970-01-01T00:00:00Z) in Coordinated Universal Time (UTC)
		 * @param end the end time as the number of milliseconds from the epoch
		 * (1970-01-01T00:00:00Z) in Coordinated Universal Time (UTC)
		 */
		public Adapter(long start, long end) {
			this.start = start;
			this.end = end;
		}
		
		@Override
		public String toString() {
			return Duration.toString(getDuration());
		}
		
		@Override
		public long getStartTime() {
			return start;
		}
		
		@Override
		public long getEndTime() {
			return end;
		}
		
		@Override
		public int compareTo(Duration other) {
			return Long.compare(this.getDuration(), other.getDuration());
		}
	}
	
	/**
	 * A {@link Duration duration} whose end time is the present.
	 * 
	 * @author Stephen G. Ware
	 */
	public static class Ongoing implements Duration, Comparable<Duration> {
		
		/**
		 * The time the duration started as the number of milliseconds from the
		 * epoch (1970-01-01T00:00:00Z) in Coordinated Universal Time (UTC)
		 */
		private final long start;
		
		/**
		 * Constructs a ongoing duration from its start time.
		 * 
		 * @param start the start time as the number of milliseconds from the
		 * epoch (1970-01-01T00:00:00Z) in Coordinated Universal Time (UTC)
		 */
		public Ongoing(long start) {
			this.start = start;
		}
		
		@Override
		public String toString() {
			return Duration.toString(getDuration());
		}
		
		@Override
		public long getStartTime() {
			return start;
		}
		
		@Override
		public long getEndTime() {
			return Instant.now().toEpochMilli();
		}
		
		@Override
		public int compareTo(Duration other) {
			return Long.compare(this.getStartTime(), other.getStartTime());
		}
	}
	
	/**
	 * Returns the time this duration began as the number of milliseconds
	 * from the epoch (1970-01-01T00:00:00Z) in Coordinated Universal Time
	 * (UTC).
	 * 
	 * @return the time this duration began
	 */
	public long getStartTime();
	
	/**
	 * Returns the time this duration stopped as the number of milliseconds
	 * from the epoch (1970-01-01T00:00:00Z) in Coordinated Universal Time
	 * (UTC).
	 * 
	 * @return the time this duration stopped
	 */
	public long getEndTime();
	
	/**
	 * Returns the amount of time this duration represents (i.e. the amount of
	 * time between the start and end times) in milliseconds.
	 * 
	 * @return the number of milliseconds between the start and end times.
	 */
	public default long getDuration() {
		return getEndTime() - getStartTime();
	}
}