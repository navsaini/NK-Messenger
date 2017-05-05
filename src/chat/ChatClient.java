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
import javafx.scene.control.Label;
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
	private TextField personToAdd;
	private VBox list;
	
	private String clientName;
	private List<Conversation> conversations;
	private List<String> users;
	private Map<Button, Conversation> map;
	private Conversation currentConversation;
	
	private boolean windowOpen;	
	private Object lock = new Object();
	
	private static int xPos = 0;

	private static int buttonsX = 0;
	
	private boolean allNeeded = true;
	
	public ChatClient(String clientName) {
		this.conversations = new ArrayList<Conversation>();
		this.users = new ArrayList<String>();
		this.clientName = clientName;
		this.windowOpen = false;
		this.map = new HashMap<Button, Conversation>();
	}
	
	public void run() throws Exception {
		setUpNetworking();
		listConversations();
	}
	
	private void listConversations() {
		Stage stage = new Stage();
		list = new VBox(5);
		list.setPadding(new Insets(5));
		
		Label name = new Label("User: " + clientName + '\n' + "Currently online: ");
		list.getChildren().add(name);
		
		// get the current number of users
		int currentSize = users.size();
		
		// ask the server for all users
		// this should honestly say "get users"
		// instead of "get conversations" but changing it
		// is too much work
		writer.println("get conversations");
		writer.flush();
		
		// wait till the list of users is updated after 
		// asking the server for it
		while (users.size() <= currentSize);
		
		// make a button for each user that's not yourself
		for (String c: users) {
			if (!c.equals(clientName)) {
				Button b = new Button("Chat with " + c);
				b.setOnAction(new OpenChatListener());
				list.getChildren().add(b);
				Conversation newConvo = new Conversation(this.clientName, c);
				conversations.add(newConvo);
				map.put(b, newConvo);
			}
		}
		
		if(users.size() > 2) {
			System.out.println("simple one called");
			Button b = new Button("Chat with All");
			b.setOnAction(new OpenChatListener());
			list.getChildren().add(b);
			Conversation newConvo = new Conversation(clientName, users);
			conversations.add(newConvo);
			map.put(b, newConvo);
		}

	    
		stage.setScene(new Scene(list, 200, 300));
	    stage.setX(buttonsX);
	    buttonsX += 210;
	    stage.setY(0);
	    stage.show();
	}

	private void initView() {
		Stage stage = new Stage();
		VBox vbox = new VBox(5);
		vbox.setPadding(new Insets(5));
		
		String welcome = "Welcome " + this.clientName + "!\n";
		welcome += "You are currently talking to " + currentConversation.receivers + "\n";
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
		
//		Button addButton = new Button("Add");
//		vbox.getChildren().add(addButton);
//		addButton.setOnAction(new AddButtonListener());
//		addButton.setTranslateX(122.5);
//		addButton.setTranslateY(15);
		
		stage.setScene(new Scene(vbox, 300, 320));
	    stage.setX(xPos);
	    stage.setY(370);
	    xPos += 310;
	    stage.show();
	}
	
	private void setUpNetworking() throws Exception {
		@SuppressWarnings("resource")
		Socket sock = new Socket("10.147.69.29", 3000);
		InputStreamReader streamReader = new InputStreamReader(sock.getInputStream());
		reader = new BufferedReader(streamReader);
		writer = new ClientObserver(sock.getOutputStream());
		// tells the server that there is a new client
		// using the keyword/key pattern of "new client: " 
		writer.println("new client: " + this.clientName);
		writer.flush();
		System.out.println("networking established");
		Thread readerThread = new Thread(new IncomingReader());
		readerThread.start();
	}

	// listener for opening new conversation
	// changes current conversation
	class OpenChatListener implements EventHandler<ActionEvent> {
		@Override 
		public void handle(ActionEvent event) {
			Conversation now = map.get(((Button) event.getSource()));
			currentConversation = now;
			System.out.println("NOW: " + now);
			if (!windowOpen) {
				initView();
				windowOpen = true;
			}
			chatSpace.clear();
			chatSpace.appendText("Welcome " + clientName + "\n");
			chatSpace.appendText("You are now chatting with " + currentConversation.receivers + "\n");
			chatSpace.appendText(currentConversation.conversationText);
		}
	}
	
	/**
	 * writes messages to the server
	 *
	 */	
	// this is the code that formats the message before sending it out to the server
	class SendButtonListener implements EventHandler<ActionEvent> {			
		@Override
		public void handle(ActionEvent event) {
			String formattedMsg = "";
			// if current conversation is a group message
			if (currentConversation.receivers.size() <= 1) {
				formattedMsg = clientName + ": " + "@" + 
					currentConversation.receivers.get(0) 
					+ " " + messageToSend.getText();
				chatSpace.appendText(clientName + ": " + messageToSend.getText() + "\n");
			}
			else {
				formattedMsg = clientName + ": ";
				formattedMsg += "@all ";
				formattedMsg += messageToSend.getText();
				
				System.out.println("group formatted msg: " + formattedMsg);
			}
			writer.println(formattedMsg);
			writer.flush();
			messageToSend.setText("");
		}
	}
	
