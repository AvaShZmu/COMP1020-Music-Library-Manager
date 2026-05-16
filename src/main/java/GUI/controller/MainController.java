package GUI.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import module3.storage.AudioStorage;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

/**
 * MainController — owns the BorderPane shell.*
 * Responsibilities:
 *   • Swap the CENTER content area between LibraryView and PlaylistView
 *   • Own the sidebar nav state (which item is "active")
 *   • Be the single place that holds a reference to your backend Controller facade
 */
public class MainController implements Initializable {

    // Injected from FXML

    /* TOP BAR */
    @FXML private TextField searchField;

    /* SIDEBAR */
    @FXML private Button   btnAllTracks;
    @FXML private Button   btnFavourites;
    @FXML private TextField playlistSearchField;
    private final List<String> allPlaylists = new ArrayList<>();
    private final ObservableList<String> displayedPlaylists = FXCollections.observableArrayList();
    @FXML private ListView<String> playlistNavList;   // String = playlist title for now

    /* CENTER */
    @FXML private StackPane contentArea;
    private Parent libraryView = null;

    /* Playbar */
    @FXML private StackPane bottomContainer;
    private Parent playbackBarView;

    /* Queue */
    @FXML private StackPane rightContainer;
    private Parent queueView = null;

    // Child controllers
    private LibraryController  libraryController;
    private PlaylistController playlistController;
    private PlaybackBarController playbackBarController;
    private QueueController queueController;

    // Backend facade
    private AudioStorage audioStorage;

    // State
    private Button activeNavButton;   // tracks which sidebar button is highlighted
    private enum ActiveView { LIBRARY, PLAYLIST, NONE }
    private ActiveView currentView = ActiveView.NONE;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        audioStorage = new AudioStorage();

        // Load bottom bar first
        showPlaybackBar();

        // Mark "All Tracks" as the default active nav item
        setActiveNav(btnAllTracks);

        // Load queue
        showQueue();

        // Load the Library view into CENTER on startup
        showLibrary();

        // Populate playlist sidebar — replace with real PlaylistStorage data in Phase 6
        allPlaylists.addAll(List.of("Late Night Jazz", "Workout Mix", "Focus Mode"));
        displayedPlaylists.setAll(allPlaylists);
        playlistNavList.setItems(displayedPlaylists);

