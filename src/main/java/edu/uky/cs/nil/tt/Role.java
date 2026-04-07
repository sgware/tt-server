package edu.uky.cs.nil.tt;

/**
 * Represents the two roles in a collaborative storytelling {@link Session
 * session}: the Player, who typically controls a single character, and the
 * Game Master, who controls all other characters and the environment.
 * 
 * @author Stephen G. Ware
 */
public enum Role {
	
	/**
	 * The game master role, who controls all characters except the player
	 * character and the environment
	 */
	GAME_MASTER,
	
	/**
	 * The player role, who controls one characters, often the main character
	 * of the story
	 */
	PLAYER;
	
	/**
	 * Returns the partner of this role. If this role is the game master, this
	 * method returns the player. If this role is the player, this method
	 * return the game master.
	 * 
	 * @return the partner of this role
	 */
	public Role getPartner() {
		if(this == GAME_MASTER)
			return PLAYER;
		else
			return GAME_MASTER;
	}
}