package edu.uky.cs.nil.tt.io;

import edu.uky.cs.nil.tt.Utilities;

/**
 * The report message is sent from an {@link edu.uky.cs.nil.tt.Agent agent} to
 * the {edu.uky.cs.nil.tt.Server server} to indicate the value of a question on
 * a survey about the session they are playing.
 * 
 * @author Stephen G. Ware
 */
public class Report extends Message {
	
	/** The name or ID of the survey question */
	public final String item;
	
	/** The answer the agent gives to the survey question */
	public final String value;
	
	/**
	 * A comment provided by the agent explaining their answer, or null if the
	 * question does not allow comments or the agent does not provide one
	 */
	public final String comment;
	
	/**
	 * Constructs a new report message.
	 * 
	 * @param item the name or ID of the survey question
	 * @param value the answer the agent gives to the question
	 * @param comment a comment explaining the answer, or null
	 */
	public Report(String item, String value, String comment) {
		this.item = item;
		this.value = value;
		this.comment = comment;
	}
	
	@Override
	public String toString() {
		return "[Report Message: \"" + item + "\"=\"" + value + "\"" + (comment == null ? "" : "; \"" + comment + "\"") + "]";
	}
	
	@Override
	public void verify() {
		Utilities.requireNonNull(item, "item");
		Utilities.requireNonNull(value, "value");
	}
}