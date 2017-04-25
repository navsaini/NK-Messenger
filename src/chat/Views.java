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
		new ChatClient("Nav").run();
		new ChatClient("Kausthub").run();
	}

}
