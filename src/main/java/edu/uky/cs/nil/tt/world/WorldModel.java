package edu.uky.cs.nil.tt.world;

import java.util.Collection;

import edu.uky.cs.nil.tt.Role;

/**
 * A world model is an implementation of a {@link World story world} that
 * contains the necessary logic to simulate how {@link Turn turns} change the
 * story state. World models are not meant to be transmitted to the participants
 * of a session. Their logic is meant to be simulated by the {@link
 * edu.uky.cs.nil.tt.Server server} only.
 * 
 * @author Stephen G. Ware
 */
public abstract class WorldModel extends World {
	
	/**
	 * Constructs a story world model with the assets needed to construct a
	 * {@link World story world}.
	 * 
	 * @param name a unique name for the story world
	 * @param entities a collection of objects that exist in the world
	 * @param variables a collection of variables whose values can change and
	 * whose current values reflect the current state of the world
	 * @param actions a collection of ways the world state can change
	 * @param endings a collection of ways a story in this world can end
	 * @throws IllegalArgumentException if the provided assets do not meet the
	 * required format, such as two assets of the same type having the same name
	 */
	public WorldModel(
		String name,
		Collection<Entity> entities,
		Collection<Variable> variables,
		Collection<Action> actions,
		Collection<Ending> endings
	) {
		super(name, entities, variables, actions, endings);
	}
	
	/**
	 * Constructs a story world model from the assets defined in an asset
	 * builder.
	 * 
	 * @param builder the asset builder that defines the assets to be used in
	 * this story world
	 * @throws IllegalArgumentException if the provided assets do not meet the
	 * required format, such as two assets of the same type having the same name
	 */
	public WorldModel(AssetBuilder builder) {
		this(
			builder.name,
			builder.entities,
			builder.variables,
			builder.actions,
			builder.endings
		);
	}
	
	/**
	 * Constructs a story world model using the assets defined in an existing
	 * world.
	 * 
	 * @param world the world whose assets will be copied
	 */
	public WorldModel(World world) {
		this(
			world.name,
			world.getEntities(),
			world.getVariables(),
			world.getActions(),
			world.getEndings()
		);
	}
	
	/**
	 * Returns the initial state of the story world for a given role as a status
	 * object. For the {@link Role#GAME_MASTER game master}, the initial status
	 * should completely and accurately define the initial state of all
	 * variables and entities. For the {@link Role#PLAYER player}, the initial
	 * status should should define the initial state of all variables and
	 * entities that the player knows about at the start of the story. The
	 * initial status should have no {@link Status#getHistory() history}.
	 * 
	 * @param role the role whose initial status is desired
	 * @return the initial status of the story world for that role
	 */
	public abstract Status start(Role role);
	
	/**
	 * Given the current status of a story world (actual or perceived), the
	 * actual current status of a story world, and a turn, this method simulates
	 * that turn and returns the new perceived status of the story world. The
	 * status returned will be for the same {@link Status#role role as the first
	 * status argument}. Since the {@link Role#GAME_MASTER game master} should
	 * always know the actual status of the story world, when calculating a new
	 * status for the game master, the first two status arguments should be the
	 * same. When calculating a new status for the {@link Role#PLAYER player},
	 * the first status argument should be the player's perceived status and the
	 * second should be the actual status available to the game master. The
	 * status returned will be the new status as perceived by that role (the new
	 * actual status for the game master or the new perceived status for the
	 * player).
	 * 
	 * @param status the current status of the story world as perceived by the
	 * status' participant
	 * @param actual the actual current status of the story world
	 * @param turn the turn to be taken
	 * @return the new story world status as perceived by the status'
	 * participant
	 */
	public abstract Status transition(Status status, State actual, Turn turn);
}