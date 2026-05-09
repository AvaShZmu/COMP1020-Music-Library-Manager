package com.yourname;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class Main extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("/fxml/MainScreen.fxml"));
        Scene scene = new Scene(fxmlLoader.load(),820,480);

        stage.setTitle("Music Library Manager");
        stage.setMinWidth(900);
        stage.setMinHeight(600);

        stage.setScene(scene);

        stage.setOnCloseRequest(event -> {
            // FileManager.getInstance().saveAll();
            // Uncomment once FileManager is wired in
        });

        stage.show();
    }
}

