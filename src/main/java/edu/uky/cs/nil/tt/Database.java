package edu.uky.cs.nil.tt;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Random;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import edu.uky.cs.nil.tt.world.LogicalWorld;
import edu.uky.cs.nil.tt.world.WorldModel;

/**
 * A database is a {@link Server server}'s persistent store of information about
 * the {@link edu.uky.cs.nil.tt.world.World story worlds} and {@link Agent
 * agents} available for {@link Session sessions}.
 * <p>
 * All of the worlds and agents in the database are available for all sessions,
 * but {@link Listable entries} either advertise that they exist (listed) or do
 * not advertise that they exist (unlisted). Listed entries are available when
 * a new user connects to browse possible worlds and agents. Unlisted entries
 * must be specifically requested by name.
 * <p>
 * A database is designed to be small and serializable. {@link WorldEntry World
 * entries} do not store the assets or implementation logic of a {@link
 * WorldModel world model} in the database; they simply store a path to the file
 * where the world model is serialized. When a database is deserialized, it
 * should deserialize all world models.
 * <p>
 * {@link AgentEntry Agent entries} can be password protected. This means that
 * an agent cannot use a protected name unless it provides the correct password.
 * This can be used to prevent two different agents from using the same name,
 * either accidentally or on purpose.
 * <p>
 * A database is not thread safe. Behavior is undefined if it is accessed
 * simultaneously by multiple threads.
 * 
 * @author Stephen G. Ware
 */
public class Database {
	
	/**
	 * A database entry represents an individual elements in the database,
	 * including its meta-data, such as its {@link #title title} and {@link
	 * #description description}.
	 * 
	 * @author Stephen G. Ware
	 */
	public static class Entry implements Named {
		
		/**
		 * The unique system {@link Named name} for this element, which is
		 * primarily used internally by the server
		 */
		private final String name;
		
		/**
		 * The title of this element that will be displayed to the participants
		 * or potential participants of a session
		 */
		private String title = null;
		
		/**
		 * A description of this element that will be shown to potential
		 * participants of a session to help them decide if this elements is
		 * something they are interested in
		 */
		private String description = null;
		
		/**
		 * Constructs a database entry with the given meta-data.
		 * 
		 * @param name the system name for this entry
		 * @param title the title for the entry that will be shown to users
		 * @param description the longer description of the entry that will be
		 * shown to users
		 */
		public Entry(String name, String title, String description) {
			Utilities.requireName(name);
			this.name = name;
			this.title = title;
			this.description = description;
		}
		
		/**
		 * Constructs a database entry from only a {@link Named name}.
		 * 
		 * @param name system name for this entry
		 */
		public Entry(String name) {
			this(name, null, null);
		}
		
		/**
		 * Constructs a database entry that copies the meta-data of an existing
		 * database entry.
		 * 
		 * @param other another database entry whose meta-data will be copied
		 */
		public Entry(Entry other) {
			this(other.name, other.title, other.description);
		}
		
		@Override
		public String toString() {
			return "[Entry: \"" + name + "\"]";
		}
		
		@Override
		public String getName() {
			return name;
		}
		
		/**
		 * Returns this entry's title, which is a short name meant to be reader
		 * by human users.
		 * 
		 * @return the title
		 */
		public String getTitle() {
			if(title == null)
				return Utilities.capitalize(getName());
			else
				return title;
		}
		
		/**
		 * Sets the {@link #getTitle() title} of this entry.
		 * 
		 * @param title the new title
		 */
		protected void setTitle(String title) {
			this.title = title;
		}
		
		/**
		 * Returns this entry's description, which is a longer explanation of
		 * what this entity provides meant to help human users decide if they
		 * are interested in this entity.
		 * 
		 * @return the description
		 */
		public String getDescription() {
			if(description == null)
				return getTitle();
			else
				return description;
		}
		
		/**
		 * Sets the {@link #getDescription() description} of this entry.
		 * 
		 * @param description the new description
		 */
		protected void setDescription(String description) {
			this.description = description;
		}
	}
	
	/**
	 * A listable {@link Entry entry} is one that can either be set as clearly
	 * publicly available (listed) or not clearly available (unlisted). Unlisted
	 * elements are still available, but their existence is not not advertised.
	 * 
	 * @author Stephen G. Ware
	 */
	public static class Listable extends Entry {
		
		/** Whether or not this entry is listed */
		private boolean listed = false;
		
		/**
		 * Constructs a listable entry with a given name.
		 * 
		 * @param name system name for this entry
		 */
		public Listable(String name) {
			super(name);
		}
		
