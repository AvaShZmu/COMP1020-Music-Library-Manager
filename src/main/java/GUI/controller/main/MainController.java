package GUI.controller.main;

import GUI.controller.playback.PlaybackBarController;
import GUI.controller.playlist.PlaylistController;
import GUI.controller.queue.QueueController;
import GUI.controller.library.LibraryController;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.StackPane;
import module2.playlistModel.Playlist;
import module3.storage.AudioStorage;
import module3.storage.PlaylistStorage;
import module5.util.LibraryLogic;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

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
    @FXML private TextField playlistSearchField;
    private final List<Playlist> allPlaylists = new ArrayList<>();
    private final ObservableList<Playlist> displayedPlaylists = FXCollections.observableArrayList();
    @FXML private ListView<Playlist> playlistNavList;

    /* CENTER */
    @FXML private StackPane contentArea;
    private Parent libraryView = null;
    private Parent playlistView = null;

    /* Playbar */
    @FXML private StackPane bottomContainer;
    private Parent playbackBarView;

    /* Queue */
    @FXML private StackPane rightContainer;
    private Parent queueView = null;

    // Child controllers
    private LibraryController libraryController;
    private PlaylistController playlistController;
    private PlaybackBarController playbackBarController;
    private QueueController queueController;

    // Backend storage
    private AudioStorage audioStorage;
    private PlaylistStorage playlistStorage;

    // State
    private Button activeNavButton;   // tracks which sidebar button is highlighted
    private enum ActiveView { LIBRARY, PLAYLIST, NONE }
    private ActiveView currentView = ActiveView.NONE;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Load bottom bar first
        showPlaybackBar();

        // Mark "All Tracks" as the default active nav item
        setActiveNav(btnAllTracks);

        // Load queue
        showQueue();

        // Load the Library view into CENTER on startup
        showLibrary();

        // Clicking a playlist name in the sidebar opens the PlaylistView
        playlistNavList.setOnMouseClicked(event -> {
            Playlist selected = playlistNavList.getSelectionModel().getSelectedItem();
            if(event.getButton() == MouseButton.PRIMARY){
                currentView = ActiveView.PLAYLIST;
                if (selected != null) {
                    if (activeNavButton != null) {
                        activeNavButton.getStyleClass().remove("active");
                        activeNavButton = null;
                    }
                    showPlaylist(selected);
                }
            }
        });

        playlistNavList.setCellFactory(lv -> {
            ListCell<Playlist> cell = new ListCell<>();

            cell.textProperty().bind(
                    Bindings.createStringBinding(() -> {
                        Playlist item = cell.getItem();
                        return item == null ? "" : item.getTitle();
                    }, cell.itemProperty())
            );

            ContextMenu menu = new ContextMenu();

            MenuItem remove = new MenuItem("Remove playlist");

            remove.setOnAction(e -> {
                Playlist item = cell.getItem();
                if (item != null) {
                    handleRemovePlaylist(item);
                }
            });

            menu.getItems().add(remove);

            cell.contextMenuProperty().bind(
                    Bindings.when(cell.emptyProperty())
                            .then((ContextMenu) null)
                            .otherwise(menu)
            );

            return cell;
        });
    }

    private void handleRemovePlaylist(Playlist playlist){
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Remove Playlist");
        confirm.setHeaderText("Remove \"" + playlist.getTitle() + "\"?");
        confirm.setContentText(
                "This removes this playlist from your library. "
        );

        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                playlistStorage.removeItem(playlist);
                allPlaylists.remove(playlist);
                String query = playlistSearchField.getText().trim();
                searchSidebarPlaylists(query);
            }
        });
    }

    public void setStorage(AudioStorage audioStorage, PlaylistStorage playlistStorage) {
        this.audioStorage = audioStorage;
        this.playlistStorage = playlistStorage;


        if (libraryController != null) {
            libraryController.setPlaylistStorage(playlistStorage);
            libraryController.setAudioStorage(audioStorage);
            if (this.playlistStorage != null) {
                allPlaylists.clear();
                allPlaylists.addAll(this.playlistStorage.getAllPlaylists());
            }
            displayedPlaylists.addAll(allPlaylists);
            playlistNavList.setItems(displayedPlaylists);
        }
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
                //libraryController.setAudioStorage(audioStorage);
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

    /** Load PlaylistView.fxml for a specific playlist. */
    public void showPlaylist(Playlist playlist) {
        try {
            if(playlistView == null) {
                FXMLLoader loader = new FXMLLoader(
                        getClass().getResource("/fxml/PlaylistView.fxml")
                );
                playlistView = loader.load();
                playlistController = loader.getController();

                //playlistController.setMainController(this);
                playlistController.setAudioStorage(audioStorage);

                playlistController.setPlaybackBarController(playbackBarController);

                //if (playbackBarView != null) {
                //    playbackBarController.setLibraryController(libraryController);
                //}
            }
            contentArea.getChildren().setAll(playlistView);
            playlistController.loadPlaylist(playlist);

        } catch (IOException e) {
            showError("Could not load Library view", e);
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
                Playlist newPlaylist = new Playlist(name,
                        "Write description here.",
                        LocalDate.now().format(DateTimeFormatter.ofPattern("dd MMMM yyyy")));
                playlistStorage.addItem(newPlaylist);
                allPlaylists.add(newPlaylist);
                String query = playlistSearchField.getText().trim();
                searchSidebarPlaylists(query);
                playlistNavList.getSelectionModel().select(newPlaylist);
                showPlaylist(newPlaylist);
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
            displayedPlaylists.clear();
            displayedPlaylists.addAll(allPlaylists);
            return;
        }
        displayedPlaylists.clear();
        displayedPlaylists.addAll(LibraryLogic.search(allPlaylists, query));
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

    private void showError(String message, Exception e) {
        e.printStackTrace();
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(message);
        alert.setContentText(e.getMessage());
        alert.showAndWait();
    }
}
