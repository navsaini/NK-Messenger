package chat;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.application.Platform;
import javafx.event.ActionEvent;

public class ChatClient  {
	private BufferedReader reader;
	private PrintWriter writer;
	private TextArea chatSpace;
	private TextField messageToSend;
	private VBox list;
	
	private String clientName;
	private List<Conversation> conversations;
	private List<String> users;
	private Map<Button, Conversation> map;
	private Conversation currentConversation;
	
	private boolean windowOpen;	
	private Object lock = new Object();
	
	private static int xPos = 0;
	
	public ChatClient(String clientName) {
		this.conversations = new ArrayList<Conversation>();
		this.users = new ArrayList<String>();
		this.clientName = clientName;
		this.windowOpen = false;
		this.map = new HashMap<Button, Conversation>();
	}
	
	public void run() throws Exception {
		setUpNetworking();
		// initView();
		listConversations();
	}
	
	private void listConversations() {
		Stage stage = new Stage();
		list = new VBox(5);
		list.setPadding(new Insets(5));
		
		int currentSize = users.size();
		
		writer.println("get conversations");
		writer.flush();
		
		while (users.size() <= currentSize);
		for (String c: users) {
			Button b = new Button(c);
			b.setOnAction(new OpenChatListener());
			list.getChildren().add(b);
			Conversation newConvo = new Conversation(this.clientName, c);
			conversations.add(newConvo);
			map.put(b, newConvo);
		}
	    
		stage.setScene(new Scene(list, 100, 300));
	    stage.setX(0);
	    stage.setY(0);
	    stage.show();
	}

	private void initView() {
		Stage stage = new Stage();
		VBox vbox = new VBox(5);
		vbox.setPadding(new Insets(5));
		
		String welcome = "Welcome " + this.clientName + "!\n";
		chatSpace = new TextArea(welcome);
		chatSpace.setEditable(false);
		chatSpace.setWrapText(true);
		vbox.getChildren().add(chatSpace);
		
		messageToSend = new TextField();
		messageToSend.setOnKeyPressed(new EnterButtonListener());
		vbox.getChildren().add(messageToSend);
		
		Button sendButton = new Button("Send");
		sendButton.setOnAction(new SendButtonListener());
		vbox.getChildren().add(sendButton);
		sendButton.setTranslateX(120);
		sendButton.setTranslateY(15);
			    
		stage.setScene(new Scene(vbox, 300, 300));
	    stage.setX(xPos);
	    stage.setY(0);
	    xPos += 310;
	    stage.show();
	}
	
	private void setUpNetworking() throws Exception {
		@SuppressWarnings("resource")
		Socket sock = new Socket("127.0.0.1", 3000);
		InputStreamReader streamReader = new InputStreamReader(sock.getInputStream());
		reader = new BufferedReader(streamReader);
		writer = new ClientObserver(sock.getOutputStream());
		writer.println("new client: " + this.clientName);
		writer.flush();
		System.out.println("networking established");
		Thread readerThread = new Thread(new IncomingReader());
		readerThread.start();
	}

	class OpenChatListener implements EventHandler<ActionEvent> {
		@Override 
		public void handle(ActionEvent event) {
			if (!windowOpen) {
				initView();
				windowOpen = true;
				System.out.println("CURRENT CONVERSATIONS: " + conversations);
			} 
			Conversation now = map.get(((Button) event.getSource()));
			currentConversation = now;
			System.out.println("NOW: " + now);
			
		}
	}
	
	/**
	 * writes messages to the server
	 *
	 */
	// TODO: based on current conversation, append to outgoing message the name of the receiver
	
	class SendButtonListener implements EventHandler<ActionEvent> {			
		@Override
		public void handle(ActionEvent event) {
			String formattedMsg = clientName + ": " + "@" + 
					currentConversation.receiver.toString().substring(1, currentConversation.receiver.toString().length() - 1) 
					+ " " + messageToSend.getText();
			writer.println(formattedMsg);
			writer.flush();
//			if (messageToSend.getText().contains("@"))
			chatSpace.appendText(clientName + ": " + messageToSend.getText() + "\n");
			messageToSend.setText("");
		}
	}
	
	class EnterButtonListener implements EventHandler<KeyEvent> {
		@Override
		public void handle(KeyEvent event) {
			if (event.getCode() == KeyCode.ENTER) {
				writer.println(clientName + ": " + messageToSend.getText());
				writer.flush();
			if (messageToSend.getText().contains("@"))
				chatSpace.appendText(clientName + ": " + messageToSend.getText() + "\n");
				messageToSend.setText("");
			}
		}
	}
	
	/**
	 * reads incoming messages from the server
	 *
	 */
	class IncomingReader implements Runnable {
		public void run() {
			String message;
			try {
				synchronized(lock) {
					while ((message = reader.readLine()) != null) {
						if (message.contains("new client")) {
							String otherName = (message.substring(message.indexOf(":")+1).trim());
							if (!clientName.equals(otherName)) {
								System.out.println(clientName + " " + otherName);
								Conversation newConvo = new Conversation(clientName, otherName);
								conversations.add(newConvo);
								System.out.println("convos: " + conversations);
								Platform.runLater(new Runnable() {
									@Override
									public void run() {
										Button b = new Button(otherName);
										b.setOnAction(new OpenChatListener());
										list.getChildren().add(b);
										map.put(b, newConvo);
									}
								});
							}
						}
						else if (message.contains("users: ")) {
							System.out.println(message.substring((message.indexOf("[") + 1), message.indexOf("]")));
							for (String s: message.substring((message.indexOf("[") + 1), message.indexOf("]")).split(", ")) {
								users.add(s);
							}
						}
						else if (message.contains("@" + clientName)) {
							chatSpace.appendText(message + "\n");
							System.out.println("contains@");
						}
						else if (!message.contains("@")) {
							chatSpace.appendText(message + "\n");
							System.out.println("!contains@: " + message);
						}
						else {
							System.out.println("none case, here's the message: " + message);
							System.out.println("client name is: " + clientName);
						}
					}
				}
			} catch (IOException ex) {
				ex.printStackTrace();
			} 
		}
	}
}
