package edu.uky.cs.nil.tt.world;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import edu.uky.cs.nil.tt.Named;
import edu.uky.cs.nil.tt.Role;
import edu.uky.cs.nil.tt.Utilities;

/**
 * A tool for generating English descriptions of certain objects based on
 * natural language templates. A describer has basic rules for conjugating verbs
 * and replacing parts of sentences with the names or descriptions of objects.
 * It is designed to be easy to serialize.
 * <p>
 * A template is a string, but with certain key phrases wrapped in curly braces
 * that will be replaced with values. When the name of a {@link Key key} appears
 * in curly braces, it will be replaced with the description of the value of
 * that key. When the {@link Verb#base base form of a verb} appears in curly
 * braces, the verb will be replaced with the {@link Verb#third third person}
 * form of the verb, unless the word {@code "you"} followed by a space appears
 * immediately before the verb, in which case the verb will be replaced by the
 * {@link Verb#second second person} form of the verb.
 * <p>
 * Here are some recommendations for how objects should be described:
 * <ul>
 * <li>{@link Action}s should be described as turns that succeed.</li>
 * <li>Turns that {@link Turn.Type#SUCCEED succeed} should be described in third
 * person present tense to the {@link Role#GAME_MASTER game master} and second
 * person present tense to the {@link Role#PLAYER player}.</li>
 * <li>Turns that {@link Turn.Type#PROPOSE propose} an action should be
 * described in present tense as a character under the control of the proposer
 * trying or offering to do something.</li>
 * <li>Turns the {@link Turn.Type#FAIL fail} should be described in present
 * tense as the proposing character attempting to do the action but failing or
 * as a character until the control of the other role refusing an offer.</li>
 * </ul>
 * 
 * @author Stephen G. Ware
 */
public class Describer {
	
	/** The names of elements which can be replaced in description templates */
	public enum Key {
		
		/**
		 * The type of object being described. The value should be a string in
		 * lower case. When the type is a Java class, it should be {@link
		 * Class#getSimpleName() the class's simple name only}, not the fully
		 * qualified name including its package.
		 */
		TYPE,
		
		/** The {@link Turn#role role who takes the turn} being described */
		ROLE,
		
		/** The {@link Turn.Type type of turn} object being described */
		TURN_TYPE,
		
		/** The entity being described */
		ENTITY,
		
		/**
		 * The variable being described or the {@link Assignment#variable
		 * variable of the assignment} being described
		 */
		VARIABLE,
		
		/**
		 * The action being described or the {@link Turn#action action of the
		 * turn} being described
		 */
		ACTION,
		
		/** The ending being described */
		ENDING,
		
		/**
		 * The name of the {@link Named named object} being described or the
		 * {@link Signature#name name part of the signature} of the object being
		 * described if the object is a {@link SignedAsset signed asset}
		 */
		NAME,
		
		/**
		 * The first {@link Signature#getArguments() argument of the signature}
		 * of the object being described
		 */
		ARG_0,
		
		/**
		 * The second {@link Signature#getArguments() argument of the signature}
		 * of the object being described
		 */
		ARG_1,
		
		/**
		 * The third {@link Signature#getArguments() argument of the signature}
		 * of the object being described
		 */
		ARG_2,
		
		/**
		 * The fourth {@link Signature#getArguments() argument of the signature}
		 * of the object being described
		 */
		ARG_3,
		
		/**
		 * The fifth {@link Signature#getArguments() argument of the signature}
		 * of the object being described
		 */
		ARG_4,
		
		/**
		 * The sixth {@link Signature#getArguments() argument of the signature}
		 * of the object being described
		 */
		ARG_5,
		
		/**
		 * The seventh {@link Signature#getArguments() argument of the
		 * signature} of the object being described
		 */
		ARG_6,
		
		/**
		 * The eighth {@link Signature#getArguments() argument of the signature}
		 * of the object being described
		 */
		ARG_7,
		
		/**
		 * The ninth {@link Signature#getArguments() argument of the signature}
		 * of the object being described
		 */
		ARG_8,
		
		/**
		 * The tenth {@link Signature#getArguments() argument of the signature}
		 * of the object being described
		 */
		ARG_9,
		
		/**
		 * The {@link Assignment#value value of the assignment} being described
		 */
		VALUE,
		
