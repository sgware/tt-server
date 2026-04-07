package edu.uky.cs.nil.tt.io;

import edu.uky.cs.nil.tt.Utilities;

/**
 * The choice message is sent from an {@link edu.uky.cs.nil.tt.Agent agent} to
 * the {edu.uky.cs.nil.tt.Server server} to signal what turn the agent wants
 * to take.
 * 
 * @author Stephen G. Ware
 */
public class Choice extends Message {
	
	/**
	 * The index of a turn from the most recent {@link Update status update}
	 * sent to the agent, starting at 0
	 */
	public final int index;
	
	/**
	 * Constructs a choice message for a given choice index.
	 * 
	 * @param index the index of the turn from the agent's most recent status
	 * update that the agent wants to take
	 */
	public Choice(int index) {
		this.index = index;
	}
	
	@Override
	public String toString() {
		String string = "[Choice Message:";
		if(getAgent() != null)
			string += " \"" + getAgent().getName() + "\"";
		string += " " + index;
		return string + "]";
	}
	
	@Override
	public void verify() {
		Utilities.requireNonNegative(index, "choice");
	}
}