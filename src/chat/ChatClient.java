package chat;

import java.io.*;
import java.net.*;

import chat.ChatClient.SendButtonListener;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.event.ActionEvent;

public class ChatClient {
	private BufferedReader reader;
	private PrintWriter writer;
	private TextArea chatSpace;
	private TextField messageToSend;
	
	static int count = 1;
	
	public void run() throws Exception {
		Stage stage = new Stage();
		VBox vbox = new VBox();
		initView(stage, vbox);
		setUpNetworking();
	}

	private void initView(Stage stage, VBox vbox) {
		vbox.setPadding(new Insets(5));
		
		chatSpace = new TextArea("begin chatting now...\n");
		chatSpace.setEditable(false);
		vbox.getChildren().add(chatSpace);
		
		messageToSend = new TextField();
		vbox.getChildren().add(messageToSend);
		
		Button sendButton = new Button("Send");
		sendButton.setOnAction(new SendButtonListener());
		vbox.getChildren().add(sendButton);
		
		stage.setScene(new Scene(vbox, 300, 300));
	    stage.setX(0);
	    stage.setY(0);
	    stage.show();
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
		@Override
		public void handle(ActionEvent event) {
			writer.println(messageToSend.getText());
			writer.flush();
			messageToSend.setText("");
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
					chatSpace.appendText(message + "\n");
				}
			} catch (IOException ex) {
				ex.printStackTrace();
			} 
		}
	}
}