		/**
		 * The {@link Role role} for whom this description is being generated
		 */
		TO
	}
	
	/** An array of argument templates in order by index */
	private static final Key[] ARG_KEYS = new Key[] { Key.ARG_0, Key.ARG_1, Key.ARG_2, Key.ARG_3, Key.ARG_4, Key.ARG_5, Key.ARG_6, Key.ARG_7, Key.ARG_8, Key.ARG_9 };
	
	/**
	 * A verb is an object used in the templates of a {@link Describer
	 * describer}. It has three parts: a base, usually the infinitive of the
	 * verb minus the word "to", the verb conjugated in second person, and the
	 * verb conjugated in third person.
	 * 
	 * @author Stephen G. Ware
	 */
	public static class Verb {
		
		/**
		 * A basic, non-conjugated form of the verb, usually the infinitive
		 * minus the word "to", for example "be"
		 */
		public final String base;
		
		/**
		 * The verb conjugated in second person present tense, for example "are"
		 */
		public final String second;
		
		/**
		 * The verb conjugated in the third person present tense, for example
		 * "is"
		 */
		public final String third;
		
		/**
		 * Constructs a new verb with its base, second person, and third person
		 * forms.
		 * 
		 * @param base a basic, non-conjugated form of the verb, usually the
		 * infinitive minus the word "to" (for example "be")
		 * @param second the verb conjugated in second person present tense (for
		 * example "are")
		 * @param third the verb conjugated in the third person present tense
		 * (for example "is")
		 */
		public Verb(String base, String second, String third) {
			this.base = base;
			this.second = second;
			this.third = third;
		}
		
		/**
		 * Constructs a verb from its second person and third person forms,
		 * using the second person form as the base.
		 * 
		 * @param second the verb conjugated in second person present tense (for
		 * example "eat")
		 * @param third the verb conjugated in the third person present tense
		 * (for example "eats")
		 */
		public Verb(String second, String third) {
			this(second, second, third);
		}
		
		@Override
		public String toString() {
			return base;
		}
	}
	
	/**
	 * A {@link Describer describer} rule has two parts: a context that defines
	 * when to apply it and a natural language template to use when this rule is
	 * applied.
	 * <p>
	 * Rules can be constructed directly using {@link
	 * Describer.Rule#Rule(Map, String)}. Alternatively, they can be constructed
	 * without a context using {@link Describer.Rule#Rule(String)} or {@link
	 * Describer.Rule#Rule()}, and then the context can be built up by calling
	 * a sequence of methods to add to the context, such as {@link
	 * Describer.Rule#when(Key, Object)}.
	 * 
	 * @author Stephen G. Ware
	 */
	public static class Rule {
		
		/**
		 * A map of keys to values that define when this rule should be applied
		 */
		private final Map<Key, String> context;
		
		/**
		 * The natural language template to be used when this rule is applied
		 */
		public final String template;
		
		/**
		 * Constructs a new rule from a context map and a natural language
		 * template.
		 * 
		 * @param context a map of keys to values which defines when to apply
		 * this rule
		 * @param template a natural language template to use when this rule is
		 * applied
		 */
		protected Rule(Map<Key, String> context, String template) {
			this.context = context;
			this.template = template;
		}
		
		/**
		 * Constructs a new rule with a template and no context. A rule with no
		 * context is typically revised by a series of calls to the methods that
		 * add to its context.
		 * 
		 * @param template a natural language template to use when this rule is
		 * applied
		 */
		public Rule(String template) {
			this(Map.of(), template);
		}
		
		/**
		 * Constructs a new empty rule with no template and no context. A rule
		 * with no context is typically revised by a series of calls to the
		 * methods that add to its context and a call to {@link
		 * Describer.Rule#write(String)} to set the template.
		 */
		public Rule() {
			this(null);
		}
		
		@Override
		public String toString() {
			return (context.size() == 0 ? "" : context + " ") + "->" + (template == null ? "" : " \"" + template + "\"");
		}
		
