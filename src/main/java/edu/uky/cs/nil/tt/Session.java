package edu.uky.cs.nil.tt;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.google.gson.GsonBuilder;

import edu.uky.cs.nil.tt.io.GenericAdapter;
import edu.uky.cs.nil.tt.io.Stop;
import edu.uky.cs.nil.tt.world.Ending;
import edu.uky.cs.nil.tt.world.Expression;
import edu.uky.cs.nil.tt.world.State;
import edu.uky.cs.nil.tt.world.Turn;
import edu.uky.cs.nil.tt.world.World;

/**
 * A session records the {@link Event events} of a story, any {@link Report
 * reports} of the story's quality, and the final {@link Result result} at the
 * end of a storytelling exercise between a player and game master.
 * <p>
 * When a session is completed, it may be {@link Log#append(Session) written}
 * to file by the server log.
 * 
 * @author Stephen G. Ware
 */
public class Session {
	
	/**
	 * A timestamped object records the moment at which it was created.
	 * 
	 * @author Stephen G. Ware
	 */
	public static class Timestamped {
		
		/**
		 * The time this object was created, as the number of milliseconds from
		 * the epoch (1970-01-01T00:00:00Z) in Coordinated Universal Time (UTC)
		 */
		public final long time;
		
		/**
		 * Constructs a new timestamped object and records the moment at which
		 * it was created.
		 */
		public Timestamped() {
			this.time = Instant.now().toEpochMilli();
		}
	}
	
	/**
	 * An event represents a single {@link Turn turn} during a storytelling
	 * session as well as the actual state of the story world after the turn
	 * and the state of the world as perceived by the player.
	 * 
	 * @author Stephen G. Ware
	 */
	public static class Event extends Timestamped {
		
		/**
		 * The turn which occurred, or null if this event records the initial
		 * state of the story world
		 */
		public final Turn turn;
		
		/** The actual state of the story world after the turn */
		public final State actual;
		
		/**
		 * The state of the story world as perceived by the player after the
		 * turn
		 */
		public final State perceived;
		
		/**
		 * Constructs a new event from a turn that occurred in the story and
		 * the actual and player perceived states after that turn.
		 * 
		 * @param turn a turn that occurred in the story, or null if this event
		 * records the initial state of the story world
		 * @param actual the actual state of the story world after the turn
		 * @param perceived the state of the story world as perceived by the
		 * player after the turn
		 */
		private Event(Turn turn, State actual, State perceived) {
			this.turn = turn;
			this.actual = actual;
			this.perceived = perceived;
		}
		
		@Override
		public String toString() {
			String string = "[Event: ";
			if(turn == null)
				string += "Start";
			else
				string += turn.getDescription();
			return string + "]";
		}
	}
	
	/**
	 * A report occurs when one participant in a session sends their response
	 * to a question about the narrative. Reports are often used to rate the
	 * quality of the narrative or to collect a participant's perception of the
	 * events in the story.
	 * 
	 * @author Stephen G. Ware
	 */
	public static class Report extends Timestamped {
		
		/** The role who sent this report */
		public final Role role;
		
		/** The name of the item or question the participant is responding to */
		public final String item;
		
		/** The value given by the participant for the item or question */
		public final String value;
		
		/**
		 * An optional free-text response given by the participant to explain
		 * the value they gave
		 */
		public final String comment;
		
		/**
		 * Constructs a new report from the message that originally contained
		 * the report.
		 * 
		 * @param role the role making this report
		 * @param report the message containing the item, value, and comment
		 */
		private Report(Role role, edu.uky.cs.nil.tt.io.Report report) {
			this.role = role;
			this.item = report.item;
			this.value = report.value;
			this.comment = report.comment;
		}
		
		@Override
		public String toString() {
			String string = "[Report: \"" + item + "\"=\"" + value + "\"";
			if(comment != null)
				string += "; \"" + comment + "\"";
			return string + "]";
		}
	}
	
	/**
	 * A result explains how the session ended.
	 * 
	 * @author Stephen G. Ware
	 */
	public static class Result extends Timestamped {
		
