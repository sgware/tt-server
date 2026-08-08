package edu.uky.cs.nil.tt;

import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.SSLSocket;

import com.sgware.serialsoc.CheckedRunnable;
import com.sgware.serialsoc.SecureSerialServerSocket;
import edu.uky.cs.nil.tt.io.Connect.Available;
import edu.uky.cs.nil.tt.io.End;
import edu.uky.cs.nil.tt.io.Join;
import edu.uky.cs.nil.tt.world.World;
import edu.uky.cs.nil.tt.world.WorldModel;

/**
 * The server listens for new connections from the network, reports which story
 * worlds and agent types it supports, and makes matches between {@link Agent
 * agents} so they can collaborate in storytelling {@link Session sessions}.
 * 
 * @author Stephen G. Ware
 */
public class Server extends SecureSerialServerSocket {
	
	/** Used to record system messages and data from completed sessions */
	public final Log log;
	
	/** A set of story worlds and agent types supported by this server */
	public final Database database;
		
	/** The ID number to assign to the next agent that connects */
	private int nextID = 0;
	
	/** A list of agents waiting for an appropriate partner */
	private final List<Join> waiting = new ArrayList<>();
	
	/** A list of agents in active sessions */
	private final List<Agent> playing = new ArrayList<>();
	
	/** Whether the server has been instructed to begin shutting down */
	private boolean stopped = false;
	
	/**
	 * Constructs a server from its various components.
	 * 
	 * @param log the log file where system messages will be recorded, or null
	 * if the server should not record a log
	 * @param sessions the directory where the server should write completed
	 * session files, or null if the server should not record session data
	 * @param database the existing database the server should use (if the file
	 * exists), or the file where the server should save its database (if the
	 * file does not exist), or null if the server start with an empty database
	 * and not save it to file
	 * @param port the network port on which the server should listen for new
	 * connections
	 * @throws IOException if a problem occurs while reading the various files
	 * created or read during setup
	 */
	public Server(File log, File sessions, File database, int port) throws IOException {
		super(port);
		try {
			this.log = new Log(log, sessions);
		}
		catch(Exception exception) {
			Log.append("An error occurred while creating the server log:", exception, null);
			close();
			throw exception;
		}
		try {
			this.database = new Database(database, this.log);
		}
		catch(Exception exception) {
			this.log.append("An error occurred while creating the database:", exception);
			close();
			this.log.close();
			throw exception;
		}
		this.log.append("Server created.");
	}
	
	/**
	 * Returns true if this server has been {@link #close() instructed to begin
	 * shutting down}.
	 * 
	 * @return true if the server has begun shutting down, false otherwise
	 */
	public boolean getStopped() {
		return stopped;
	}
	
	@Override
	protected void connect() throws IOException {
		if(System.getProperty("javax.net.ssl.keyStore") == null)
			log.append("Warning: The system property \"javax.net.ssl.keyStore\" is not set. The server may not be able to establish SSL sockets.");
		else if(System.getProperty("javax.net.ssl.keyStorePassword") == null)
			log.append("Warning: The system property \"javax.net.ssl.keyStorePassword\" is not set. The server may not be able to establish SSL sockets.");
		super.connect();
		log.append("Server now listening for new connections on port " + getPort() + ".");
	}
	
	@Override
	protected SSLSocket accept() throws IOException {
		SSLSocket socket = null;
		while(socket == null) {
			socket = super.accept();
			try {
				socket.startHandshake();
			}
			catch(IOException exception) {
				log.append("An exception occurred while a client was attempting to connect.", exception);
				try {
					socket.close();
				}
				catch(Exception other) {
					// Ignore exceptions from closing the socket.
				}
				socket = null;
			}
		}
		return socket;
	}
	
	@Override
	protected Agent create(Socket socket) throws IOException {
		return new Agent(this, (SSLSocket) socket, nextID++);
	}
	
	@Override
	protected void execute(CheckedRunnable runnable) {
		super.execute(runnable);
	}
	
	/**
	 * Returns a list of story world/agent pairs that are currently waiting and
	 * could be matched immediately. Only pairs where both the world and agent
	 * are publicly listed in the server's database will be returned.
	 * 
	 * @return a list of world/agent pairs that are currently waiting for
	 * matches
	 */
	public Available[] getAvailable() {
		List<Available> available = new ArrayList<>();
		List<Database.Entry> worlds = database.getListedWorlds();
		for(Join waiting : this.waiting) {
			Database.AgentEntry entry = database.getAgent(waiting.name);
			if(entry != null && entry.getListed()) {
				if(waiting.world == null)
					for(Database.Entry world : worlds)
						available.add(new Available(world.getName(), waiting.name));
				else
					available.add(new Available(waiting.world, waiting.name));
			}
		}
		return available.toArray(new Available[available.size()]);
	}
	
	/**
	 * This method is called each time a new {@link Agent agent} sends its
	 * {@link Join join} message. This method will register the new join
	 * request and try to {@link #makeMatches() make matches} between pairs
	 * of compatible agents.
	 * 
	 * @param join a new agent's join message, specifying the details of its
	 * request
	 * @throws Exception if an exception occurs while processing the agent's
	 * join request or while making matches between agents
	 */
	protected void onJoin(Join join) throws Exception {
		Agent agent = join.getAgent();
		// Log join request.
		String message = "Agent " + agent.id + " joined with name \"" + join.name + "\"";
		if(join.world != null)
			message += " for world \"" + join.world + "\"";
		if(join.role != null)
			message += " as " + join.role;
		if(join.partner != null)
			message += " requesting partner \"" + join.partner + "\"";
		message += ".";
		log.append(message);
		// Make matches.
		waiting.add(join);
		makeMatches();
	}
	
