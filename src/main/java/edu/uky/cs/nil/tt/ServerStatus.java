package edu.uky.cs.nil.tt;

import java.util.List;

/**
 * An object the summarizes the the server's status by listing all sessions that
 * are currently {@link ServerStatus.Playing running} and all agents that are
 * currently {@link ServerStatus.Waiting waiting}.
 * 
 * @author Stephen G. Ware
 */
public class ServerStatus {
	
	/**
	 * A summary of a storytelling session that is currently running.
	 * 
	 * @author Stephen G. Ware
	 */
	public static class Playing extends Duration.Ongoing {
		
		/** The name, title, and description of the game master agent */
		public final Titled gm;
		
		/** The name, title, and description of the player agent */
		public final Titled player;
		
		/** The name, title, and description of the story world */
		public final Titled world;
		
		/**
		 * Constructs a summary of a running session from its important values.
		 * 
		 * @param gm the name, title, and description of the game master agent
		 * @param player the name, title, and description of the player agent
		 * @param world the name, title, and description of the story world
		 * @param start the moment when the sessions started as the number of
		 * milliseconds from the epoch (1970-01-01T00:00:00Z) in Coordinated
		 * Universal Time (UTC)
		 */
		public Playing(Titled gm, Titled player, Titled world, long start) {
			super(start);
			Utilities.requireNonNull(gm, "game master");
			this.gm = new Entry(gm);
			Utilities.requireNonNull(player, "player");
			this.player = new Entry(player);
			Utilities.requireNonNull(gm, "story world");
			this.world = new Entry(world);
		}
		
		/**
		 * Constructs a summary of a running session from a session object.
		 * 
		 * @param session the running session to be summarized
		 */
		public Playing(Session session) {
			this(session.gm, session.player, session.world, session.getStartTime());
		}
		
		@Override
		public String toString() {
			return "gm=" + gm.getName() + " player=" + player.getName() + " world=" + world.getName() + " duration=" + super.toString();
		}
	}
	
	/**
	 * A summary of an agent that has joined the server and is waiting for a
	 * partner.
	 * 
	 * @author Stephen G. Ware
	 */
	public static class Waiting extends Duration.Ongoing {
		
		/** The name, title, and description of the waiting agent */
		public final Titled agent;
		
		/**
		 * The name, title, and description of the story world the waiting agent
		 * wants to play in, or null if the agent is willing to play in any
		 * world
		 */
		public final Titled world;
		
		/**
		 * The role the waiting agent wants to play in the story world, or null
		 * if the agent is willing to play either role
		 */
		public final Role role;
		
		/**
		 * The name, title, and description of the partner the waiting agent
		 * wants to play with, or null if the agent is willing to play with any
		 * partner
		 */
		public final Titled partner;
		
		/**
		 * Constructs a summary of a waiting agent from its important values.
		 * 
		 * @param agent the name, title, and description of the waiting agent
		 * @param world the name, title, and description of the story world the
		 * waiting agent wants to play in, or null if the agent is willing to
		 * play in any world
		 * @param role the role the waiting agent wants to play in the story
		 * world, or null if the agent is willing to play either role
		 * @param partner the name, title, and description of the partner the
		 * waiting agent wants to play with, or null if the agent is willing to
		 * play with any partner
		 * @param start the moment when the agent joined the server as the
		 * number of milliseconds from the epoch (1970-01-01T00:00:00Z) in
		 * Coordinated Universal Time (UTC)
		 */
		public Waiting(Titled agent, Titled world, Role role, Titled partner, long start) {
			super(start);
			Utilities.requireNonNull(agent, "agent");
			this.agent = new Entry(agent);
			this.world = world == null ? null : new Entry(world);
			this.role = role;
			this.partner = partner == null ? null : new Entry(partner);
		}
		
		@Override
		public String toString() {
			return "agent=" + name(agent) + " world=" + name(world) + " role=" + name(role) + " partner=" + name(partner) + " duration=" + super.toString();
		}
		
		private static final String name(Object object) {
			if(object == null)
				return "";
			else if(object instanceof Titled titled)
				return titled.getName();
			else
				return object.toString();
		}
	}
	
	/** The version number of the server that created this object */
	public final String version = Settings.VERSION;
	
	/** A list of currently running session summaries */
	public final List<Playing> playing;
	
	/** A list of currently waiting agent summaries */
	public final List<Waiting> waiting;
	
	/**
	 * Constructs a server summary object.
	 * 
	 * @param playing a list of currently running session summaries
	 * @param waiting a list of currently waiting agent summaries
	 */
	public ServerStatus(List<Playing> playing, List<Waiting> waiting) {
		this.playing = playing;
		this.waiting = waiting;
	}
	
	@Override
	public String toString() {
		String string = playing.size() + " sessions:";
		for(Playing playing : this.playing)
			string += "  \n" + playing;
		string += "\n" + waiting.size() + " agents waiting:";
		for(Waiting waiting : this.waiting)
			string += "  \n" + waiting;
		return string;
	}
}