		/**
		 * Tests whether the context of this rule matches the context of the
		 * object a describer is currently trying to describe. A rule is applied
		 * when the values of all the keys in the rule's context match the
		 * values of those keys for the object being described. The rule context
		 * does not need to be an exact match to the object's context--only the
		 * keys defined in the rule's context are checked. Keys that have values
		 * in the object's context but are not defined in the rule's context are
		 * not checked and do not prevent a rule from being applied.
		 * 
		 * @param context the context of the object being described
		 * @return true if all of the keys defined in this rule's context match
		 * the values of those keys in the given context
		 */
		public boolean matches(Map<Key, Object> context) {
			for(Map.Entry<Key, String> entry : this.context.entrySet())
				if(!Objects.equals(entry.getValue(), Objects.toString(context.get(entry.getKey()))))
					return false;
			return true;
		}
		
		/**
		 * Returns a new rule that is identical to this rule, except with the
		 * requirement that the given key must have the given value for this
		 * rule to be applied by a describer.
		 * 
		 * @param key the key whose value will be constrained
		 * @param value the value that key must have for this rule to be applied
		 * @return a new rule reflecting the updated context
		 */
		public Rule when(Key key, Object value) {
			if(value instanceof Class)
				value = ((Class<?>) value).getSimpleName().toLowerCase();
			LinkedHashMap<Key, String> context = new LinkedHashMap<>();
			this.context.forEach((k, v) -> context.put(k, v));
			context.put(key, Objects.toString(value));
			return new Rule(Collections.unmodifiableMap(context), template);
		}
		
		/**
		 * Returns a new rule identical to this one, except that {@link
		 * Key#TYPE} must have the given value. See {@link Key#TYPE}.
		 * 
		 * @param type the value the key must have for this rule to be applied
		 * @return a new rule reflecting the updated context
		 */
		public Rule typeIs(Class<?> type) {
			return when(Key.TYPE, type);
		}
		
		/**
		 * Returns a new rule identical to this one, except that {@link
		 * Key#ROLE} must have the given value.
		 * 
		 * @param role the value the key must have for this rule to be applied
		 * @return a new rule reflecting the updated context
		 */
		public Rule roleIs(Role role) {
			return when(Key.ROLE, role);
		}
		
		/**
		 * Returns a new rule identical to this one, except that {@link
		 * Key#TURN_TYPE} must have the given value.
		 * 
		 * @param type the value the key must have for this rule to be applied
		 * @return a new rule reflecting the updated context
		 */
		public Rule turnIs(Turn.Type type) {
			return when(Key.TURN_TYPE, type);
		}
		
		/**
		 * Returns a new rule identical to this one, except that {@link
		 * Key#ENTITY} must have the given value.
		 * 
		 * @param entity the value the key must have for this rule to be applied
		 * @return a new rule reflecting the updated context
		 */
		public Rule entityIs(Entity entity) {
			return when(Key.ENTITY, entity);
		}
		
		/**
		 * Returns a new rule identical to this one, except that {@link
		 * Key#NAME} must have the value of the name of the given signed asset,
		 * and each of the {@link Key#ARG_0 argument keys} must have the values
		 * of the corresponding arguments in the signature of the given signed
		 * asset.
		 * 
		 * @param signed the signed asset whose name and arguments will be used
		 * to set the corresponding keys
		 * @return a new rule reflecting the updated context
		 */
		private final Rule signed(SignedAsset signed) {
			Rule rule = nameIs(signed.signature.name);
			int length = Math.min(ARG_KEYS.length, signed.signature.getArguments().size());
			for(int i = 0; i < length; i++)
				rule = rule.when(ARG_KEYS[i], signed.signature.getArguments().get(i));
			return rule;
		}
		
		/**
		 * Returns a new rule identical to this one, except that {@link
		 * Key#VARIABLE} must have the given value.
		 * 
		 * @param variable the value the key must have for this rule to be
		 * applied
		 * @return a new rule reflecting the updated context
		 */
		public Rule variableIs(Variable variable) {
			return when(Key.VARIABLE, variable).signed(variable);
		}
		
		/**
		 * Returns a new rule identical to this one, except that {@link
		 * Key#ACTION} must have the given value.
		 * 
		 * @param action the value the key must have for this rule to be applied
		 * @return a new rule reflecting the updated context
		 */
		public Rule actionIs(Action action) {
			return when(Key.ACTION, action).signed(action);
		}
		
		/**
		 * Returns a new rule identical to this one, except that {@link
		 * Key#ENDING} must have the given value.
		 * 
		 * @param ending the value the key must have for this rule to be applied
		 * @return a new rule reflecting the updated context
		 */
		public Rule endingIs(Ending ending) {
			return when(Key.ENDING, ending).signed(ending);
		}
		
