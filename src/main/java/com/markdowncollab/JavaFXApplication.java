package com.markdowncollab;

import com.markdowncollab.ui.EditorUI;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;

public class JavaFXApplication extends Application {
    private ConfigurableApplicationContext springContext;
    
    @Override
    public void init() {
        // Start the Spring Boot application
        springContext = new SpringApplicationBuilder(MarkdownCollabApplication.class)
                .headless(false)
                .run();
    }
    
    @Override
    public void start(Stage primaryStage) {
        // Get the EditorUI from the Spring context
        EditorUI editorUI = springContext.getBean(EditorUI.class);
        editorUI.initialize(primaryStage);
    }
    
    @Override
    public void stop() {
        // Close the Spring context when JavaFX application stops
        springContext.close();
        Platform.exit();
    }
    
    public static void main(String[] args) {
        // Launch the JavaFX application
        launch(args);
    }
}