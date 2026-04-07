package edu.uky.cs.nil.tt;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.Random;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import edu.uky.cs.nil.tt.Session.Event;
import edu.uky.cs.nil.tt.world.Expression;
import edu.uky.cs.nil.tt.world.Status;
import edu.uky.cs.nil.tt.world.Turn;

public class Test {
	
	public static void main(String[] args) throws Exception {
		// Set system properties.
		System.setProperty("javax.net.ssl.keyStore", "server.keystore");
		System.setProperty("javax.net.ssl.keyStorePassword", "serverpassword");
		System.setProperty("javax.net.ssl.trustStore", "client.truststore");
		System.setProperty("javax.net.ssl.trustStorePassword", "clientpassword");
		// Start server.
		Server server = new Server(new File("log.txt"), new File("sessions"), new File("database.json"), 12345);
		new Thread(() -> {
			try {
				server.run();
			}
			catch(Exception exception) {
				exception.printStackTrace();
			}
		}).start();
		// Connect two random clients.
		Client client = new RandomClient("random");
		ClientFactory factory = new RandomClientFactory();
		factory.call();
		String id = null;
		try {
			id = client.call();
		}
		catch(Exception exception) {
			exception.printStackTrace();
		}
		// Stop the server.
		server.close();
		client.close();
		factory.close();
		// Read the session log.
		GsonBuilder builder = new GsonBuilder();
		Expression.configure(builder);
		Gson gson = builder.create();
		try(BufferedReader input = new BufferedReader(new FileReader(new File("sessions/" + id + ".json")))) {
			Session session = gson.fromJson(input, Session.class);
			System.out.println(session);
			for(Event event : session.getEvents())
				if(event.turn != null && event.turn.type == Turn.Type.SUCCEED)
					System.out.println(event);
			System.out.println(session.getResult());
		}
	}
	
	private static class RandomClientFactory extends ClientFactory {
		
		@Override
		protected RandomClient create() {
			return new RandomClient();
		}
	}
	
	public static class RandomClient extends Client {
		
		private final Random random = new Random(0);
		
		public RandomClient(String partner) {
			super("random", "random", null, null, partner, null, DEFAULT_URL, DEFAULT_PORT);
		}
		
		public RandomClient() {
			this(null);
		}
		
		@Override
		protected int onChoice(Status status) {
			return random.nextInt(status.getChoices().size());
		}
	}
}