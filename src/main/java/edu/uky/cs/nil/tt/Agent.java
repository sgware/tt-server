package edu.uky.cs.nil.tt;

import java.util.List;

import javax.net.ssl.SSLSocket;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;

import edu.uky.cs.nil.tt.io.*;
import edu.uky.cs.nil.tt.io.Error;
import edu.uky.cs.nil.tt.world.Ending;
import edu.uky.cs.nil.tt.world.State;
import edu.uky.cs.nil.tt.world.Status;
import edu.uky.cs.nil.tt.world.Turn;
import edu.uky.cs.nil.tt.world.WorldModel;
import com.sgware.serialsoc.SerialSocket;

/**
 * An agent represents an individual connection to a {@link Server server} that
 * can server as one of the {@link Role roles} in a {@link Session session}.
 * <p>
 * Every agent is assigned a unique, sequential {@link #id ID number} when it is
 * created. These ID numbers have no special meaning; they exist mainly to
 * identify an agent in system log messages. ID numbers start over at 0 each
 * time the server starts, and an agent's ID number is not preserved in places
 * like the {@link Database database} or {@link Session session log}.
 * <p>
 * An agent also has a {@link #getName() name}, which identifies the type of
 * agent it is. It is possible that multiple agents with the same name can be
 * connected to the server at the same time (if all of those agents are of the
 * same type). To identify a specific agent on the server, use its ID number.
 * 
 * @author Stephen G. Ware
 */
public class Agent extends SerialSocket implements Named {
	
	/** The server which manages this agent */
	public final Server server;
	
	/** A unique, sequential ID number assigned to this agent */
	public final int id;
	
	/** Used to encode and decode network messages as JSON */
	private final Gson gson;
	
	/** Whether the agent is still connected to the server */
	private boolean connected = true;
	
	/**
	 * The agent's name (not necessarily unique), or null if the agent has not
	 * yet sent its join message
	 */
	private String name = null;
	
	/** This agent's session, or null if the agent has not yet been matched */
	private Session session = null;
	
	/**
	 * The story world in which this agent's session takes place, or null if the
	 * agent has not yet been matched
	 */
	private WorldModel world = null;
	
	/**
	 * The role this agent is playing in its session, or null if the agent has
	 * not yet been matched
	 */
	private Role role;
	
	/**
	 * The other agent in this agent's session, or null if the agent has not yet
	 * been matched
	 */
	private Agent partner = null;
	
	/**
	 * The current status of the agent's story world, or null if the agent has
	 * not yet been matched
	 */
	private Status status = null;
	
	/**
	 * The time (in milliseconds since the Epoch) at which this agent will have
	 * waited too long to respond to a choice or to send a report
	 */
	private long timeout = Long.MAX_VALUE;
	
	/** Whether this agent has been sent a {@link Stop stop} message */
	private boolean stopped = false;
	
	/**
	 * Constructs an agent.
	 * 
	 * @param server the server that manages this agent
	 * @param socket the socket this agent uses to send and receive messages
	 * @param id a unique ID number assigned to the agent by the server
	 * @throws Exception if a problem occurs when creating the agent
	 */
	protected Agent(Server server, SSLSocket socket, int id) throws Exception {
		super(server, socket);
		this.id = id;
		this.server = server;
		GsonBuilder builder = new GsonBuilder();
		Message.configure(builder);
		this.gson = builder.create();
	}
	
	@Override
	public String toString() {
		return "agent " + id;
	}
	
	@Override
	public String getName() {
		return name;
	}
	
	/**
	 * Returns true if this agent is still being managed in some way by the
	 * server. This method may continue to return true even after the agent's
	 * socket has been closed if the agent's information might still be needed
	 * by the server. When this method returns false, it means this agent's
	 * socket has been closed and its information is no longer needed.
	 * 
	 * @return true if the agent is still being used in some way by the server
	 */
	public boolean getConnected() {
		return connected;
	}
	
