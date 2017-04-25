package chat;

import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Observable;
import java.util.Observer;

public class ClientObserver extends PrintWriter implements Observer {
	Object lock = new Object();
	
	public ClientObserver(OutputStream out) {
		super(out);
	}
	
	// sending message back to client
	@Override
	public void update(Observable o, Object arg) {
		System.out.println("update called count: " + ChatClient.count);
		ChatClient.count++;
		synchronized(lock) {
			this.println(arg);
			this.flush();
		}
	}

}