	/**
	 * This method tries to find pairs of compatible agents who are waiting for
	 * sessions and, if any are found, starts sessions for them.
	 * 
	 * @throws Exception if a problem occurs while matching agents
	 */
	protected void makeMatches() throws Exception {
		// Remove any waiting agents who have disconnected.
		waiting.removeIf(join -> !join.getAgent().getConnected());
		// Make matches until no more can be found.
		while(makeMatch());
	}
	
	/**
	 * This method finds a single pair of compatible agents who are waiting for
	 * a session and, if one is found, starts a session for them.
	 * 
	 * @return true if a compatible pairs of agents was found and a session
	 * started, or false if not
	 * @throws Exception if a problem occurs while matching agents
	 */
	protected boolean makeMatch() throws Exception {
		// Find a pair of compatible join requests.
		Join first = null;
		Join second = null;
		for(int i = 0; i < waiting.size() - 1 && second == null; i++) {
			first = waiting.get(i);
			for(int j = i + 1; j < waiting.size(); j++) {
				second = waiting.get(j);
				if(matches(first, second))
					break;
				else
					second = null;
			}
		}
		// Fail if no pair was found.
		if(first == null || second == null)
			return false;
		// Remove requests from the waiting list.
		waiting.remove(first);
		waiting.remove(second);
		// Find story world.
		WorldModel world;
		if(first.world != null)
			world = database.requireWorld(first.world).getWorld();
		else if(second.world != null)
			world = database.requireWorld(second.world).getWorld();
		else
			world = database.getRandomWorld().getWorld();
		// Assign GM and player roles.
		Agent gm, player;
		if(first.role == Role.GAME_MASTER || second.role == Role.PLAYER) {
			gm = first.getAgent();
			player = second.getAgent();
		}
		else {
			gm = second.getAgent();
			player = first.getAgent();
		}
		// Start session.
		onStart(world, gm, player);
		return true;
	}
	
	/**
	 * Checks whether a pair of join requests represents an opportunity for a
	 * new session to start.
	 * 
	 * @param first the join request of the first agent
	 * @param second the join request of the second agent
	 * @return true if the requests from these agents is compatible and allowed
	 * by the server, false otherwise
	 */
	private final boolean matches(Join first, Join second) {
		// Check if the requests are compatible.
		if(!first.matches(second))
			return false;
		// Check if the limits allow the first agent to play.
		int firstCount = 0;
		for(Agent agent : playing)
			if(agent.getName().equals(first.name))
				firstCount++;
		Database.AgentEntry firstEntry = database.getAgent(first.name);
		if(firstEntry != null && firstEntry.getLimit() > 0 && firstCount >= firstEntry.getLimit())
			return false;
		// Check if the limits allow the second agent to play.
		int secondCount = 0;
		for(Agent agent : playing)
			if(agent.getName().equals(second.name))
				secondCount++;
		if(second.name.equals(first.name))
			secondCount++;
		Database.AgentEntry secondEntry = database.getAgent(second.name);
		if(secondEntry != null && secondEntry.getLimit() > 0 && secondCount >= secondEntry.getLimit())
			return false;
		// Allow the match.
		return true;
	}
	
	/**
	 * This method is called each time a new {@link Session session} starts
	 * between two compatible {@link Agent agents}.
	 * 
	 * @param world the story world in which the session will take place
	 * @param gm the agent playing the game master role
	 * @param player the agent playing the player role
	 * @throws Exception if a problem occurs while starting the new session
	 */
	protected void onStart(World world, Agent gm, Agent player) throws Exception {
		log.append("New session started in world \"" + world.getName() + "\" with agent " + gm.id + " \"" + gm.getName() + "\" as game master and agent " + player.id + " \"" + player.getName() + "\" as player.");
		playing.add(gm);
		playing.add(player);
		Session session = new Session(world, gm.getName(), player.getName());
		gm.onStart(session, gm, player);
		player.onStart(session, gm, player);
		session.append(null, gm.getStatus().getState(), player.getStatus().getState());
	}
		
	/**
	 * This method is called each time a {@link Session session} ends. Note that
	 * this method is not called immediately after its story ends, but only
	 * after both agents have either acknowledged the end via {@link
	 * edu.uky.cs.nil.tt.io.Stop stop} messages or by disconnecting, which means
	 * all {@link edu.uky.cs.nil.tt.io.Report reports} will have been logged.
	 * 
	 * @param session the session which has ended
	 * @param gm the agent playing the role of game master in the ended session
	 * @param player the agent playing the role of player in the ended session
	 * @throws Exception if a problem occurs while ending the session
	 */
	protected void onEnd(Session session, Agent gm, Agent player) throws Exception {
		// Log the end of the session.
		String id = log.append(session);
		// Send the end message to both agents.
		End end = new End(id);
		gm.send(end);
		player.send(end);
		// Disconnect both agents.
		gm.close();
		player.close();
		// Remove the agents from the list of active agents.
		playing.remove(gm);
		playing.remove(player);
		// When sessions end, this may free up resources for new sessions, so
		// check for new matches.
		execute(() -> makeMatches());
	}
		
	@Override
	protected void onException(Exception exception) {
		log.append("An uncaught exception caused the server to stop:", exception);
	}
	
	@Override
	protected void onClose() {
		log.append("Server stopped.");
		stopped = true;
	}
	
	@Override
	protected void onStop() throws IOException {
		log.append("Server shut down.");
		log.close();
	}
}