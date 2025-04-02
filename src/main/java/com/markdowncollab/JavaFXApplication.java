package com.markdowncollab;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class JavaFXApplication extends Application {
    
    @Override
    public void start(Stage stage) {
        // Your startup code here
        Label label = new Label("Hello, JavaFX!");
        Scene scene = new Scene(new StackPane(label), 640, 480);
        stage.setScene(scene);
        stage.setTitle("Markdown Editor");
        stage.show();
    }
    
    public static void main(String[] args) {
        launch(args);
    }
}