//	class AddButtonListener implements EventHandler<ActionEvent> {
//		@Override
//		public void handle(ActionEvent event) {
//			promptAddPerson();
//		}
//	}
	
	/**
	 * listener to add a person to a group
	 *
	 */
	class AddGroupListener implements EventHandler<ActionEvent> {
		@Override
		public void handle(ActionEvent event) {
			currentConversation.addReceiver(personToAdd.getText());
			System.out.println("should be a group: " + currentConversation);
			((Button) event.getSource()).getParent().getScene().getWindow().hide();
			chatSpace.appendText("You are currently talking to: " + currentConversation.receivers);
		}
	}
	
//	/**
//	 * called when you click add inside a conversation window
//	 */
//	private void promptAddPerson() {
//		Stage stage = new Stage();
//		VBox vbox = new VBox(5);
//		vbox.setPadding(new Insets(5));
//
//		Label promptMsg = new Label("Who would you like to add to this chat?");
//		vbox.getChildren().add(promptMsg);
//		
//		personToAdd = new TextField("enter someone's name");
//		vbox.getChildren().add(personToAdd);
//		
//		Button addToGroup = new Button("Start group");
//		vbox.getChildren().add(addToGroup);
//		
//		addToGroup.setOnAction(new AddGroupListener());
//		
//		stage.setScene(new Scene(vbox, 300, 320));
//		stage.setX(0);
//	    stage.setY(0);
//	    stage.show();
//	}
	
	// TODO: make this similar to sendbuttonlistener
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
	 * and checks if I should be displaying the message
	 *
	 */
	class IncomingReader implements Runnable {
		public void run() {
			String message;
			try {
//				synchronized(lock) {
					while ((message = reader.readLine()) != null) {
						
						System.out.println("message that was sent out: " + message + " and the receiver: " + clientName);
						System.out.println(message.contains("@" + clientName));
						// if the message from the server has the keywords "new client"
						if (message.contains("new client")) {
							// since the message from the server has the pattern "new client: ..."
							// find the index of ":" and take everything after that,
							// which the actual name of the new client
							// otherName = the client that just joined the server
							String otherName = (message.substring(message.indexOf(":") + 1).trim());
							if (!clientName.equals(otherName)) {
								System.out.println(clientName + " " + otherName);
								// add this person to the list of this client's conversations
								Conversation newConvo = new Conversation(clientName, otherName);
								conversations.add(newConvo);
								// make a new button with this person's name
								// this has to be done in this weird "Platform.runLater"
								// because IncomingReader (this runnable class) is already a runnable
								// so it needs to switch threads before it can mess with javafx
								Platform.runLater(new Runnable() {
									@Override
									public void run() {
										System.out.println(conversations.size());
										if (users.size() > 2 && allNeeded) {
											System.out.println("complicated one called");
											allNeeded = false;
											Button b = new Button("Chat with All");
											b.setOnAction(new OpenChatListener());
											list.getChildren().add(b);
											Conversation newConvo = new Conversation(clientName, users);
											conversations.add(newConvo);
											map.put(b, newConvo);
										}
										// make a button with the new name
										Button b = new Button("Chat with " + otherName);
										// attach a listener to the button
										b.setOnAction(new OpenChatListener());
										// add it to the vbox called list (the list of online users)
										list.getChildren().add(b);
										// add it to the hashmap of buttons to conversations
										map.put(b, newConvo);
									}
								});
							}
						}
						// if the message from the server contains the keyword "users: "
						else if (message.contains("users: ")) {
							// take off the brackets
							for (String s: message.substring((message.indexOf("[") + 1), message.indexOf("]")).split(", ")) {
								if (!users.contains(s))
									// add to list of users
									users.add(s);
							}
						}
						else if (message.contains("@all")) {
							int startName = message.indexOf("@");
							String formattedMsg = message.substring(0, startName);
							formattedMsg += message.substring(startName + 5).trim();
							if (chatSpace != null)
								chatSpace.appendText(formattedMsg + "\n");
							currentConversation.addTextToConv(formattedMsg);
						}
						else if (message.contains("@" + clientName) && currentConversation.receivers.size() == 1) {
							int endSenderName = message.indexOf(":");
							String senderName = message.substring(0, endSenderName).trim();
							int startName = message.indexOf("@");
							String formattedMsg = message.substring(0, startName);
							formattedMsg += message.substring(startName + clientName.length() + 1).trim();
							if (senderName.equals(currentConversation.receivers.get(0))) {
								if (chatSpace != null ) 
									chatSpace.appendText(formattedMsg + "\n");
								currentConversation.addTextToConv(formattedMsg);
							} else {
								for (Conversation c: conversations) {
									if (c.receivers.size() == 1) {
										if (c.receivers.get(0).equals(senderName)) 
											c.addTextToConv(formattedMsg);
									}
								}
							}
						}
						else if (!message.contains("@")) {
							chatSpace.appendText(message + "\n");
						}
						else {
							System.out.println("none case");
						}
					}
//				}
			} catch (IOException ex) {
				ex.printStackTrace();
			} 
		}
	}
}
