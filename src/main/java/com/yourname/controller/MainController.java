package com.yourname.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.application.Platform;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

/**
 * MainController — owns the BorderPane shell.
 *
 * Responsibilities:
 *   • Swap the CENTER content area between LibraryView and PlaylistView
 *   • Own the playback bar (bottom) and delegate to PlaybackBarController
 *   • Own the sidebar nav state (which item is "active")
 *   • Be the single place that holds a reference to your backend Controller facade
 */
public class MainController implements Initializable {

    // ── Injected from FXML ────────────────────────────────────────────────────

    /* TOP BAR */
    @FXML private TextField searchField;

    /* SIDEBAR */
    @FXML private Button   btnAllTracks;
    @FXML private Button   btnFavourites;
    @FXML private TextField playlistSearchField;
    // All playlist names — never changes
    private List<String> allPlaylists = new ArrayList<>();

    // What the ListView currently shows
    private ObservableList<String> displayedPlaylists = FXCollections.observableArrayList();
    @FXML private ListView<String> playlistNavList;   // String = playlist title for now

    /* CENTER */
    @FXML private StackPane contentArea;

    /* BOTTOM — playback */
    @FXML private Label  nowPlayingTitle;
    @FXML private Label  nowPlayingArtist;
    @FXML private Button btnPlayPause;
    @FXML private Button btnPrevious;
    @FXML private Button btnNext;
    @FXML private Button btnShuffle;
    @FXML private Button btnRepeat;
    @FXML private Slider progressSlider;
    @FXML private Slider volumeSlider;
    @FXML private Label  currentTimeLabel;
    @FXML private Label  totalTimeLabel;

    // ── Child controllers (set via loader after swapping views) ───────────────
    private LibraryController  libraryController;
    private PlaylistController playlistController;

    // ── Backend facade — wire this once your Controller class is accessible ──
    // private playback.Controller backend;

    // ── State ─────────────────────────────────────────────────────────────────
    private Button activeNavButton;   // tracks which sidebar button is highlighted
    private enum ActiveView { LIBRARY, PLAYLIST, NONE }
    private ActiveView currentView = ActiveView.NONE;

    // ─────────────────────────────────────────────────────────────────────────
    @Override
    public void initialize(URL url, ResourceBundle rb) {

        // 1. Mark "All Tracks" as the default active nav item
        setActiveNav(btnAllTracks);

        // 2. Load the Library view into CENTER on startup
        showLibrary();

        // 3. Populate playlist sidebar — replace with real PlaylistStorage data in Phase 6
        allPlaylists.addAll(List.of("Late Night Jazz", "Workout Mix", "Focus Mode"));
        displayedPlaylists.setAll(allPlaylists);
        playlistNavList.setItems(displayedPlaylists);

        // 4. Clicking a playlist name in the sidebar opens the PlaylistView
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

        // 5. Volume slider initial sync
        // Uncomment once backend is wired:
        // backend.setVolume(volumeSlider.getValue());
        volumeSlider.valueProperty().addListener((obs, oldVal, newVal) ->
                handleVolumeChange()
        );
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  VIEW SWITCHING
    // ─────────────────────────────────────────────────────────────────────────

    /** Load LibraryView.fxml into the CENTER pane. */
    @FXML
    public void showLibrary() {
        currentView = ActiveView.LIBRARY;
        playlistNavList.getSelectionModel().clearSelection();
        setActiveNav(btnAllTracks);
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/fxml/LibraryView.fxml")
            );
            Parent libraryView = loader.load();
            contentArea.getChildren().setAll(libraryView);
            libraryController = loader.getController();

            // Pass the search text if already typed
            // libraryController.setBackend(backend);

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

    @FXML
    private void handleSort() {

        switch (currentView) {
            case LIBRARY  -> { if (libraryController  != null) libraryController.cycleSortOrder();   }
            case PLAYLIST -> { if (playlistController != null) playlistController.cycleSortOrder();  }
            case NONE     -> {}
        }
    }

    @FXML
    private void handleFilter() {
        switch (currentView) {
            case LIBRARY  -> { if (libraryController  != null) libraryController.showFilterDialog();  }
            case PLAYLIST -> { if (playlistController != null) playlistController.showFilterDialog(); }
            case NONE     -> {}
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  SIDEBAR HANDLERS
    // ─────────────────────────────────────────────────────────────────────────

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
                playlistNavList.getItems().add(name);    // optimistic UI update
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

    // ─────────────────────────────────────────────────────────────────────────
    //  PLAYBACK BAR HANDLERS
    // ─────────────────────────────────────────────────────────────────────────

    @FXML
    private void handlePlayPause() {
        // backend.pause() / backend.resume()   — wire in Phase 5
        boolean isPlaying = btnPlayPause.getText().equals("⏸");
        btnPlayPause.setText(isPlaying ? "▶" : "⏸");
    }

    @FXML
    private void handlePrevious() {
        // backend.playPrevious();
    }

    @FXML
    private void handleNext() {
        // backend.playNext();
    }

    @FXML
    private void handleShuffle() {
        // backend.shuffle();
    }

    @FXML
    private void handleRepeat() {
        // toggle repeat mode
    }

    @FXML
    private void handleProgressPressed() {
        // Pause timeline updates while user is dragging
    }

    @FXML
    private void handleProgressReleased() {
        // backend.seek(progressSlider.getValue());
    }

    @FXML
    private void handleVolumeChange() {
        // backend.setVolume(volumeSlider.getValue());
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  PUBLIC API — called by child controllers
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Called by LibraryController or PlaylistController when the user
     * double-clicks a track to start playing it.
     */
    public void playTrack(String trackId, String title, String artist) {
        nowPlayingTitle.setText(title);
        nowPlayingArtist.setText(artist);
        btnPlayPause.setText("⏸");
        // backend.loadSingle(audioItem);   — wire in Phase 5
    }

    /**
     * Called by a background Timeline every second to update the progress bar.
     * Wire this up in Phase 5 with:
     *   Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(1), e -> tickProgress()));
     *   timeline.setCycleCount(Timeline.INDEFINITE);
     *   timeline.play();
     */
    public void tickProgress(double currentSeconds, double totalSeconds) {
        Platform.runLater(() -> {
            progressSlider.setValue((currentSeconds / totalSeconds) * 100);
            currentTimeLabel.setText(formatTime((int) currentSeconds));
            totalTimeLabel.setText(formatTime((int) totalSeconds));
        });
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  HELPERS
    // ─────────────────────────────────────────────────────────────────────────

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
