package GUI;

import GUI.controller.main.MainController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import module3.storage.AudioStorage;
import module3.storage.FileManager;
import module3.storage.PlaylistStorage;
import java.io.IOException;

/**
 * The primary entry point for the JavaFX application.
 * <p>
 *     This class serves as the composition root for the application. It handles
 *     the initialization of the primary window (Stage), loads the root FXML layout,
 *     and wires up the core dependencies. It also manages the data persistence
 *     lifecycle by reading JSON files on startup and adding a shutdown hook to
 *     save the state when the user closes the app.
 * </p>
 */

public class Main extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        // Load mainscreen
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
        mainController.setStorage(audioStorage, playlistStorage);

        stage.setTitle("Music Library Manager");
        stage.setMinWidth(960);
        stage.setMinHeight(650);
        stage.setMaximized(true);

        stage.setScene(scene);

        stage.setOnCloseRequest(event -> {
            fileManager.saveAudio(audioStorage.getAllItems());
            fileManager.savePlaylist(playlistStorage.getAllPlaylists());
        });

        stage.show();
    }
}

