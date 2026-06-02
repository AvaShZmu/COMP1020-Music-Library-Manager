package GUI.controller.library;

import GUI.controller.util.dialog.FilterDialog;
import GUI.controller.util.dialog.TrackEditDialog;
import GUI.controller.main.MainController;
import GUI.controller.playback.PlaybackBarController;
import javafx.animation.FadeTransition;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.stage.Window;
import module1.audioModel.AudioItem;
import java.net.URL;
import java.util.*;
import module2.playlistModel.Playlist;
import module3.storage.AudioStorage;
import module3.storage.PlaylistStorage;
import module5.util.LibraryLogic;

/**
 * The primary UI controller managing the main library view.
 *<p>
 *     This class coordinates the rendering of the track grid, handles global search,
 *     filter, and sorting states, and hands media interactions down to the
 *     {@link PlaybackBarController}. It acts as a sub-controller managed by
 *     the facade {@link MainController}.
 *</p>
 */

public class LibraryController implements Initializable, CardBuildUtil.CardInteractionListener, FilterDialog.FilterListener {

    /* FXML bindings */

    /** The root container for the library view. */
    @FXML private VBox libraryView;

    /** The grid where track cards are rendered. */
    @FXML private FlowPane cardGrid;

    /** Displays the current number of tracks visible versus total tracks. */
    @FXML private Label trackCountLabel;

    /** Dropdown menu for selecting sorting criteria. */
    @FXML private ComboBox<String> sortComboBox;

    /** Button to open the metadata filter dialogue. */
    @FXML private Button btnFilter;

    /** Button to clear active filters and restore the full library view. */
    @FXML private Button btnClearFilter;

    /* State and dependencies */

    /** The master list of all tracks currently loaded from the database. */
    private final List<AudioItem> masterList = new ArrayList<>();

    private AudioItem selectedItem = null;
    private VBox selectedCard = null;

    private VBox playingCard  = null;
    private String playingID = null;

    private MainController mainController;
    private AudioStorage audioStorage;
    private PlaylistStorage playlistStorage;
    private PlaybackBarController playbackBarController;

    private String currentQuery = "";
    private String filterCategory = null;  // "genre" or "releaseDate"
    private String filterOperator = null;  // "=" for genre, ">=" "<=" "=" for date
    private String filterValue    = null;  // the actual value

    /* Controller initialization, dependency injection */

    /**
     * Automatically invoked by JavaFX after the FXML files have been loaded.
     * Initializes default UI states and populates sorting dropdown.
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Deselect cards when clicking empty background.
        libraryView.setOnMouseClicked(event -> {
            if (selectedCard != null) {
                selectedCard.getStyleClass().remove("selected");
                selectedCard = null;
                selectedItem = null;
            }
        });

        sortComboBox.getItems().addAll(LibraryLogic.SORT_LABELS);
        sortComboBox.setValue(LibraryLogic.SORT_LABELS.get(0));  // default selection
    }

    /** Injects the root parent controller. */
    public void setMainController(MainController mc) {
        this.mainController = mc;
    }

    /**
     * Injects the core audio database and trigger initial library render.
     *
     * @param as The initialized {@link AudioStorage} database.
     */
    public void setAudioStorage(AudioStorage as) {
        this.audioStorage = as;

        // Populate the library as soon as MainController passes the storage down
        if (this.audioStorage != null) {
            masterList.clear();
            masterList.addAll(this.audioStorage.getAllItems());
            refreshLibrary();
        }
    }

    /** Injects the playlist repository for context-menu operations. */
    public void setPlaylistStorage(PlaylistStorage ps){
        this.playlistStorage = ps;
    }

    /** Injects the playback controller for routing media commands. */
    public void setPlaybackBarController(PlaybackBarController pc) { this.playbackBarController = pc; }

    /* UI Action handlers */

    /**
     * Synchronizes the play/pause icon on the active track card to match the global playback state.
     *
     * @param textState The icon text to apply (e.g., "▶" or "⏸").
     */
    public void syncActiveCardButton(String textState) {
        if (playingCard != null) {
            Button btn = (Button) playingCard.getProperties().get("playButton");
            if (btn != null) {
                btn.setText(textState);
            }
        }
    }

    /** Triggers the OS file chooser to import new tracks. */
    @FXML
    private void handleImport() {
        Window window = cardGrid.getScene().getWindow();
        ImportUtil.handleImport(window, masterList, audioStorage, this::refreshLibrary);
    }

    /** Opens the filter dialogue menu. */
    @FXML
    private void handleFilter() {
        FilterDialog.showFilterDialog(masterList, this);
    }

    /** Clears all active filters and forces a full grid re-render. */
    @FXML
    private void handleClearFilter() {
        filterCategory = null; filterOperator = null; filterValue = null;
        btnClearFilter.setVisible(false);
        btnClearFilter.setManaged(false);
        btnFilter.getStyleClass().remove("active");
        refreshLibrary();
    }

    /** Re-sorts the grid based on the newly selected ComboBox value. */
    @FXML
    private void handleSort() {
        refreshLibrary();
    }

    /**
     * Updates the active search query and triggers a grid re-render.
     *
     * @param query The partial text string to match against track titles or artists.
     */
    public void applySearch(String query) {
        currentQuery = query;
        refreshLibrary();
    }

    /* Core rendering logic */