		/**
		 * One of the endings defined by the story world, or null if this
		 * session did not reach one of the pre-defined endings
		 */
		public final Ending ending;
		
		/** The role who caused the ending */
		public final Role role;
		
		/** A message explaining the reason or way the session ended */
		public final String message;
		
		/**
		 * Constructs a result from the server message which caused the session
		 * to end.
		 * 
		 * @param stop the server message which caused the session to end
		 */
		private Result(Stop stop) {
			this.ending = stop.ending;
			this.role = stop.role;
			this.message = stop.message;
		}
		
		@Override
		public String toString() {
			return "[Result: \"" + (ending == null ? message : ending.getDescription()) + "\"]";
		}
	}
	
	/**
	 * Configures a {@link GsonBuilder} to encode and decode session objects.
	 * 
	 * @param builder the GSON builder to configure
	 */
	public static void configure(GsonBuilder builder) {
		Expression.configure(builder);
		builder.registerTypeAdapterFactory(new GenericAdapter<>(World.class));
	}
	
	/** The world in which the session took place */
	public final World world;
	
	/** The system name of the agent in the game master role */
	public final String gm;
	
	/** The system name of the agent in the player role */
	public final String player;
	
	/** A list of all story events */
	private final List<Event> events;
	
	/** A list of all reports by the participants */
	private final List<Report> reports;
	
	/** The result that records how the session ended */
	private Result result = null;
	
	/**
	 * Constructs a new session from a story world and the names of the game
	 * master and player agents.
	 * 
	 * @param world the story world in which the session will take place
	 * @param gm the name of the agent in the game mast role
	 * @param player the name of the agent in the player role
	 */
	public Session(World world, String gm, String player) {
		this.world = world;
		this.gm = gm;
		this.player = player;
		this.events = new ArrayList<>();
		this.reports = new ArrayList<>();
	}
	
	@Override
	public String toString() {
		return "[Session: world=\"" + world.getName() + "\" gm=\"" + gm + "\" player=\"" + player + "\"]";
	}
	
	/**
	 * Returns an unmodifiable list of all events {@link
	 * #append(Turn, State, State) appended} to this session so far.
	 * 
	 * @return a list of this session's events
	 */
	public List<Event> getEvents() {
		return Collections.unmodifiableList(events);
	}
	
	/**
	 * Returns an unmodifiable list of all reports {@link
	 * #append(Role, edu.uky.cs.nil.tt.io.Report) appended} to this session so
	 * far.
	 * 
	 * @return a list of this session's reports
	 */
	public List<Report> getReports() {
		return Collections.unmodifiableList(reports);
	}
	
	/**
	 * Returns the result of this session, or null if this session does not yet
	 * have a result.
	 * 
	 * @return this session's result
	 */
	public Result getResult() {
		return result;
	}
	
	/**
	 * Adds a new event to this session, which records a turn in the story,
	 * the actual and player perceived states after that turn, and the time
	 * it occurred.
	 * 
	 * @param turn a turn in the story
	 * @param actual the actual state of the story world after the turn
	 * @param perceived the state of the story world as perceived by the player
	 * after the turn
	 */
	protected void append(Turn turn, State actual, State perceived) {
		events.add(new Event(turn, actual, perceived));
	}
	
	/**
	 * Adds a new report to this session, which records a response by one of the
	 * participants to a rating or survey question about the story.
	 * 
	 * @param role the role who sent the report
	 * @param report the message that contains the item, value, and comment of
	 * the report
	 */
	protected void append(Role role, edu.uky.cs.nil.tt.io.Report report) {
		reports.add(new Report(role, report));
	}
	
	/**
	 * Sets the result of this session based on the message which caused the
	 * session to end.
	 * 
	 * @param stop the message which caused the session to end and which
	 * contains the pre-definded ending (if any) and a message explaining why
	 * the session ended
	 */
	protected void setResult(Stop stop) {
		this.result = new Result(stop);
	}
}