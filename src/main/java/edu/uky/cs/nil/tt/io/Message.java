package edu.uky.cs.nil.tt.io;

import com.google.gson.GsonBuilder;

import edu.uky.cs.nil.tt.Agent;
import edu.uky.cs.nil.tt.world.Expression;
import edu.uky.cs.nil.tt.world.World;

/**
 * A message is information sent to or from a {@link edu.uky.cs.nil.tt.Server
 * server} in JSON format.
 * 
 * @author Stephen G. Ware
 */
public abstract class Message {
	
	/**
	 * Configures a {@link GsonBuilder} to encode and decode {@link Message}
	 * objects as JSON.
	 * 
	 * @param builder the GSON builder to configure
	 */
	public static void configure(GsonBuilder builder) {
		// Read and write the various subclasses of Expression.
		Expression.configure(builder);
		// Read and write the various subclasses of Message.
		AbstractAdapter<Message> adapter = new AbstractAdapter<>(Message.class);
		builder.registerTypeAdapterFactory(adapter);
		builder.registerTypeAdapter(Message.class, adapter);
		// Serialize all implementations of worlds as generic World objects.
		builder.registerTypeAdapterFactory(new GenericAdapter<>(World.class));
	}
	
	/**
	 * The agent who sent the message, if this message was sent to the server,
	 * or null if this message was sent from the server.
	 */
	private transient Agent agent = null;
	
	/**
	 * Constructs a message.
	 */
	public Message() {
		// Do nothing.
	}
	
	/**
	 * Returns the {@link Agent} who sent this message, or null if this message
	 * was sent by the server.
	 * 
	 * @return the agent who sent the message, or null
	 */
	public Agent getAgent() {
		return agent;
	}
	
	/**
	 * Sets the {@link Agent} who sent this message. For messages sent to the
	 * server, this method should be called soon after the message has been
	 * parsed.
	 * 
	 * @param agent the agent who sent this message
	 */
	public void setAgent(Agent agent) {
		this.agent = agent;
	}
	
	/**
	 * Checks that this message is correctly configured and throws an exception
	 * if not. This method should be called soon after the message has been
	 * parsed. It should check that all the necessary fields are set and that
	 * their values are legal values. 
	 */
	public abstract void verify();
}