    /**
     * The main method for rendering the library.
     * <p>
     *     Processes the {@code masterList} through the search query, filter, and sorting criteria.
     *     The resulting list is then converted into JavaFX nodes via {@link CardBuildUtil} and
     *     appended to the grid.
     * </p>
     */
    private void refreshLibrary() {
        List<AudioItem> result = new ArrayList<>(masterList);

        // Apply search
        if(!currentQuery.isBlank()){
            result = LibraryLogic.search(masterList, currentQuery);
        }

        // Apply filter
        if(filterCategory!=null){
            result = LibraryLogic.filter(result, filterCategory, filterOperator, filterValue);
        }

        // Apply sort
        result = LibraryLogic.sort(result, sortComboBox.getValue());

        // Rebuild and update grid
        cardGrid.getChildren().clear();
        for (AudioItem item : result) {
            cardGrid.getChildren().add(CardBuildUtil.buildCard(item, playlistStorage, playbackBarController, playingCard, this));
        }
        updateTrackCount(result.size());

        if (playingID != null) {
            playingCard = null;
            highlightPlayingCard(playingID);
        }
    }

    /**
     * Visually highlights a specific track card to denote it the current playing state,
     * remove the highlight from the previous active card.
     *
     * @param trackID The UUID of the
     */
    public void highlightPlayingCard(String trackID) {
        // Clean up prev track
        if (playingCard != null) {
            playingCard.getStyleClass().remove("playing");

            Button oldBtn = (Button) playingCard.getProperties().get("playButton");
            FadeTransition oldFadeIn = (FadeTransition) playingCard.getProperties().get("fadeIn");
            FadeTransition oldFadeOut = (FadeTransition) playingCard.getProperties().get("fadeOut");

            if (oldBtn != null) {
                oldBtn.setText("▶");

                if (!playingCard.isHover()) {
                    if (oldFadeIn != null) oldFadeIn.stop();
                    if (oldFadeOut != null) oldFadeOut.playFromStart();
                }
            }
        }

        playingID = trackID;
        playingCard = null;
        if (trackID == null) {
            return;
        }

        // Set up new card
        cardGrid.getChildren().stream()
                .filter(node -> trackID.equals(node.getUserData()))
                .findFirst()
                .ifPresent(node -> {
                    node.getStyleClass().add("playing");
                    playingCard = (VBox) node;

                    Button newBtn = (Button) playingCard.getProperties().get("playButton");
                    FadeTransition newFadeIn = (FadeTransition) playingCard.getProperties().get("fadeIn");
                    FadeTransition newFadeOut = (FadeTransition) playingCard.getProperties().get("fadeOut");
                    if (newBtn != null) {
                        newBtn.setText("⏸");

                        if (newFadeOut != null) newFadeOut.stop();
                        newBtn.setVisible(true);
                        if (newFadeIn != null) newFadeIn.playFromStart();
                    }
                });
    }

    /** Updates the label tracking the number of displayed results. */
    private void updateTrackCount(int displayed) {
        int total = masterList.size();
        if (displayed == total) {
            trackCountLabel.setText(total + (total == 1 ? " song" : " songs"));
        } else {
            trackCountLabel.setText(displayed + " of " + total + " songs");
        }
    }

    /* FilterListener interface implementations */

    @Override
    public void onFilterApplied(String category, String operator, String value, String clearLabelText) {
        this.filterCategory = category;
        this.filterOperator = operator;
        this.filterValue = value;

        btnClearFilter.setText("✕ " + clearLabelText);
        btnClearFilter.setVisible(true);
        btnClearFilter.setManaged(true);
        btnFilter.getStyleClass().add("active");

        refreshLibrary();
    }

    /* CardInteractionListener interface implementations */

    @Override
    public void onPlayClicked(AudioItem item, VBox card) {
        if (playingCard == card) {
            if (playbackBarController != null) playbackBarController.handlePlayPause();
        } else {
            if (playbackBarController != null) playbackBarController.playTrack(item);
        }
    }

    @Override
    public void onCardSelected(AudioItem item, VBox card, boolean doubleClicked) {
        if (selectedCard != null) {
            selectedCard.getStyleClass().remove("selected");
        }
        card.getStyleClass().add("selected");
        selectedCard = card;
        selectedItem = item;

        if (doubleClicked && playbackBarController != null) {
            playbackBarController.playTrack(item);
        }
    }

    @Override
    public void onRemoveRequested(AudioItem item) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Remove Track");
        confirm.setHeaderText("Remove \"" + item.getTitle() + "\"?");
        confirm.setContentText(
                "This removes the track from your library. " +
                        "The original file will not be deleted."
        );

        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                masterList.remove(item);
                audioStorage.removeItem(item);
                for(Playlist pl : playlistStorage.getAllPlaylists()){
                    pl.removeTrack(item);
                }
                refreshLibrary();
            }
        });
    }

    @Override
    public void onAddToQueue(AudioItem item) {
        if (playbackBarController != null) {
            playbackBarController.addToQueue(item);
        }
    }

    @Override
    public void onEditRequested(AudioItem item) {
        TrackEditDialog dialog = new TrackEditDialog(item, TrackEditDialog.Mode.EDIT);
        dialog.showAndWait().ifPresent(updatedItem -> {
            int index = masterList.indexOf(item);
            if (index != -1) {
                masterList.set(index, item);
            }

            audioStorage.updateItem(updatedItem);
            refreshLibrary();
        });
    }
}
