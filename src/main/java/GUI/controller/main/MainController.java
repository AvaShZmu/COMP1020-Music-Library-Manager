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
 * The root UI controller, which acts as the central facade for the application.
 * <p>
 *     This class manages the BorderPane layout. It is responsible for
 *     coordinating sidebar navigation, injecting shared backend dependencies (storage modules),
 *     and swapping the center content area between the main library and individual playlists.
 * </p>
 */

public class MainController implements Initializable {

    /* FXML Bindings */

    /** The global search bar in the top navigation header. */
    @FXML private TextField searchField;

    /** The sidebar button used to navigate back to the main "All Tracks" library view. */
    @FXML private Button   btnAllTracks;

    /** The search bar used exclusively for filtering the sidebar playlist list. */
    @FXML private TextField playlistSearchField;

    /** The list view displaying all playlists in the sidebar. */
    @FXML private ListView<Playlist> playlistNavList;

    /** The central content pane for library/playlist views. */
    @FXML private StackPane contentArea;

    /** The bottom pane for playback bar. */
    @FXML private StackPane bottomContainer;

    /** The right-side pane for queue. */
    @FXML private StackPane rightContainer;

    /* Child controllers and views */

    private Parent libraryView = null;
    private Parent playlistView = null;
    private Parent playbackBarView = null;
    private Parent queueView = null;

    private LibraryController libraryController;
    private PlaylistController playlistController;
    private PlaybackBarController playbackBarController;
    private QueueController queueController;

    /* Backend storage */

    private AudioStorage audioStorage;
    private PlaylistStorage playlistStorage;

    /* Internal state */

    private final List<Playlist> allPlaylists = new ArrayList<>();
    private final ObservableList<Playlist> displayedPlaylists = FXCollections.observableArrayList();
    private Button activeNavButton;
    private enum ActiveView { LIBRARY, PLAYLIST, NONE }
    private ActiveView currentView = ActiveView.NONE;

    /* Initialization */

    /**
     * Initializes the core UI shell.
     * Loads the sidebars, sets the default view to main library,
     * and configures the context menus for the sidebar playlist navigation.
     */
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

        // Custom CellFactory for Playlist Sidebar
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
            MenuItem rename = new MenuItem("Rename playlist");

            remove.setOnAction(e -> {
                Playlist item = cell.getItem();
                if (item != null) {
                    handleRemovePlaylist(item);
                }
            });

            rename.setOnAction(e -> {
                Playlist item = cell.getItem();
                if(item != null) {
                    TextInputDialog dialog = new TextInputDialog();
                    dialog.setTitle("Rename Playlist");
                    dialog.setHeaderText("Rename this playlist");
                    dialog.setContentText("Playlist name:");

                    dialog.showAndWait().ifPresent(name -> {
                        if (!name.isBlank()) {
                            item.setTitle(name);
                            String query = playlistSearchField.getText().trim();
                            searchSidebarPlaylists(query);
                            showPlaylist(item);
                        }
                    });
                }
            });

            menu.getItems().addAll(remove, rename);

            cell.contextMenuProperty().bind(
                    Bindings.when(cell.emptyProperty())
                            .then((ContextMenu) null)
                            .otherwise(menu)
            );

            return cell;
        });
    }

    /* Dependencies */

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

    /* View navigation and loading */

    /** Loads and displays the main "All Tracks" library view in the center pane. */
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

    /**
     * Loads and displays the detail view for a specific playlist in the center pane.
     *
     * @param playlist The {@link Playlist} object to render.
     */
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

    /** Loads the playback bar into the bottom container. */
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

    /** Loads the queue sidebar into the right container. */
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

    /* Global and sidebar */

    /**
     * Get text from the global top search bar and direct it to the
     * currently active center view controller (Library or Playlist).
     */
    @FXML
    private void handleSearch() {
        String query = searchField.getText().trim();
        switch (currentView) {
            case LIBRARY  -> { if (libraryController  != null) libraryController.applySearch(query);  }
            case PLAYLIST -> { if (playlistController != null) playlistController.applySearch(query); }
            case NONE     -> { }
        }
    }

    /** Spawns a dialog to create a new playlist and automatically navigates to it. */
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

    /** Triggers a local search within the sidebar playlist navigation. */
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

    /** Prompts for confirmation before permanently deleting a playlist. */
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

    /** Toggles the visibility state of the right-hand queue sidebar. */
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

    /** Displays a critical error dialog to the user during FXML loading failures. */
    private void showError(String message, Exception e) {
        e.printStackTrace();
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(message);
        alert.setContentText(e.getMessage());
        alert.showAndWait();
    }
}
