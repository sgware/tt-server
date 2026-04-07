package edu.uky.cs.nil.tt.io;

import java.util.List;

import edu.uky.cs.nil.tt.Database.Entry;
import edu.uky.cs.nil.tt.Settings;
import edu.uky.cs.nil.tt.Utilities;

/**
 * The connect message is sent from the {edu.uky.cs.nil.tt.Server server} to an
 * {@link edu.uky.cs.nil.tt.Agent agent} as soon as the agent connects to give
 * details about which story worlds and agents are available for play.
 * 
 * @author Stephen G. Ware
 */
public class Connect extends Message {
	
	/**
	 * Advertises that a specific story world and agent pair is waiting for a
	 * match.
	 * 
	 * @author Stephen G. Ware
	 */
	public static class Available {
		
		/** The name of the story world the waiting agent wants to play in */
		public final String world;
		
		/** The name of the agent waiting for a match */
		public final String agent;
		
		/**
		 * Constructs an available world/agent pair from a world name and agent
		 * name.
		 * 
		 * @param world the name of the world the waiting agent wants to play in
		 * @param agent the name of the agent waiting for a match
		 */
		public Available(String world, String agent) {
			this.world = world;
			this.agent = agent;
		}
		
		@Override
		public String toString() {
			return "[Available: world=\"" + world + "\"; agent=\"" + agent + "\"]";
		}
	}
	
	/** The server's software version */
	public final String version;
	
	/**
	 * A list of {@link Entry entries} giving the names and descriptions of the
	 * publicly listed worlds available on this server
	 */
	public final List<Entry> worlds;
	
	/**
	 * A list of {@link Entry entries} giving the names and descriptions of the
	 * publicly listed agents available on this server
	 */
	public final List<Entry> agents;
	
	/**
	 * A list of {@link Available available} world/agent pairs that lets an
	 * agent know whether a specific combination of a story world and agent
	 * is immediately available for play
	 */
	public final Available[] available;
	
	/**
	 * Constructs a new connect message with a given list of worlds and agents.
	 * 
	 * @param worlds a list of the names and descriptions of worlds available on
	 * the server
	 * @param agents a list of the names and descriptions of agents available on
	 * the server
	 * @param available a list of currently available world/agent pairs
	 */
	public Connect(List<Entry> worlds, List<Entry> agents, Available[] available) {
		this.version = Settings.VERSION;
		this.worlds = worlds;
		this.agents = agents;
		this.available = available;
	}
	
	@Override
	public String toString() {
		return "[Connect Message: " + worlds.size() + " worlds; " + agents.size() + " agents]";
	}
	
	@Override
	public void verify() {
		Utilities.requireNonNull(version, "version");
		Utilities.requireNonNull(worlds, "worlds");
		Utilities.requireNonNull(agents, "agents");
	}
}