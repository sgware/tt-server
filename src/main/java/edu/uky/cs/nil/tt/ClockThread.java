package edu.uky.cs.nil.tt;

/**
 * The clock thread regularly calls the server's {@link Server#tick() tick}
 * method.
 * 
 * @author Stephen G. Ware
 */
public class ClockThread extends Thread {
	
	/** The server this thread will call {@link Server#tick()} on */
	private final Server server;
	
	/**
	 * Constructs a clock thread for a given server.
	 * 
	 * @param server the server for which this thread will call {@link
	 * Server#tick()}
	 */
	public ClockThread(Server server) {
		this.server = server;
	}
	
	@Override
	public void run() {
		try {
			while(true) {
				Thread.sleep(Settings.TICK_SPEED);
				server.execute(() -> {
					if(!server.getStopped())
						server.tick();
				});
			}
		}
		catch(InterruptedException exception) {
			if(!server.getStopped())
				server.log.append("The server's clock thread was interrupted before the server was stopped.", exception);
		}
	}
}