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
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.event.ActionEvent;

public class ChatClient  {
	private BufferedReader reader;
	private PrintWriter writer;
	private TextArea chatSpace;
	private TextField messageToSend;
	
	private String clientName;
	
	private Object lock = new Object();
	
	private static int xPos = 0;
	private static int yPos = 0;
	
	public ChatClient(String clientName) {
		this.clientName = clientName;
	}
	
	public void run() throws Exception {
		Stage stage = new Stage();
		VBox vbox = new VBox();
		initView(stage, vbox);
		setUpNetworking();
	}

	private void initView(Stage stage, VBox vbox) {
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
			writer.println(clientName + ": " + messageToSend.getText());
			writer.flush();
			if (messageToSend.getText().contains("@"))
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
	
//	messageToSend.setKeyOnPressed(new EventHandler<KeyEvent>()
//    {
//        @Override
//        public void handle(KeyEvent ke)
//        {
//            if (ke.getCode().equals(KeyCode.ENTER))
//            {
//            	writer.println(clientName + ": " + messageToSend.getText());
//    			writer.flush();
//    			if (messageToSend.getText().contains("@"))
//    				chatSpace.appendText(clientName + ": " + messageToSend.getText() + "\n");
//    			messageToSend.setText("");
//            }
//        }
//    });
	
	/**
	 * reads incoming messages from the server
	 *
	 */
	class IncomingReader implements Runnable {
		public void run() {
			String message;
			try {
				synchronized (lock) {
					while ((message = reader.readLine()) != null) {
						if (message.contains("@" + clientName))
							chatSpace.appendText(message + "\n");
						else if (!message.contains("@"))
							chatSpace.appendText(message + "\n");
					}
				}
			} catch (IOException ex) {
				ex.printStackTrace();
			} 
		}
	}
}
