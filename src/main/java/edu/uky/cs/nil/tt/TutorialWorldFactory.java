package edu.uky.cs.nil.tt;

import java.io.File;

import edu.uky.cs.nil.tt.world.*;
import edu.uky.cs.nil.tt.world.Describer.*;

/**
 * A example of how to create a {@link LogicalWorld logical story world model}.
 * This example produces a small story world model that can teach players the
 * basic features of Tandem Tales.
 * 
 * @author Stephen G. Ware
 */
public class TutorialWorldFactory {
	
	/**
	 * Creates the example logical story world, serializes it to file, and
	 * prints a description of the world.
	 * 
	 * @param args Java command line arguments, ignored
	 * @throws Exception if a problem occurs when creating the story world or
	 * writing it to file
	 */
	public static void main(String[] args) throws Exception {
		// Create world
		LogicalWorld world = makeWorld();
		// Write world to file
		File file = new File("worlds/tutorial.json");
		world.write(file);
		System.out.println("World \"" + world.getName() + "\" written to \"" + file + "\".");
		// Read world from file
		world = LogicalWorld.read(file);
		System.out.println("World \"" + world.getName() + "\" read from \"" + file + "\".");
		// Print world
		System.out.println(world.describe());
	}
	
	/**
	 * Creates an example {@link LogicalWorld logical story world model} from
	 * scratch, demonstrating how to use tools like the {@link AssetBuilder
	 * asset builder} and {@link Describer describer}.
	 * 
	 * @return a small logical story world model
	 */
	public static LogicalWorld makeWorld() {
		AssetBuilder builder = new AssetBuilder();
		builder.setName("tutorial");
		// Entities
		Entity player = builder.addEntity("Player", "the player");
		Entity barista = builder.addEntity("Barista", "the barista");
		Entity outside = builder.addEntity("Outside", "outside the shop");
		Entity shop = builder.addEntity("Shop", "the shop");
		Entity money = builder.addEntity("Money", "some money");
		Entity coffee = builder.addEntity("Coffee", "a cup of coffee");
		Entity tea = builder.addEntity("Tea", "a cup of herbal tea");
		// Variables
		Variable atPlayer = builder.addVariable(new Signature("at", player), "entity", "the player's location");
		Variable atBarista = builder.addVariable(new Signature("at", barista), "entity", "the barista's location");
		Variable hasMoney = builder.addVariable(new Signature("has", money), "entity", "who has the money");
		Variable hasCoffee = builder.addVariable(new Signature("has", coffee), "entity", "who has the coffee");
		Variable hasTea = builder.addVariable(new Signature("has", tea), "entity", "who has the tea");
		// Actions
		Action walkPlayerShop = builder.addAction(new Signature("go", player, shop), new Entity[]{ player }, "The player enters the shop.");
		Action walkPlayerOutside = builder.addAction(new Signature("go", player, outside), new Entity[]{ player }, "The player leaves the shop.");
		Action orderPlayerCoffee = builder.addAction(new Signature("order", player, coffee), new Entity[]{ player }, "The player orders a cup of coffee.");
		Action orderPlayerTea = builder.addAction(new Signature("order", player, tea), new Entity[]{ player }, "The player orders a cup of herbal tea.");
		Action brewBaristaCoffee = builder.addAction(new Signature("brew", barista, coffee), new Entity[]{ barista }, "The barista brews a cup of coffee.");
		Action brewBaristaTea = builder.addAction(new Signature("brew", barista, tea), new Entity[]{ barista }, "The barista brews a cup of herbal tea.");
		Action givePlayerMoneyBarista = builder.addAction(new Signature("give", player, money, barista), new Entity[]{ player, barista }, "The player gives some money to the barista.");
		Action giveBaristaCoffeePlayer = builder.addAction(new Signature("give", barista, coffee, player), new Entity[]{ player, barista }, "The barista gives a cup of coffee to the player.");
		Action giveBaristaTeaPlayer = builder.addAction(new Signature("give", barista, tea, player), new Entity[]{ player, barista }, "The barista gives a cup of herbal tea to the player.");
		Action tradePlayerMoneyBaristaCoffee = builder.addAction(new Signature("trade", player, money, barista, coffee), new Entity[]{ player, barista }, "The player trades some money to the barista for a cup of coffee.");
		Action tradePlayerMoneyBaristaTea = builder.addAction(new Signature("trade", player, money, barista, tea), new Entity[]{ player, barista }, "The player trades some money to the barista for a cup of herbal tea.");
		// Endings
		Ending donation = builder.addEnding(new Signature("donation"), "The player gave money to the barista but got not drink.");
		Ending freeCoffee = builder.addEnding(new Signature("free", coffee), "The player got a cup of coffee for free.");
		Ending freeTea = builder.addEnding(new Signature("free", tea), "The player got a cup of herbal tea for free.");
		Ending boughtCoffee = builder.addEnding(new Signature("bought", coffee), "The player purchased a coffee.");
		Ending boughtTea = builder.addEnding(new Signature("bought", tea), "The player purchased a cup of herbal tea.");
		// World
		LogicalWorld world = new LogicalWorld(builder);
		Describer describer = world.getDescriber();
		// Entity Visibility
		world.setVisibility(player, Proposition.TRUE);
		world.setVisibility(barista, eq(atPlayer, atBarista));
		world.setVisibility(outside, eq(atPlayer, outside));
		world.setVisibility(shop, eq(atPlayer, shop));
		world.setVisibility(money, or(eq(hasMoney, player), and(eq(atPlayer, atBarista), eq(hasMoney, barista))));
		world.setVisibility(coffee, or(eq(hasCoffee, player), and(eq(atPlayer, atBarista), eq(hasCoffee, barista))));
		world.setVisibility(tea, or(eq(hasTea, player), and(eq(atPlayer, atBarista), eq(hasTea, barista))));
		// Entity Descriptions
		describer.add(new Rule().entityIs(player).to(Role.PLAYER).write("you"));
		// Initial State
		world.setInitialValue(atPlayer, outside);
		world.setInitialValue(atBarista, shop);
		world.setInitialValue(hasMoney, player);
		// Variable Visibility
		world.setVisibility(atBarista, eq(atPlayer, atBarista));
		world.setVisibility(hasMoney, or(eq(hasMoney, player), and(eq(atPlayer, atBarista), eq(hasMoney, barista))));
		world.setVisibility(hasCoffee, or(eq(hasCoffee, player), and(eq(atPlayer, atBarista), eq(hasCoffee, barista))));
		world.setVisibility(hasTea, or(eq(hasTea, player), and(eq(atPlayer, atBarista), eq(hasTea, barista))));
		// Assignment Descriptions
		describer.add(new Verb("be", "are", "is"));
		describer.add(new Verb("have", "has"));
		describer.add(new Rule().typeIs(Assignment.class).nameIs("at").valueIs(outside).write("{ARG_0} {BE} outside the shop"));
		describer.add(new Rule().typeIs(Assignment.class).nameIs("at").valueIs(shop).write("{ARG_0} {BE} in the shop"));
		describer.add(new Rule().typeIs(Assignment.class).nameIs("has").write("{VALUE} {HAVE} {ARG_0}"));
		describer.add(new Rule().typeIs(Assignment.class).nameIs("has").valueIs(null).write("{ARG_0} does not exist"));
		// Walk Actions
		world.setPrecondition(walkPlayerShop, eq(atPlayer, outside));
		world.setEffects(walkPlayerShop, new Effect(atPlayer, shop));
		describer.add(new Verb("want", "wants"));
		describer.add(new Verb("enter", "enters"));
		describer.add(new Verb("try", "tries"));
		describer.add(new Rule().roleIs(Role.PLAYER).turnIs(Turn.Type.PROPOSE).nameIs("go").when(Key.ARG_1, shop).write("{ARG_0} {WANT} to enter the shop"));
		describer.add(new Rule().roleIs(Role.PLAYER).turnIs(Turn.Type.PROPOSE).nameIs("go").when(Key.ARG_1, shop).to(Role.PLAYER).write("Enter the shop"));
		describer.add(new Rule().turnIs(Turn.Type.SUCCEED).nameIs("go").when(Key.ARG_1, shop).write("{ARG_0} {ENTER} the shop"));
		describer.add(new Rule().turnIs(Turn.Type.FAIL).nameIs("go").when(Key.ARG_1, shop).write("{ARG_0} {TRY} to enter the shop, but the door gets stuck"));
		world.setPrecondition(walkPlayerOutside, eq(atPlayer, shop));
		world.setEffects(walkPlayerOutside, new Effect(atPlayer, outside));
		describer.add(new Verb("leave", "leaves"));
		describer.add(new Rule().roleIs(Role.PLAYER).turnIs(Turn.Type.PROPOSE).nameIs("go").when(Key.ARG_1, outside).write("{ARG_0} {WANT} to leave the shop"));
		describer.add(new Rule().roleIs(Role.PLAYER).turnIs(Turn.Type.PROPOSE).nameIs("go").when(Key.ARG_1, outside).to(Role.PLAYER).write("Leave the shop"));
		describer.add(new Rule().turnIs(Turn.Type.SUCCEED).nameIs("go").when(Key.ARG_1, outside).write("{ARG_0} {LEAVE} the shop"));
		describer.add(new Rule().turnIs(Turn.Type.FAIL).nameIs("go").when(Key.ARG_1, outside).write("{ARG_0} {TRY} to leave the shop, but the door gets stuck"));
		// Order Actions
		world.setPrecondition(orderPlayerCoffee, eq(atPlayer, shop));
		world.setPrecondition(orderPlayerTea, eq(atPlayer, shop));
		describer.add(new Verb("order", "orders"));
		describer.add(new Verb("reconsider", "reconsiders"));
		describer.add(new Rule().turnIs(Turn.Type.PROPOSE).nameIs("order").write("{ARG_0} {WANT} to order {ARG_1}"));
		describer.add(new Rule().turnIs(Turn.Type.PROPOSE).nameIs("order").to(Role.PLAYER).write("Order {ARG_1}"));
		describer.add(new Rule().turnIs(Turn.Type.SUCCEED).nameIs("order").write("{ARG_0} {ORDER} {ARG_1}"));
		describer.add(new Rule().turnIs(Turn.Type.FAIL).nameIs("order").write("{ARG_0} {TRY} to order {ARG_1}, but {ARG_0} {RECONSIDER}."));
		// Brew Actions
		world.setPrecondition(brewBaristaCoffee, eq(hasCoffee, Constant.NULL));
		world.setEffects(brewBaristaCoffee, new Effect(hasCoffee, barista));
		world.setVisibility(brewBaristaCoffee, eq(atPlayer, shop));
		world.setPrecondition(brewBaristaTea, eq(hasTea, Constant.NULL));
		world.setEffects(brewBaristaTea, new Effect(hasTea, barista));
		world.setVisibility(brewBaristaTea, eq(atPlayer, shop));
		describer.add(new Verb("brew", "brews"));
		describer.add(new Rule().turnIs(Turn.Type.SUCCEED).nameIs("brew").write("{ARG_0} {BREW} {ARG_1}"));
		describer.add(new Rule().turnIs(Turn.Type.FAIL).nameIs("brew").write("{ARG_0} {TRY} to brew {ARG_1}, but something goes wrong"));
		// Give Actions
		world.setPrecondition(givePlayerMoneyBarista, and(eq(atPlayer, atBarista), eq(hasMoney, player)));
		world.setEffects(givePlayerMoneyBarista, new Effect(hasMoney, barista));
		world.setPrecondition(giveBaristaCoffeePlayer, and(eq(atBarista, atPlayer), eq(hasCoffee, barista)));
		world.setEffects(giveBaristaCoffeePlayer, new Effect(hasCoffee, player));
		world.setPrecondition(giveBaristaTeaPlayer, and(eq(atBarista, atPlayer), eq(hasTea, barista)));
		world.setEffects(giveBaristaTeaPlayer, new Effect(hasTea, player));
		describer.add(new Verb("offer", "offers"));
		describer.add(new Verb("request", "requests"));
		describer.add(new Verb("give", "gives"));
		describer.add(new Verb("refuse", "refuses"));
		describer.add(new Rule().turnIs(Turn.Type.PROPOSE).nameIs("give").write("{ARG_0} {OFFER} {ARG_1} to {ARG_2}"));
		describer.add(new Rule().roleIs(Role.GAME_MASTER).turnIs(Turn.Type.PROPOSE).nameIs("give").when(Key.ARG_0, player).write("{ARG_2} {REQUEST} {ARG_1} from {ARG_0}"));
		describer.add(new Rule().roleIs(Role.PLAYER).turnIs(Turn.Type.PROPOSE).nameIs("give").when(Key.ARG_2, player).write("{ARG_2} {REQUEST} {ARG_1} from {ARG_0}"));
		describer.add(new Rule().roleIs(Role.PLAYER).turnIs(Turn.Type.PROPOSE).nameIs("give").when(Key.ARG_0, player).to(Role.PLAYER).write("Offer {ARG_1} to {ARG_2}."));
		describer.add(new Rule().roleIs(Role.PLAYER).turnIs(Turn.Type.PROPOSE).nameIs("give").when(Key.ARG_2, player).to(Role.PLAYER).write("Request {ARG_1} from {ARG_0}."));
		describer.add(new Rule().turnIs(Turn.Type.SUCCEED).nameIs("give").write("{ARG_0} {GIVE} {ARG_1} to {ARG_2}"));
		describer.add(new Rule().turnIs(Turn.Type.FAIL).nameIs("give").write("{ARG_0} {OFFER} {ARG_1} to {ARG_2}, but {ARG_2} refuses."));
		describer.add(new Rule().roleIs(Role.GAME_MASTER).turnIs(Turn.Type.FAIL).nameIs("give").when(Key.ARG_2, player).write("{ARG_2} {REQUEST} {ARG_1} from {ARG_0}, but {ARG_0} {REFUSE}."));
		describer.add(new Rule().roleIs(Role.PLAYER).turnIs(Turn.Type.FAIL).nameIs("give").when(Key.ARG_0, player).write("{ARG_2} {REQUEST} {ARG_1} from {ARG_0}, but {ARG_0} {REFUSE}."));
		// Trade Actions
		world.setPrecondition(tradePlayerMoneyBaristaCoffee, and(eq(atPlayer, atBarista), eq(hasMoney, player), eq(hasCoffee, barista)));
		world.setEffects(tradePlayerMoneyBaristaCoffee, new Effect(hasMoney, barista), new Effect(hasCoffee, player));
		world.setPrecondition(tradePlayerMoneyBaristaTea, and(eq(atPlayer, atBarista), eq(hasMoney, player), eq(hasTea, barista)));
		world.setEffects(tradePlayerMoneyBaristaTea, new Effect(hasMoney, barista), new Effect(hasTea, player));
		describer.add(new Verb("trade", "trades"));
		describer.add(new Rule().turnIs(Turn.Type.PROPOSE).nameIs("trade").write("{ARG_0} {OFFER} {ARG_1} to {ARG_2} for {ARG_3}"));
		describer.add(new Rule().roleIs(Role.GAME_MASTER).turnIs(Turn.Type.PROPOSE).nameIs("trade").write("{ARG_2} {OFFER} {ARG_3} to {ARG_0} for {ARG_1}"));
		describer.add(new Rule().roleIs(Role.PLAYER).turnIs(Turn.Type.PROPOSE).nameIs("trade").to(Role.PLAYER).write("Offer {ARG_1} to {ARG_2} for {ARG_3}"));
		describer.add(new Rule().turnIs(Turn.Type.SUCCEED).nameIs("trade").write("{ARG_0} {TRADE} {ARG_1} to {ARG_2} for {ARG_3}"));
		describer.add(new Rule().turnIs(Turn.Type.FAIL).nameIs("trade").write("{ARG_0} {OFFER} {ARG_1} to {ARG_2} for {ARG_3}, but {ARG_2} {REFUSE}."));
		describer.add(new Rule().roleIs(Role.PLAYER).turnIs(Turn.Type.FAIL).nameIs("trade").write("{ARG_2} {OFFER} {ARG_3} to {ARG_0} for {ARG_1}, but {ARG_0} {REFUSE}."));
		// Endings
		world.setCondition(donation, and(eq(atPlayer, outside), eq(hasMoney, barista), neq(hasCoffee, player), neq(hasTea, player)));
		describer.add(new Rule().endingIs(donation).to(Role.GAME_MASTER).write("The player decided they weren't thirsy and made a generous donation to the barista."));
		describer.add(new Rule().endingIs(donation).to(Role.PLAYER).write("You decided you weren't thirsy and made a generous donation to the barista."));
		world.setCondition(freeCoffee, and(eq(atPlayer, outside), eq(hasMoney, player), eq(hasCoffee, player)));
		world.setCondition(freeTea, and(eq(atPlayer, outside), eq(hasMoney, player), eq(hasTea, player)));
		describer.add(new Rule().typeIs(Ending.class).nameIs("free").to(Role.GAME_MASTER).write("The player walks away, happily sipping {ARG_0} that they got for free."));
		describer.add(new Rule().typeIs(Ending.class).nameIs("free").to(Role.PLAYER).write("You walk away, happily sipping {ARG_0} that you got for free."));
		world.setCondition(boughtCoffee, and(eq(atPlayer, outside), eq(hasMoney, barista), eq(hasCoffee, player)));
		world.setCondition(boughtTea, and(eq(atPlayer, outside), eq(hasMoney, barista), eq(hasTea, player)));
		describer.add(new Rule().typeIs(Ending.class).nameIs("bought").to(Role.GAME_MASTER).write("The player walks away, happily sipping {ARG_0} that they purchased."));
		describer.add(new Rule().typeIs(Ending.class).nameIs("bought").to(Role.PLAYER).write("You walk away, happily sipping {ARG_0} that you purchased."));
		// Pass
		describer.add(new Rule().roleIs(Role.GAME_MASTER).turnIs(Turn.Type.PASS).to(Role.GAME_MASTER).write("let the player act"));
		describer.add(new Rule().roleIs(Role.GAME_MASTER).turnIs(Turn.Type.PASS).to(Role.PLAYER).write("the game master lets you act"));
		describer.add(new Rule().roleIs(Role.PLAYER).turnIs(Turn.Type.PASS).to(Role.GAME_MASTER).write("the player lets the game master act"));
		describer.add(new Rule().roleIs(Role.PLAYER).turnIs(Turn.Type.PASS).to(Role.PLAYER).write("let the game master act"));
		return world;
	}
	
	private static final Proposition eq(Expression...arguments) {
		return new Proposition(Proposition.Operator.EQUALS, arguments);
	}
	
	private static final Proposition neq(Expression...arguments) {
		return new Proposition(Proposition.Operator.NOT, new Proposition(Proposition.Operator.EQUALS, arguments));
	}
	
	private static final Proposition and(Expression...arguments) {
		return new Proposition(Proposition.Operator.AND, arguments);
	}
	
	private static final Proposition or(Expression...arguments) {
		return new Proposition(Proposition.Operator.OR, arguments);
	}
	
	private TutorialWorldFactory() {
		// Do nothing.
	}
}