        // Clicking a playlist name in the sidebar opens the PlaylistView
        playlistNavList.setOnMouseClicked(event -> {
            String selected = playlistNavList.getSelectionModel().getSelectedItem();
            currentView = ActiveView.PLAYLIST;
            if (selected != null) {
                if (activeNavButton != null) {
                    activeNavButton.getStyleClass().remove("active");
                    activeNavButton = null;
                }
                showPlaylist(selected);
            }
        });


    }

    // ─────────────────────────────────────────────────────────────────────────
    //  VIEW LOADERS
    // ─────────────────────────────────────────────────────────────────────────

    /** Load LibraryView.fxml into the CENTER pane. */
    @FXML
    public void showLibrary() {
        currentView = ActiveView.LIBRARY;
        playlistNavList.getSelectionModel().clearSelection();
        setActiveNav(btnAllTracks);
        try {
            if(libraryView == null) {
                FXMLLoader loader = new FXMLLoader(
                        getClass().getResource("/fxml/LibraryView.fxml")
                );
                libraryView = loader.load();
                libraryController = loader.getController();
                libraryController.setMainController(this);
                libraryController.setAudioStorage(audioStorage);

                libraryController.setPlaybackBarController(playbackBarController);

                if (playbackBarView != null) {
                    playbackBarController.setLibraryController(libraryController);
                }
            }
            contentArea.getChildren().setAll(libraryView);

        } catch (IOException e) {
            showError("Could not load Library view", e);
        }
    }

    /** Load LibraryView filtered to favourites. */
    @FXML
    public void showFavourites() {
        currentView = ActiveView.LIBRARY;
        playlistNavList.getSelectionModel().clearSelection();
        setActiveNav(btnFavourites);
        // For now, reuse LibraryView — in Phase 3 pass a filter flag
        showLibrary();
    }

    /** Load PlaylistView.fxml for a specific playlist. */
    public void showPlaylist(String playlistTitle) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/fxml/PlaylistView.fxml")
            );
            Parent playlistView = loader.load();
            contentArea.getChildren().setAll(playlistView);
            playlistController = loader.getController();
            playlistController.loadPlaylist(playlistTitle);

        } catch (IOException e) {
            showError("Could not load Playlist view", e);
        }
    }

    public void showPlaybackBar() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/fxml/PlaybackBar.fxml")
            );
            playbackBarView = loader.load();
            playbackBarController = loader.getController();

            // Add main controller to playback
            playbackBarController.setMainController(this);

            bottomContainer.getChildren().setAll(playbackBarView);
        } catch (IOException e) {
            showError("Could not load PlaybackBar view", e);
        }
    }

    public void showQueue() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/fxml/QueueView.fxml")
            );
            queueView = loader.load();
            queueController = loader.getController();

            if (playbackBarController != null) {
                queueController.setPlaybackBarController(playbackBarController);
                playbackBarController.setQueueController(queueController);
            }
            rightContainer.getChildren().setAll(queueView);
        } catch (IOException e) {
            showError("Could not load QueueView view", e);
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  TOP BAR HANDLERS
    // ─────────────────────────────────────────────────────────────────────────

    @FXML
    private void handleSearch() {
        String query = searchField.getText().trim();

        switch (currentView) {
            case LIBRARY  -> { if (libraryController  != null) libraryController.applySearch(query);  }
            case PLAYLIST -> { if (playlistController != null) playlistController.applySearch(query); }
            case NONE     -> { /* nothing loaded yet, do nothing */ }
        }
    }


    /* Left side */

    @FXML
    private void handleNewPlaylist() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("New Playlist");
        dialog.setHeaderText("Create a new playlist");
        dialog.setContentText("Playlist name:");

        dialog.showAndWait().ifPresent(name -> {
            if (!name.isBlank()) {
                // backend.createPlaylist(name);          // wire in Phase 6
                allPlaylists.add(name);
                displayedPlaylists.add(name);
                String query = playlistSearchField.getText().trim();
                searchSidebarPlaylists(query);
                //playlistNavList.getItems().add(name);    // optimistic UI update
                showPlaylist(name);
            }
        });
    }

    @FXML
    private void handlePlaylistSearch() {
        String query = playlistSearchField.getText().trim();
        searchSidebarPlaylists(query);
    }

    private void searchSidebarPlaylists(String query) {
        if (query.isBlank()) {
            displayedPlaylists.setAll(allPlaylists);
            return;
        }
        List<String> filtered = allPlaylists.stream()
                .filter(name -> name.toLowerCase().contains(query.toLowerCase()))
                .collect(Collectors.toList());
        displayedPlaylists.setAll(filtered);
    }

    /* Right side (Queues) */
    public void toggleQueue() {
        boolean isHidden = !rightContainer.isVisible();

        if (isHidden) {
            rightContainer.setVisible(true);
            rightContainer.setManaged(true);
        }
        else {
            rightContainer.setVisible(false);
            rightContainer.setManaged(false);
        }

    }

    /* Helpers */

    /** Highlights the active sidebar button and removes highlight from previous. */
    private void setActiveNav(Button button) {
        if (activeNavButton != null) {
            activeNavButton.getStyleClass().remove("active");
        }
        button.getStyleClass().add("active");
        activeNavButton = button;
    }

    /** Formats seconds → "m:ss" string for time labels. */
    private String formatTime(int totalSeconds) {
        int minutes = totalSeconds / 60;
        int seconds = totalSeconds % 60;
        return String.format("%d:%02d", minutes, seconds);
    }

    private void showError(String message, Exception e) {
        e.printStackTrace();
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(message);
        alert.setContentText(e.getMessage());
        alert.showAndWait();
    }
}