	/**
	 * Returns the session this agent is participating in, or null if this agent
	 * has not yet been matched.
	 * 
	 * @return this agent's session, or null if the agent has not been matched
	 */
	public Session getSession() {
		return session;
	}
	
	/**
	 * Returns the story world model in which this agent's session takes place,
	 * or null if the agent has not yet been matched.
	 * 
	 * @return this agent's story world model, or null if the agent has not yet
	 * been matched
	 */
	public WorldModel getWorld() {
		return world;
	}
	
	/**
	 * Returns the role this agent is playing in its session, or null if the
	 * agent has not yet been matched.
	 * 
	 * @return the agent's role, or null if the agent has not yet been matched
	 */
	public Role getRole() {
		return role;
	}
	
	/**
	 * Returns the other agent in this agent's session, or null if this agent
	 * has not yet been matched.
	 * 
	 * @return this agent's partner, or null if this agent has not yet been
	 * matched
	 */
	public Agent getPartner() {
		return partner;
	}
	
	/**
	 * Returns the {@link Role#GAME_MASTER game master} for this agent's
	 * session (which might be this agent or their partner), or null if this
	 * agent has not yet been matched.
	 * 
	 * @return the game master of this agent's session, or null if this agent
	 * has not yet been matched
	 */
	public Agent getGM() {
		if(getRole() == null)
			return null;
		else if(getRole() == Role.GAME_MASTER)
			return this;
		else
			return getPartner();
	}
	
	/**
	 * Returns the {@link Role#PLAYER player} for this agent's session (which
	 * might be this agent or their partner), or null if this agent has not yet
	 * been matched.
	 * 
	 * @return the player of this agent's session, or null if this agent has not
	 * yet been matched
	 */
	public Agent getPlayer() {
		if(getRole() == null)
			return null;
		else if(getRole() == Role.PLAYER)
			return this;
		else
			return getPartner();
	}
	
	/**
	 * Returns the current status of the story for this agent's session, or null
	 * if this agent has not yet been matched.
	 * 
	 * @return the status of the story for this agent's session, or null if this
	 * agent has not yet been matched
	 */
	public Status getStatus() {
		return status;
	}
	
	/**
	 * Returns the current state of the story world for this agent's session,
	 * as perceived by this agent, or null if this agent has not yet been
	 * matched. If this agent is the session's game master, this method returns
	 * the actual state of the story world. If this agent is the session's
	 * player, this method returns the state the player perceives.
	 * 
	 * @return the state of the story world for this agent's session, or null if
	 * this agent has not yet been matched
	 */
	public State getState() {
		Status status = getStatus();
		if(status == null)
			return null;
		else
			return status.getState();
	}
	
	/**
	 * Returns the current list of turns this agent can take in their session
	 * (which may be empty), or null if this agent has not yet been matched.
	 * 
	 * @return a list of available turns, or null if this agent has not yet been
	 * matched
	 */
	public List<Turn> getChoices() {
		Status status = getStatus();
		if(status == null)
			return null;
		else
			return status.getChoices();
	}
	
	/**
	 * Checks whether this agent has been sent a {@link Stop stop} message yet.
	 * 
	 * @return true if this agent has been sent a stop message
	 */
	public boolean getStopped() {
		return stopped;
	}
	
	@Override
	protected void onConnect() throws Exception {
		String address = socket.getInetAddress().toString();
		if(address.startsWith("/"))
			address = address.substring(1);
		server.log.append("Agent " + id + " connected from IP address " + address + ".");
		Connect connect = new Connect(server.database.getListedWorlds(), server.database.getListedAgents(), server.getAvailable());
		send(connect);
	}
	
	/**
	 * Encodes a message as JSON and sends it to the client via this agent's
	 * socket.
	 * 
	 * @param message the message to send
	 * @throws Exception if a problem occurs while encoding the message or
	 * sending it via the socket
	 */
	public void send(Message message) throws Exception {
		send(gson.toJson(message));
	}
	
