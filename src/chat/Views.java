package chat;

import java.io.IOException;

import javafx.application.Application;
import javafx.scene.control.TextArea;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class Views extends Application {

	public static void main(String[] args) {
		launch(args);
	}
	
	@Override
	public void start(Stage stage) throws Exception {
		VBox vbox = new VBox(5);
		Stage nextStage = new Stage();
		VBox nextVbox = new VBox(5);
		try {
			Painter.addWindow(stage, vbox, new ChatClient());
			Painter.addWindow(nextStage, nextVbox, new ChatClient());
		} catch (Exception e) {
			e.printStackTrace();
		}		
	}

}
