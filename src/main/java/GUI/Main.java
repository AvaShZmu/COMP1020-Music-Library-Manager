package GUI;

import GUI.controller.MainController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.stage.Screen;
import javafx.stage.Stage;
import module3.storage.AudioStorage;
import module3.storage.FileManager;
import module3.storage.PlaylistStorage;

import java.io.IOException;

public class Main extends Application {
    @Override
    public void start(Stage stage) throws IOException {

        FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("/fxml/MainScreen.fxml"));
        Scene scene = new Scene(fxmlLoader.load(),820,480);

        // Initialize FileManager and Storage
        FileManager fileManager = new FileManager("library.json", "playlist.json");
        AudioStorage audioStorage = new AudioStorage();
        PlaylistStorage playlistStorage = new PlaylistStorage();

        // Load data
        audioStorage.importAudio(fileManager.loadAudio());
        playlistStorage.importPlaylists(fileManager.loadPlaylist());

        // Pass to MainController
        MainController mainController = fxmlLoader.getController();
        mainController.setStorage(audioStorage);

        Rectangle2D visualBounds = Screen.getPrimary().getVisualBounds();
        stage.setTitle("Music Library Manager");
        stage.setMinWidth(940);
        stage.setMinHeight(visualBounds.getHeight());
        stage.setMaximized(true);

        stage.setScene(scene);

        stage.setOnCloseRequest(event -> {
            fileManager.saveAudio(audioStorage.getAllItems());
            fileManager.savePlaylist(playlistStorage.getAllPlaylists());
        });

        stage.show();
    }
}