	@Override
	protected void receive(String string) throws Exception {
		try {
			Message message = parse(string);
			if(message instanceof Join join)
				onJoin(join);
			else if(message instanceof Choice choice)
				onChoice(choice);
			else if(message instanceof Report report)
				onReport(report);
			else if(message instanceof Stop stop)
				onStop(stop);
			else
				throw new IllegalArgumentException("The message type \"" + message.getClass().getSimpleName() + "\" is not recognized.");
		}
		catch(Exception exception) {
			server.log.append("An error occurred while processing a message from agent " + id + ":", exception);
			send(new Error(exception));
		}
	}
	
	private final Message parse(String string) {
		try {
			Message message = gson.fromJson(string, Message.class);
			message.verify();
			message.setAgent(this);
			return message;
		}
		catch(JsonSyntaxException exception) {
			throw new IllegalArgumentException("The message could not be parsed as a JSON object.", exception);
		}
	}
	
	/**
	 * This method is called each time a {@link Join join} message is received
	 * to establish the agent's identity and preferences. Typically, an agent
	 * should send exactly one join message. If there is a problem processing
	 * the join message, the agent may be notified of the problem, allowing
	 * them to send another. Once an agent has successfully established their
	 * identity, they should not send any further join messages. This method
	 * sets this agent's name. If the agent is requesting a password-protected
	 * name, the password is verified. If everything is in order, this method
	 * {@link Server#onJoin(Join) notifies the server} that a new agent has
	 * joined.
	 * 
	 * @param join the join message containing the agent's identity and
	 * preferences
	 * @throws Exception if a problem occurs while establishing this agent's
	 * identify or preferences
	 * @throws IllegalStateException if the agent has previously established its
	 * identity
	 * @throws IllegalArgumentException if the agent failed to provide the
	 * correct password when a password is needed
	 */
	protected void onJoin(Join join) throws Exception {
		if(getName() != null)
			throw new IllegalStateException("You cannot join the server more than once.");
		Database.AgentEntry entry = server.database.getAgent(join.name);
		if(entry != null && !entry.verify(join.password))
			throw new IllegalArgumentException("The name \"" + join.name + "\" is reserved, but the password you provided was not correct.");
		this.name = join.name;
		server.onJoin(join);
	}
	
	/**
	 * This method is called when the {@link Server server} starts a new {@link
	 * Session session} that includes this agent. Typically, this should only
	 * happen once per session because agents are disconnected after a session
	 * ends. This method establishes things like the story world, this agent's
	 * partner, etc.
	 * 
	 * @param session the new session this agent is participating in
	 * @param gm the agent who is playing the game master role in the session
	 * @param player the agent who is playing the player role in the session
	 * @throws Exception if a problem occurs while the agent is joining the
	 * session
	 * @throws IllegalStateException if the agent has not yet {@link
	 * #onJoin(Join) joined} or if the agent is already part of a different
	 * session
	 */
	protected void onStart(Session session, Agent gm, Agent player) throws Exception {
		if(getName() == null)
			throw new IllegalStateException("You cannot start a session before joining the server.");
		if(getStatus() != null)
			throw new IllegalStateException("You cannot start more than one session.");
		// Determine who is the GM and who is the player.
		if(gm == this) {
			this.role = Role.GAME_MASTER;
			this.partner = player;
		}
		else {
			this.role = Role.PLAYER;
			this.partner = gm;
		}
		this.session = session;
		this.world = (WorldModel) session.world;
		// Start the session.
		send(new Start(this.world, this.role));
		// Set the initial status.
		this.status = world.start(role);
		send(new Update(status));
		// If this agent goes first, mark the timeout time.
		if(this.status.getChoices().size() > 0)
			timeout = System.currentTimeMillis() + Settings.AGENT_TIMEOUT;
	}
	