		/**
		 * Returns a new rule identical to this one, except that {@link
		 * Key#NAME} must have the given value.
		 * 
		 * @param name the value the key must have for this rule to be applied
		 * @return a new rule reflecting the updated context
		 */
		public Rule nameIs(String name) {
			return when(Key.NAME, name);
		}
		
		/**
		 * Returns a new rule identical to this one, except that {@link
		 * Key#VALUE} must have the given value.
		 * 
		 * @param value the value the key must have for this rule to be applied
		 * @return a new rule reflecting the updated context
		 */
		public Rule valueIs(Value value) {
			return when(Key.VALUE, value);
		}
		
		/**
		 * Returns a new rule identical to this one, except that {@link
		 * Key#TO} must have the given value.
		 * 
		 * @param role the value the key must have for this rule to be applied
		 * @return a new rule reflecting the updated context
		 */
		public Rule to(Role role) {
			return when(Key.TO, role);
		}
		
		/**
		 * Returns a new rule identical to this one, except that its {@link
		 * #template natural language template} will be the given value.
		 * 
		 * @param template the natural language template the new rule will have
		 * @return a new rule using the given natural language template
		 */
		public Rule write(String template) {
			return new Rule(context, template);
		}
	}
	
	/**
	 * The verbs used by this describer, with the key being the {@link Verb#base
	 * verb's base} converted to upper case and wrapped in curly braces
	 */
	private final Map<String, Verb> verbs = new LinkedHashMap<>();
	
	/** The list of rules used by this describer */
	private final List<Rule> rules = new LinkedList<>();
	
	/**
	 * Constructs an empty describer with no verbs or rules.
	 */
	public Describer() {
		// Do nothing.
	}
	
	@Override
	public String toString() {
		return "[Describer: " + verbs.size() + " verbs; " + rules.size() + " rules]";
	}
	
	/**
	 * Returns all the verbs used by this describer.
	 * 
	 * @return this describer's verbs
	 */
	public Iterable<Verb> getVerbs() {
		return Collections.unmodifiableMap(verbs).values();
	}
	
	/**
	 * Returns the verb with the given {@link Verb#base base}.
	 * 
	 * @param base the base of the desired verb
	 * @return the verb with that base, or null if this describer has no verb
	 * with that base
	 */
	public Verb getVerb(String base) {
		return verbs.get("{" + base.toUpperCase() + "}");
	}
	
	/**
	 * Adds a verb to this describer, or replaces an existing verb with the same
	 * base, if one exists.
	 * 
	 * @param verb the new verb to add to this describer
	 */
	public void add(Verb verb) {
		verbs.put("{" + verb.base.toUpperCase() + "}", verb);
	}
	
	/**
	 * Returns an unmodifiable list of all the rules used by this describer,
	 * in the order they will be checked to see if they apply.
	 * 
	 * @return this describer's rules
	 */
	public Iterable<Rule> getRules() {
		return Collections.unmodifiableList(rules);
	}
	
	/**
	 * Returns the first rule from this describer that {@link Rule#matches(Map)
	 * matches} a given context.
	 * 
	 * @param context a map of keys to the value that describe an object to be
	 * described
	 * @return the first rule which would be used to describe an object with
	 * the given context
	 */
	public Rule getRule(Map<Key, Object> context) {
		for(Rule rule : rules)
			if(rule.matches(context))
				return rule;
		return null;
	}
	
	/**
	 * Adds a rule to the end of the list of rules that this describer can
	 * apply when describing an object.
	 * 
	 * @param rule the new rule to be added
	 */
	public void add(Rule rule) {
		rules.add(0, rule);
	}
	