		/**
		 * Returns whether this entry should advertise itself as being available
		 * (listed) or whether this entry should not advertise that is is
		 * available (unlisted).
		 * 
		 * @return true is this entry is listed, false otherwise
		 */
		public boolean getListed() {
			return listed;
		}
		
		/**
		 * Sets whether this entry is {@link #getListed() listed}.
		 * 
		 * @param value whether or not this entry should advertise that it is
		 * available
		 */
		protected void setListed(boolean value) {
			this.listed = value;
		}
	}
	
	/**
	 * A {@link Listable database entry} for a {@link
	 * edu.uky.cs.nil.tt.world.World story world}.
	 * 
	 * @author Stephen G. Ware
	 */
	public static class WorldEntry extends Listable {
		
		/** The story world model */
		private transient WorldModel world = null;
		
		/** The URL for the serialized story world model file */
		private String path = null;
		
		/**
		 * Constructs a new world database entry from a world model, using the
		 * {@link WorldModel#getName() world's name}.
		 * 
		 * @param world the world represented in this entry
		 */
		public WorldEntry(WorldModel world) {
			super(world.getName());
			this.world = world;
		}
		
		/**
		 * Returns this entry world's model.
		 * 
		 * @return the world model
		 */
		public WorldModel getWorld() {
			return world;
		}
		
		/**
		 * Sets this entry's {@link #getWorld() world model}. This method does
		 * not update the final {@link #getName() name} field of the entry, so
		 * the world model given should have the same name as this entry.
		 * 
		 * @param world the new world model for this entry
		 */
		protected void setWorld(WorldModel world) {
			this.world = world;
		}
		
		/**
		 * Sets the URL of the file where this entry's world model is
		 * serialized.
		 * 
		 * @param path the path to the world model file
		 */
		protected void setPath(String path) {
			this.path = path;
		}
		
		/**
		 * Deserializes this entry's world model from the {@link #path file}
		 * where the world model is stored.
		 * 
		 * @throws IOException if this entry's path is not set, if the file is
		 * not found, or if there is a problem deserializing the world model
		 */
		protected void read() throws IOException {
			if(path == null)
				throw new IOException("The path to world \"" + world.getName() + "\" is null, so it cannot be read from file.");
			else
				setWorld(LogicalWorld.read(new File(path)));
		}
	}
	
	/**
	 * A {@link Listable database entry} for an {@link Agent agent}.
	 * 
	 * @author Stephen G. Ware
	 */
	public static class AgentEntry extends Listable {
		
		/** The salt used when hashing the agent's password */
		private final String salt;
		
		/** A salted hash of the agent's password */
		private String hash = null;
		
		/**
		 * The maximum number of agents with this name that may be in sessions
		 * simultaneously, or 0 for no limit
		 */
		private int limit = 0;
		
		/**
		 * Constructs a new agent database entry from a name.
		 * 
		 * @param name the system name of the agent
		 */
		public AgentEntry(String name) {
			super(name);
			byte[] salt = new byte[16];
			new SecureRandom().nextBytes(salt);
			this.salt = Base64.getEncoder().encodeToString(salt);
		}
		
		/**
		 * Checks whether a given password matches this agent's password.
		 * Technically, this method checks whether the salted hashes of the
		 * agent's password and the given password match; hash collisions are
		 * unlikely but possible.
		 * 
		 * @param password the password to check against the agent's password
		 * @return true if this agent has no password or if the passwords match,
		 * false otherwise
		 */
		public boolean verify(String password) {
			return this.hash == null || (password != null && this.hash.equals(hash(salt, password)));
		}
		
		/**
		 * Sets this agent's password. The given password can be null to remove
		 * password protection from this agent.
		 * 
		 * @param password the new agent password, or null to remove password
		 * protection
		 */
		public void setPassword(String password) {
			if(password == null)
				this.hash = null;
			else
				this.hash = hash(salt, password);
		}
		
		/**
		 * Returns the maximum number of agents with this entry's name that may
		 * be participating in sessions simultaneously. Agent limits can be used
		 * to prevent agents which require many resources from overwhelming the
		 * server.
		 * 
		 * @return the maximum number of instances of this agent that may be
		 * in sessions simultaneously, or 0 if there is no limit
		 */
		public int getLimit() {
			return limit;
		}
		
		/**
		 * Sets the maximum number of agents with this entry's name that may be
		 * participating in sessions simultaneously.
		 * 
		 * @param limit the maximum number of instances of this agent that may
		 * be in sessions simultaneously, or 0 for no limit
		 */
		public void setLimit(int limit) {
			this.limit = limit;
		}
	}
	
