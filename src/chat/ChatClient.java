package chat;

import java.io.*;
import java.net.*;

import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.event.ActionEvent;

public class ChatClient {
	private BufferedReader reader;
	private PrintWriter writer;
	
	static int count = 1;
	
	public void run() throws Exception {
		setUpNetworking();
	}

	private void setUpNetworking() throws Exception {
		@SuppressWarnings("resource")
		Socket sock = new Socket("127.0.0.1", 3000);
		InputStreamReader streamReader = new InputStreamReader(sock.getInputStream());
		reader = new BufferedReader(streamReader);
		writer = new ClientObserver(sock.getOutputStream());
		System.out.println("networking established");
		Thread readerThread = new Thread(new IncomingReader());
		readerThread.start();
	}

	/**
	 * writes messages to the server
	 *
	 */
	class SendButtonListener implements EventHandler<ActionEvent> {	
		TextField messageField;
		
		public SendButtonListener(TextField messageField) {
			this.messageField = messageField;
		}
		
		@Override
		public void handle(ActionEvent event) {
			writer.println(messageField.getText());
			writer.flush();
			messageField.setText("");
		}
		
	}

	// reads incoming messages from the server
	class IncomingReader implements Runnable {
		public void run() {
			String message;
			try {
				while ((message = reader.readLine()) != null) {
					System.out.println("incoming append count: " + count);
					count++;
					for (VBox vbox: Painter.vboxes) {
						for (Object child: vbox.getChildren()) {
							if (child instanceof TextArea) {
								((TextArea) child).appendText(message + "\n");
							}
						}
					}
				}
			} catch (IOException ex) {
				ex.printStackTrace();
			} 
		}
	}
}