	/**
	 * Applies this describer's rules to generate a natural language description
	 * of the given object for a given role. Each {@link #getRules() rule} is
	 * checked in order until one is found that {@link Rule#matches(Map)
	 * matches} the values of the given object. Then that rule's natural
	 * language template is applied to create the object's description.
	 * 
	 * @param object the object to be described
	 * @param to the role for whom the description is being generated, and the
	 * value that will be assigned to {@link Key#TO}
	 * @return a description of the object suitable for the given role
	 */
	public String getPhrase(Object object, Role to) {
		// Set context
		Map<Key, Object> context = new HashMap<>();
		context.put(Key.TO, to);
		context.put(Key.TYPE, object.getClass().getSimpleName().toLowerCase());
		if(object instanceof Entity entity) {
			context.put(Key.ENTITY, entity);
			context.put(Key.NAME, entity.getName());
		}
		else if(object instanceof Variable variable) {
			context.put(Key.VARIABLE, variable);
			signature(variable, context);
		}
		else if(object instanceof Action action) {
			context.put(Key.ACTION, action);
			signature(action, context);
		}
		else if(object instanceof Ending ending) {
			context.put(Key.ENDING, ending);
			signature(ending, context);
		}
		else if(object instanceof Assignment assignment) {
			context.put(Key.VARIABLE, assignment.variable);
			signature(assignment.variable, context);
			context.put(Key.VALUE, assignment.value);
		}
		else if(object instanceof Turn turn) {
			context.put(Key.ROLE, turn.role);
			context.put(Key.TURN_TYPE, turn.type);
			if(turn.action != null) {
				context.put(Key.ACTION, turn.action);
				signature(turn.action, context);
			}
		}
		else if(object instanceof Named named)
			context.put(Key.NAME, named.getName());
		else if(object != null)
			context.put(Key.NAME, Objects.toString(object));
		// Apply rule
		Rule rule = getRule(context);
		if(rule == null || rule.template == null) {
			if(object instanceof Described described)
				return described.getDescription();
			else if(object instanceof Named named)
				return named.getName();
			else
				return Objects.toString(object);
		}
		else {
			// Substitue keys
			String string = rule.template;
			for(Map.Entry<Key, Object> entry : context.entrySet()) {
				String key = "{" + entry.getKey().toString() + "}";
				if(string.contains(key)) {
					String value = getPhrase(entry.getValue(), to);
					string = string.replace(key, value);
				}
			}
			// Substitute verbs
			for(Map.Entry<String, Verb> entry : verbs.entrySet()) {
				String key = entry.getKey();
				int index = string.indexOf(key);
				while(index != -1) {
					String value = entry.getValue().third;
					if(index >= 4 && string.substring(index - 4, index - 1).equalsIgnoreCase("you"))
						value = entry.getValue().second;
					string = string.substring(0, index) + value + string.substring(index + key.length());
					index = string.indexOf(key);
				}
			}
			return string;
		}
	}
	
	private static final void signature(SignedAsset asset, Map<Key, Object> context) {
		context.put(Key.NAME, asset.signature.name);
		int length = Math.min(ARG_KEYS.length, asset.signature.getArguments().size());
		for(int i = 0; i < length; i++)
			context.put(ARG_KEYS[i], asset.signature.getArguments().get(i));
	}
	
	/**
	 * Applies this describer's rules to generate a natural language description
	 * of the given object for a session's {@link Role#GAME_MASTER game master}.
	 * 
	 * @param object the object to be described
	 * @return a description of the object
	 * @see #getPhrase(Object, Role)
	 */
	public String getPhrase(Object object) {
		return getPhrase(object, Role.GAME_MASTER);
	}
	
	/**
	 * Applies this describer's rules to generate a natural language description
	 * of the given object for a given role, with the first letter capitalized
	 * and ending in a period.
	 * 
	 * @param object the object to be described
	 * @param to the role for whom the description is being generated, and the
	 * value that will be assigned to {@link Key#TO}
	 * @return a description of the object suitable for the given role as a
	 * sentence with a capitalized first letter and ending with a period
	 * @see #getPhrase(Object, Role)
	 */
	public String getSentence(Object object, Role to) {
		String sentence = getPhrase(object, to);
		if(sentence != null) {
			sentence = Utilities.capitalize(sentence);
			if(!sentence.endsWith("."))
				sentence += ".";
		}
		return sentence;
	}
	
	/**
	 * Applies this describer's rules to generate a natural language description
	 * of the given object for a session's {@link Role#GAME_MASTER game master},
	 * with the first letter capitalized and ending in a period.
	 * 
	 * @param object the object to be described
	 * @return a description of the object as a sentence with a capitalized
	 * first letter and ending with a period
	 * @see #getSentence(Object, Role)
	 */
	public String getSentence(Object object) {
		return getSentence(object, Role.GAME_MASTER);
	}
}