	/**
	 * Returns a salted hash of a given password.
	 * 
	 * @param salt a random string to perturb the hash process
	 * @param password the password
	 * @return a salted hash of the password
	 */
	private static final String hash(String salt, String password) {
		Utilities.requireNonNull(password, "password");
		try {
			MessageDigest hash = MessageDigest.getInstance("SHA-512");
			hash.update(Base64.getDecoder().decode(salt));
			byte[] bytes = password.getBytes(StandardCharsets.UTF_8);
			bytes = hash.digest(bytes);
			return Base64.getEncoder().encodeToString(bytes);
		}
		catch(NoSuchAlgorithmException exception) {
			throw new RuntimeException(exception);
		}
	}
	
	/** The log to which important status messages will be written */
	private transient final Log log;
	
	/** A map of world entries by name */
	private final LinkedHashMap<String, WorldEntry> worlds;
	
	/** A map of agent entries by name */
	private final LinkedHashMap<String, AgentEntry> agents;
	
	/** The file to which this database should be serialized */
	private transient final File file;
	
	/**
	 * Constructs a new empty database or deserializes it from a file.
	 * 
	 * @param file the file in which the database is serialized, or null if an
	 * empty database should be created
	 * @param log the log to which important status messages will be written
	 * as the database is created and deserialized
	 * @throws IOException if the file cannot be found or if a problem occurs
	 * when deserializing the database
	 */
	public Database(File file, Log log) throws IOException {
		this.log = log;
		if(file == null) {
			worlds = new LinkedHashMap<>();
			agents = new LinkedHashMap<>();
			Log.append("Created a new database that will not be written to file.", log);
		}
		else if(!file.exists()) {
			worlds = new LinkedHashMap<>();
			agents = new LinkedHashMap<>();
			Log.append("Created a new database that will be written to \"" + file.getPath() + "\".", log);
		}
		else {
			try(BufferedReader in = new BufferedReader(new FileReader(file))) {
				Database database = new GsonBuilder().create().fromJson(in, Database.class);
				this.worlds = database.worlds;
				this.agents = database.agents;
			}
			for(WorldEntry world : this.worlds.values()) {
				try {
					world.read();
				}
				catch(Exception exception) {
					Log.append("An error occurred while reading world \"" + world.getName() + "\" from file:", exception, log);
				}
			}
			Log.append("Read database \"" + file.getPath() + "\".", log);
		}
		this.file = file;
	}
	
	/**
	 * Returns a list of {@link Entry entries} for all {@link
	 * Listable#getListed() listed} worlds in this database.
	 * 
	 * @return a list of worlds that are advertised as available
	 */
	public List<Entry> getListedWorlds() {
		ArrayList<Entry> list = new ArrayList<>();
		for(WorldEntry entry : worlds.values())
			if(entry.getListed())
				list.add(new Entry(entry));
		return list;
	}
	
	/**
	 * Returns the entry for the world model with the given name or null if
	 * the database has no world model with that name. This method may return
	 * an {@link Listable#getListed() unlisted} world.
	 * 
	 * @param name the name of the world model whose entry is desired
	 * @return the entry for the world model with that name, or null if the
	 * database has no entry for a world with that name
	 */
	public WorldEntry getWorld(String name) {
		return worlds.get(name);
	}
	
	/**
	 * Returns the entry for the world model with the given name or throws an
	 * exception if no such entry exists.
	 * 
	 * @param name the name of the world model whose entry is desired
	 * @return the entry for the world model with that name
	 * @throws IllegalArgumentException if there is no entry for a world model
	 * with that name
	 */
	public WorldEntry requireWorld(String name) {
		WorldEntry entry = getWorld(name);
		if(entry == null)
			throw new IllegalArgumentException("World \"" + name + "\" is not defined.");
		else
			return entry;
	}
	
	/**
	 * Returns a randomly chosen {@link Listable#getListed() listed} world
	 * entry.
	 * 
	 * @return a random listed world entry
	 * @throws IllegalStateException if the database contains no listed world
	 * entries
	 */
	public WorldEntry getRandomWorld() {
		List<Entry> listed = getListedWorlds();
		if(listed.size() == 0)
			throw new IllegalStateException("There are no worlds available.");
		int index = new Random().nextInt(listed.size());
		return requireWorld(listed.get(index).getName());
	}
	
