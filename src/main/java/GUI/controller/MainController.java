package GUI.controller;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;
import module1.audioModel.AudioItem;
import module3.storage.AudioStorage;
import module4.playback.Controller;

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
    @FXML private ComboBox<String> sortComboBox;
    @FXML private Button btnFilter;
    @FXML private Button btnClearFilter;

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

    /* BOTTOM — playback */
    @FXML private Label  nowPlayingTitle;
    @FXML private Label  nowPlayingArtist;
    @FXML private Button btnPlayPause;
    @FXML private Button btnPrevious;
    @FXML private Button btnNext;
    //@FXML private Button btnShuffle;
    //@FXML private Button btnRepeat;
    @FXML private Slider progressSlider;
    @FXML private Slider volumeSlider;
    @FXML private Label  currentTimeLabel;
    @FXML private Label  totalTimeLabel;

    private Timeline progressTimeline;

    // ── Child controllers (set via loader after swapping views) ───────────────
    private LibraryController  libraryController;
    private PlaylistController playlistController;

    // ── Backend facade
    private Controller playbackController;
    private AudioStorage audioStorage;

    // ── State ─────────────────────────────────────────────────────────────────
    private Button activeNavButton;   // tracks which sidebar button is highlighted
    private enum ActiveView { LIBRARY, PLAYLIST, NONE }
    private ActiveView currentView = ActiveView.NONE;

    // ─────────────────────────────────────────────────────────────────────────
    @Override
    public void initialize(URL url, ResourceBundle rb) {

        playbackController = new Controller();
        audioStorage = new AudioStorage();

        // Attach UI callback to Controller:
        playbackController.setOnTrackChangedListener(() -> {
            Platform.runLater(this::updateNowPlaying);
        });

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
        volumeSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            // 1. Update the backend volume
            if (playbackController != null) {
                playbackController.setVolume(newVal.doubleValue());
            }

            // 2. Dynamically color the track
            javafx.scene.Node track = volumeSlider.lookup(".track");
            if (track != null) {
                // Calculate the percentage safely based on whatever your slider's max is
                double percentage = (newVal.doubleValue() / volumeSlider.getMax()) * 100.0;

                // Format CSS: White for active volume, Gray for remaining
                String style = String.format(
                        "-fx-background-color: linear-gradient(to right, #ffffff %f%%, #535353 %f%%);",
                        percentage, percentage
                );
                track.setStyle(style);
            }
        });

        sortComboBox.getItems().addAll(
                "Title A → Z",
                "Title Z → A",
                "Newest first",
                "Oldest first"
        );
        sortComboBox.setValue("Title A → Z");  // default selection

        // Listener to dynamically color the track as it progresses
        progressSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            // Look up the track node inside the slider
            javafx.scene.Node track = progressSlider.lookup(".track");
            if (track != null) {
                double percentage = newVal.doubleValue(); // Assuming max is 100

                // Format a CSS linear gradient: green (#1db954) for passed, gray (#535353) for remaining
                String style = String.format(
                        "-fx-background-color: linear-gradient(to right, #ffffff %f%%, #535353 %f%%);",
                        percentage, percentage
                );
                track.setStyle(style);
            }
        });

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
            if(libraryView == null) {
                FXMLLoader loader = new FXMLLoader(
                        getClass().getResource("/fxml/LibraryView.fxml")
                );
                libraryView = loader.load();
                libraryController = loader.getController();
                libraryController.setMainController(this);
                libraryController.setAudioStorage(audioStorage);
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
        String selected = sortComboBox.getValue();
        if (selected == null) return;

        switch (currentView) {
            case LIBRARY  -> { if (libraryController  != null) libraryController.applySortOrder(selected);  }
            case PLAYLIST -> { if (playlistController != null) playlistController.applySortOrder(selected); }
            case NONE     -> { }
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

    public void showClearFilter(String label){
        btnClearFilter.setText("✕ " + label);
        btnClearFilter.setVisible(true);
        btnClearFilter.setManaged(true);

        btnFilter.getStyleClass().add("active");
    }

    @FXML
    public void handleClearFilter() {
        if(libraryController!=null){
            libraryController.clearFilter();
        }
        btnClearFilter.setVisible(false);
        btnClearFilter.setManaged(false);

        btnFilter.getStyleClass().remove("active");
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

    // ─────────────────────────────────────────────────────────────────────────
    //  PLAYBACK BAR HANDLERS
    // ─────────────────────────────────────────────────────────────────────────

    @FXML
    private void handlePlayPause() {
        if(btnPlayPause.getText().equals("⏸")){
            playbackController.pause();
            btnPlayPause.setText("▶");
        }
        else{
            playbackController.resume();
            btnPlayPause.setText("⏸");
        }
    }

    @FXML
    private void handlePrevious() {
        playbackController.playPrevious();
    }

    @FXML
    private void handleNext() {
        playbackController.playNext();
    }
    /*
    @FXML
    private void handleShuffle() {
        playbackController.shuffle();
    }

    @FXML
    private void handleRepeat() {
        // toggle repeat mode
    }
     */

    @FXML
    private void handleProgressPressed() {
        if(progressTimeline != null){
            progressTimeline.pause();
        }
    }

    @FXML
    private void handleProgressReleased() {
        double total  = playbackController.getDuration().toSeconds();
        System.out.println(progressSlider.getValue());
        double seekTo = (progressSlider.getValue() / 100.0) * total;
        System.out.println(seekTo);
        playbackController.setTime(seekTo);

        // Resume timeline after seek
        if (progressTimeline != null) {
            progressTimeline.play();
        }
    }

    @FXML
    private void handleVolumeChange() {
        playbackController.setVolume(volumeSlider.getValue());
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  PUBLIC API — called by child controllers
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Called by LibraryController or PlaylistController when the user
     * double-clicks a track to start playing it.
     */
    public void playTrack(AudioItem item) {
        // Look up the AudioItem from AudioStorage directly
        if(item == null) {
            return;
        }

        // 3. Tell playback Controller to load and play
        //    Replace loadSingle with your actual method name
        // Clear whatever was in the queue
        playbackController.clearQueue();

        // Load the item and play it directly
        playbackController.loadSingle(item);
        playbackController.startPlayBack(item);

    }

    public void addToQueue(AudioItem item){
        if (playbackController.getCurrentTrack() == null) {
            playTrack(item);
        }
        else{
            playbackController.loadSingle(item);
        }
    }

    private void updateNowPlaying() {
        AudioItem current = playbackController.getCurrentTrack();
        if (current == null) return;

        nowPlayingTitle.setText(current.getTitle());
        nowPlayingArtist.setText(current.getAuthor());
        btnPlayPause.setText("⏸");

        // Update card highlight in library if visible
        if (libraryController != null) {
            libraryController.highlightPlayingCard(current.getTrackID());
        }

        // Restart timeline for new track
        startProgressTimeline();
    }

    private void startProgressTimeline() {
        // Stop any existing timeline first
        if (progressTimeline != null) {
            progressTimeline.stop();
        }

        progressTimeline = new Timeline(
                new KeyFrame(Duration.seconds(1), e -> tickProgress())
        );
        progressTimeline.setCycleCount(Timeline.INDEFINITE);
        progressTimeline.play();
    }

    private void tickProgress() {
        double current = playbackController.getCurrentTime().toSeconds();
        double total   = playbackController.getDuration().toSeconds();

        if (total <= 0) return;

        // Update slider position
        progressSlider.setValue((current / total) * 100);

        // Update time labels
        currentTimeLabel.setText(formatTime((int) current));
        totalTimeLabel.setText(formatTime((int) total));
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

    private void printQueue(){
        for(AudioItem item : playbackController.getQueue()){
            System.out.print(item.getTitle() + " ");
        }
        System.out.println();
    }
}
