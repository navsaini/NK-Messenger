package chat;

import java.util.ArrayList;

import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class Painter {
	public static ArrayList<VBox> vboxes = new ArrayList<VBox>();
	
	public static void addWindow(Stage stage, VBox vbox, ChatClient c) {
		try {
			c.run();
		} catch (Exception e) {
			e.printStackTrace();
		}
		vbox.setPadding(new Insets(5));
		
		TextArea chatSpace = new TextArea("begin chatting now...\n");
		chatSpace.setEditable(false);
		vbox.getChildren().add(chatSpace);
		
		TextField messageToSend = new TextField();
		vbox.getChildren().add(messageToSend);
		
		Button sendButton = new Button("Send");
		sendButton.setOnAction(c.new SendButtonListener(messageToSend));
		vbox.getChildren().add(sendButton);
		vboxes.add(vbox);
		
		stage.setScene(new Scene(vbox, 300, 300));
	    stage.setX(0);
	    stage.setY(0);
	    stage.show();
	}
	
}