	/**
	 * Adds a new world entry to the database for a given world and writes the
	 * database to file. If the database already contains an entry for a world
	 * with the given world's name, the old entry is replaced.
	 * 
	 * @param world the new world model to add to the database
	 */
	public void addWorld(WorldModel world) {
		addWorld(world, null);
	}
	
	/**
	 * Deserializes a world model from a given file, {@link
	 * #addWorld(WorldModel) adds} the world model to the database, and writes
	 * the database to file.
	 * 
	 * @param file a file that contains a serialized world model
	 * @throws IOException if the file does not exist or if there is a problem
	 * reading the world model from the file
	 */
	public void addWorld(File file) throws IOException {
		WorldModel world = LogicalWorld.read(file);
		String path = file.getPath().replace("\\", "/");
		addWorld(world, path);
	}
	
	private void addWorld(WorldModel world, String path) {
		modify(() -> {
			WorldEntry entry = worlds.get(world.getName());
			if(entry == null) {
				entry = new WorldEntry(world);
				worlds.put(world.getName(), entry);
				Log.append("Added world \"" + world.getName() + "\" to database.", log);
			}
			else {
				entry.setWorld(world);
				Log.append("Updated world \"" + world.getName() + "\".", log);
			}
			entry.setPath(path);
		});
	}
	
	/**
	 * Sets the {@link Entry#getTitle() title} of a world model's entry and
	 * writes the database to file.
	 * 
	 * @param name the system name of the world model
	 * @param title the new title for the world model
	 * @throws IllegalArgumentException if there is no entry for a world model
	 * with that name
	 */
	public void setWorldTitle(String name, String title) {
		modify(() -> {
			requireWorld(name).setTitle(title);
			Log.append("World \"" + name + "\" title set to \"" + title + "\".", log);
		});
	}
	
	/**
	 * Sets the {@link Entry#getDescription() description} of a world model's
	 * entry and writes the database to file.
	 * 
	 * @param name the system name of the world model
	 * @param description the new description for the world model
	 * @throws IllegalArgumentException if there is no entry for a world model
	 * with that name
	 */
	public void setWorldDescription(String name, String description) {
		modify(() -> {
			requireWorld(name).setDescription(description);
			Log.append("World \"" + name + "\" description set to \"" + description + "\".", log);
		});
	}
	
	/**
	 * Sets whether a given world model's entry is {@link Listable#getListed()
	 * listed} and writes the database to file.
	 * 
	 * @param name the system name of the world model
	 * @param value true if the world should be listed, false if it should not
	 * be listed
	 * @throws IllegalArgumentException if there is no entry for a world model
	 * with that name
	 */
	public void setWorldListed(String name, boolean value) {
		modify(() -> {
			requireWorld(name).setListed(value);
			if(value)
				Log.append("World \"" + name + "\" will be publically listed.", log);
			else
				Log.append("World \"" + name + "\" will not be publically listed.", log);
		});
	}
	
	/**
	 * Removes the entry from this database for the world model with the given
	 * name (if one exists) and writes the database to file.
	 * 
	 * @param name the system name of the world model
	 * @throws IllegalArgumentException if there is no entry for a world model
	 * with that name
	 */
	public void removeWorld(String name) {
		modify(() -> {
			requireWorld(name);
			worlds.remove(name);
			Log.append("World \"" + name + "\" removed from database.", log);
		});
	}
	
	/**
	 * Returns a list of {@link Entry entries} for all {@link
	 * Listable#getListed() listed} agents in this database.
	 * 
	 * @return a list of agents that are advertised as available
	 */
	public List<Entry> getListedAgents() {
		ArrayList<Entry> list = new ArrayList<>();
		for(AgentEntry entry : agents.values())
			if(entry.getListed())
				list.add(new Entry(entry));
		return list;
	}
	
	/**
	 * Returns the entry for the agent with the given name or null if the
	 * database has no agent that name. This method may return an {@link
	 * Listable#getListed() unlisted} agent.
	 * 
	 * @param name the name of the agent whose entry is desired
	 * @return the entry for the agent with that name, or null if the database
	 * has no entry for an agent with that name
	 */
	public AgentEntry getAgent(String name) {
		return agents.get(name);
	}
	
	/**
	 * Returns the entry for the agent with the given name or throws an
	 * exception if no such entry exists.
	 * 
	 * @param name the name of the agent whose entry is desired
	 * @return the entry for the agent with that name
	 * @throws IllegalArgumentException if there is no entry for an agent with
	 * that name
	 */
	public AgentEntry requireAgent(String name) {
		AgentEntry entry = getAgent(name);
		if(entry == null)
			throw new IllegalArgumentException("Agent \"" + name + "\" is not defined.");
		else
			return entry;
	}
	
