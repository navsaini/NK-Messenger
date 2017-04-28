package chat;

import java.io.IOException;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class Views extends Application {
	private static int numTalking = 0;
	
	public static void main(String[] args) {
		launch(args);
	}
	
	@Override
	public void start(Stage stage) throws Exception {
		promptCount();
//		new ChatClient("Nav").run();
//		new ChatClient("Kausthub").run();
//		new ChatClient("Moez").run();
	}
	
	public static void promptCount() {
		Stage firstStage = new Stage();
		VBox promptAmountVbox = new VBox(5);
		
		promptAmountVbox.setPadding(new Insets(5));
		
		Label promptMessage = new Label("How many people are chatting?");
		promptMessage.setWrapText(true);
		promptAmountVbox.getChildren().add(promptMessage);
		
		TextField promptAmountField = new TextField();
		promptAmountVbox.getChildren().add(promptAmountField);
		
		Button acceptAmount = new Button("Continue");
		promptAmountVbox.getChildren().add(acceptAmount);
		
		acceptAmount.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				numTalking = Integer.parseInt(promptAmountField.getText());
				acceptAmount.getParent().getScene().getWindow().hide();
				promptNames();
			}
		});
		
		firstStage.setScene(new Scene(promptAmountVbox, 250, 100));
	    firstStage.setX(0);
	    firstStage.setY(0);
	    firstStage.show();
	}
	
	public static void promptNames() {
		Stage stage = new Stage();
		VBox vbox = new VBox(5);
		
		vbox.setPadding(new Insets(5));
		
		Label promptNames = new Label("Enter your names");
		vbox.getChildren().add(promptNames);
		
		for (int k = 0; k < numTalking; k++) {
			TextField nameField = new TextField();
			vbox.getChildren().add(nameField);
		}
		
		Button acceptNames = new Button("Start chatting!");
		acceptNames.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				acceptNames.getParent().getScene().getWindow().hide();
				for (Object child: vbox.getChildren()) {
					if (child instanceof TextField) {
						String name = ((TextField) child).getText().replaceAll("[^a-zA-Z0-9]","");
						createClient(name);
					}
				}
			}
		});
		
		vbox.getChildren().add(acceptNames);
		
		stage.setScene(new Scene(vbox, 250, numTalking * 50 + 50));
		stage.setX(0);
	    stage.setY(0);
	    stage.show();
	}
	
	public static void createClient(String name) {
		try {
			new ChatClient(name).run();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