	/**
	 * This method is called each time a {@link Choice choice} message is
	 * received to take the chosen {@link Turn turn} in the agent's session.
	 * Choices can only be made after a session starts, before it stops, and
	 * when it is this agent's turn. Once a turn is taken, both agent in the
	 * session are {@link #onTurn(Turn) notified}.
	 * 
	 * @param choice the choice that specifies which turn the agent wants to
	 * take
	 * @throws Exception if a problem occurs when taking the chosen turn
	 * @throws IllegalStateException if the agent has not yet begun a session,
	 * if that session has ended, if it is not this agent's turn to act, or if
	 * the index of the choice is out of bounds for the list of choices
	 */
	protected void onChoice(Choice choice) throws Exception {
		List<Turn> choices = getChoices();
		if(choices == null)
			throw new IllegalStateException("You cannot choose actions until the session starts.");
		if(getStopped())
			throw new IllegalStateException("You cannot choose actions after stopping the session.");
		if(session.getResult() != null)
			throw new IllegalStateException("You cannot choose actions after the story has stopped.");
		if(choices.size() == 0)
			throw new IllegalStateException("It is not your turn.");
		if(choice.index < 0 || choice.index >= choices.size())
			throw new IllegalStateException("The choice " + choice.index + " is not allowed right now. You must choose a value from 0 to " + (getStatus().getChoices().size() - 1) + ".");
		// Take the indicated turn.
		Turn turn = choices.get(choice.index);
		server.log.append("Agent " + id + " chose \"" + turn.getDescription() + "\"");
		onTurn(turn);
		getPartner().onTurn(turn);
		session.append(turn, getGM().getState(), getPlayer().getState());
		// Check if the story has ended.
		if(session.getResult() == null) {
			Stop stop = null;
			// Check if the story has reached a pre-defined ending.
			Ending ending = getGM().getStatus().getEnding();
			if(ending != null)
				stop = new Stop(ending);
			// Check if there have been too many passes in a row.
			else if(passLimitReached())
				stop = new Stop("Control has been passed " + Settings.PASS_LIMIT + " times without any progress.");
			if(stop != null) {
				server.log.append("Agent " + id + " session stopped because \"" + stop.message + "\"");
				session.setResult(stop);
				send(stop);
				getPartner().send(stop);
				// Mark the timeout time.
				timeout = System.currentTimeMillis() + Settings.AGENT_TIMEOUT;
				getPartner().timeout = timeout;
			}
		}
	}
	
	/**
	 * This method is called each time a new turn is taken in the this agent's
	 * session. The agent updates their current story status and sends the
	 * relevant information over the socket.
	 * 
	 * @param turn the turn to be taken in the story
	 * @throws Exception if a problem occurs updating the story status
	 * @throws IllegalStateException if the agent's session has not yet started
	 */
	protected void onTurn(Turn turn) throws Exception {
		if(getStatus() == null)
			throw new IllegalStateException("Turns cannot be taken before your session starts.");
		State actual = getGM().getState();
		this.status = getWorld().transition(status, actual, turn);
		send(new Update(this.status));
		// If it is this agent's turn, mark the timeout time.
		if(this.status.getChoices().size() > 0)
			timeout = System.currentTimeMillis() + Settings.AGENT_TIMEOUT;
	}
	
	/**
	 * Checks whether the last {@link Settings#PASS_LIMIT} turns have all been
	 * passes.
	 * 
	 * @return true if all of the last {@link Settings#PASS_LIMIT} turns have
	 * been passes, false otherwise
	 */
	private final boolean passLimitReached() {
		List<Session.Event> events = getSession().getEvents();
		if(events.size() < Settings.PASS_LIMIT)
			return false;
		for(int i = events.size() - Settings.PASS_LIMIT; i < events.size(); i++)
			if(events.get(i).turn == null || events.get(i).turn.type != Turn.Type.PASS)
				return false;
		return true;
	}
	