	/**
	 * Adds a new agent entry to the database for a given agent name and writes
	 * the database to file. If the database already contains an entry for an
	 * agent with the given name, the entry is unchanged.
	 * 
	 * @param name the name of the agent to add to the database
	 */
	public void addAgent(String name) {
		modify(() -> {
			AgentEntry entry = agents.get(name);
			if(entry == null) {
				entry = new AgentEntry(name);
				agents.put(name, entry);
				Log.append("Agent \"" + name + "\" added to database.", log);
			}
		});
	}
	
	/**
	 * Sets the {@link Entry#getTitle() title} of an agent's entry and writes
	 * the database to file.
	 * 
	 * @param name the system name of the agent
	 * @param title the new title for the agent
	 * @throws IllegalArgumentException if there is no entry for an agent with
	 * that name
	 */
	public void setAgentTitle(String name, String title) {
		modify(() -> {
			requireAgent(name).setTitle(title);
			Log.append("Agent \"" + name + "\" title set to \"" + title + "\".", log);
		});
	}
	
	/**
	 * Sets the {@link Entry#getDescription() description} of an agent's entry
	 * and writes the database to file.
	 * 
	 * @param name the system name of the agent
	 * @param description the new description for the agent
	 * @throws IllegalArgumentException if there is no entry for an agent with
	 * that name
	 */
	public void setAgentDescription(String name, String description) {
		modify(() -> {
			requireAgent(name).setDescription(description);
			Log.append("Agent \"" + name + "\" description set to \"" + description + "\".", log);
		});
	}
	
	/**
	 * Sets the {@link AgentEntry#setPassword(String) password} of an agent's
	 * entry and writes the database to file. The password will be salted and
	 * hashed before it is stored.
	 * 
	 * @param name the system name of the agent
	 * @param password the new password for the agent
	 * @throws IllegalArgumentException if there is no entry for an agent with
	 * that name
	 */
	public void setAgentPassword(String name, String password) {
		modify(() -> {
			requireAgent(name).setPassword(password);
			Log.append("Agent \"" + name + "\" password updated.", log);
		});
	}
	
	/**
	 * Sets the {@link AgentEntry#setLimit(int) limit} on how many agents with
	 * the given name may be in sessions simultaneously and writes the database
	 * to file.
	 * 
	 * @param name the system name of the agent
	 * @param limit the maximum number of agents with the given name which may
	 * be running simultaneously, or 0 for no limit
	 * @throws IllegalArgumentException if there is no entry for an agent with
	 * that name
	 */
	public void setAgentLimit(String name, int limit) {
		modify(() -> {
			requireAgent(name).setLimit(limit);
			Log.append("Agent \"" + name + "\" limit set to " + limit + ".", log);
		});
	}
	
	/**
	 * Sets whether a given agent's entry is {@link Listable#getListed() listed}
	 * and writes the database to file.
	 * 
	 * @param name the system name of the agent
	 * @param value true if the agent should be listed, false if it should not
	 * be listed
	 * @throws IllegalArgumentException if there is no entry for an agent with
	 * that name
	 */
	public void setAgentListed(String name, boolean value) {
		modify(() -> {
			requireAgent(name).setListed(value);
			if(value)
				Log.append("Agent \"" + name + "\" will be publically listed.", log);
			else
				Log.append("Agent \"" + name + "\" will not be publically listed.", log);
		});
	}
	
	/**
	 * Removes the entry from this database for the agent with the given name
	 * (if one exists) and writes the database to file.
	 * 
	 * @param name the system name of the agent
	 * @throws IllegalArgumentException if there is no entry for an agent with
	 * that name
	 */
	public void removeAgent(String name) {
		modify(() -> {
			requireAgent(name);
			agents.remove(name);
			Log.append("Agent \"" + name + "\" removed from database.", log);
		});
	}
	
	private final void modify(Runnable runnable) {
		runnable.run();
		try {
			write();
		}
		catch(IOException exception) {
			Log.append("An error occurred while writing the database to file:", exception, log);
		}
	}
	
	/**
	 * Writes this database to file. The file to which it is written is set
	 * in the database's constructor. If this database is temporary (i.e. never
	 * written to file), this method does nothing.
	 * 
	 * @throws IOException if a problem occurs while writing the database to
	 * file
	 */
	public void write() throws IOException {
		if(file == null)
			return;
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		try(BufferedWriter out = new BufferedWriter(new FileWriter(file))) {
			gson.toJson(this, out);
		}
	}
}