	/**
	 * This method is called each time a {@link Report report} message is
	 * received to record the agent's responses. Reports can only be received
	 * after a session starts and before this agent either sends a {@link Stop
	 * stop} message or causes the session to stop.
	 * 
	 * @param report the report message containing the details of the item and
	 * reported value
	 * @throws IllegalStateException if the agent's session has not yet started
	 * or has already stopped
	 */
	protected void onReport(Report report) {
		if(getStatus() == null)
			throw new IllegalStateException("You cannot report values until your session starts.");
		if(getStopped())
			throw new IllegalStateException("You cannot report values after stopping your session.");
		server.log.append("Agent " + id + " reported \"" + report.item + "\" as \"" + report.value + "\"" + (report.comment == null ? "" : " with comment \"" + report.comment + "\"") + ".");
		session.append(getRole(), report);
		// Postpone the timeout.
		timeout = System.currentTimeMillis() + Settings.AGENT_TIMEOUT;
	}
	
	/**
	 * This method is called regularly by the Server to check things that are
	 * time-sensative but not triggered by events, such as disconnecting agents
	 * who have taken too long to send a message.
	 * 
	 * @throws Exception if a problem occurs while this method is running
	 */
	protected void tick() throws Exception {
		if(getStatus() != null) {
			// If the session has started, it's this agent's turn, but they haven't made a choice in a long time, stop.
			if(session.getResult() == null && getStatus().getChoices().size() > 0 && System.currentTimeMillis() > timeout)
				onStop(new Stop("The " + (getRole() == Role.GAME_MASTER ? "game master" : "player") + " waited too long to make a choice."));
			// If the session has ended, this agent hasn't sent their stop message yet, but they haven't sent a report in a long time, stop.
			else if(session.getResult() != null && !getStopped() && System.currentTimeMillis() > timeout)
				onStop(new Stop("The " + (getRole() == Role.GAME_MASTER ? "game master" : "player") + " waited too long to report their preceptions of the story."));
		}
	}
	
	/**
	 * This method is called either when a {@link Stop stop} message is received
	 * or when a session is stopped by an agent's partner or the server. A
	 * session can only stop after it has started. Stop messages may be sent to
	 * the agent when the story reaches one of its pre-defined endings, but this
	 * method is not called then. This method is only called when the agent
	 * sends a stop message to the server, when the agent disconnects, or when
	 * the server stops a session. After this method is called, the agent cannot
	 * take more turns or send more {@link #onReport(Report) reports}. So an
	 * agent may send reports after they receive a stop message because the
	 * story reached a pre-defined ending, but they cannot send reports after
	 * they send a stop message to the server. Once both agents have either
	 * acknowledged that the session has stopped or disconnected, this method
	 * {@link Server#onEnd(Session, Agent, Agent) alerts the server} that the
	 * session has ended.
	 * 
	 * @param stop the stop message containing the reason for stopping the
	 * session
	 * @throws Exception if a problem occurs while ending the session
	 * @throws IllegalStateException if the agent's session has not yet started
	 */
	protected void onStop(Stop stop) throws Exception {
		if(getStatus() == null)
			throw new IllegalStateException("You cannot stop a session before it has started.");
		if(stop.getAgent() == this)
			stop = new Stop(getRole());
		if(session.getResult() == null) {
			server.log.append("Agent " + id + " session stopped because \"" + stop.message + "\"");
			session.setResult(stop);
			send(stop);
			getPartner().send(stop);
		}
		if(!stopped) {
			stopped = true;
			if(getPartner().getStopped())
				server.onEnd(session, getGM(), getPlayer());
		}
	}
	
	@Override
	protected void onClose() throws Exception {
		if(getStatus() != null && !getStopped()) {
			if(server.getStopped())
				onStop(new Stop("The session has ended because the server shut down."));
			else
				onStop(new Stop(getRole()));
		}
	}
	
	@Override
	protected void onDisconnect() throws Exception {
		server.log.append("Agent " + id + " disconnected.");
		connected = false;
	}
}