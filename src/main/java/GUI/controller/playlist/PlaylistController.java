package GUI.controller.playlist;

import GUI.controller.playback.PlaybackBarController;
import GUI.controller.util.dialog.FilterDialog;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import module1.audioModel.AudioItem;
import module2.playlistModel.Playlist;
import module3.storage.AudioStorage;
import module5.util.LibraryLogic;
import java.net.URL;
import java.util.*;

/**
 * This is the UI controller that manages the detailed view of a single playlist.
 * <p>
 *     This class handles the rendering of the track table, inline playlist metadata
 *     editing (e.g., renaming description), manage local search, filter and sort specific to
 *     the active playlist.
 * </p>
 */

public class PlaylistController implements Initializable, TableBuildUtil.TableInteractionListener, FilterDialog.FilterListener {

    /* FXML Bindings */

    /** The primary data grid that displays the list of tracks in the playlist. */
    @FXML private TableView<AudioItem> trackTable;

    /** Table column displaying the index of the track. */
    @FXML private TableColumn<AudioItem, Integer> colNumber;

    /** Table column displaying the title of the track. */
    @FXML private TableColumn<AudioItem, String>  colTitle;

    /** Table column displaying the author or artist of the track. */
    @FXML private TableColumn<AudioItem, String>  colArtist;

    /** Table column displaying the genre of the track. */
    @FXML private TableColumn<AudioItem, String>  colGenre;

    /** Table column displaying the formatted duration of the track. */
    @FXML private TableColumn<AudioItem, String>  colDuration;

    /** The large header label displaying the playlist's name. */
    @FXML private Label playlistTitle;

    /** Label displaying the current number of tracks visible in the table. */
    @FXML private Label trackCountLabel;

    /** Button used to clear active filters and restore the full tracklist view. */
    @FXML private Button btnClearFilter;

    /** Button used to open the metadata filter dialogue popup. */
    @FXML private Button btnFilter;

    /** Dropdown menu for selecting sorting criteria (e.g., "Shortest First", "Title A-Z"). */
    @FXML private ComboBox<String> sortComboBox;

    /** Label displaying the user-created description of the playlist. Double-clickable for editing. */
    @FXML private Label playlistDesc;

    /** Label displaying the system-generated creation date of the playlist. */
    @FXML private Label playlistDateLabel;

    /** A hidden text field that replaces {@code playlistDesc} during inline editing. */
    @FXML private TextField descField;

    /* State Variables */

    /** The actual playlist object currently being viewed. */
    private Playlist playlist;

    /** The master backend list of all tracks belonging to this playlist. */
    private List<AudioItem> masterList = new ArrayList<>();

    /** The frontend list bound directly to the {@code trackTable} for UI updates. */
    private ObservableList<AudioItem> displayList = FXCollections.observableArrayList();

    private AudioStorage audioStorage;
    private PlaybackBarController playbackBarController;

    private String currentQuery = "";
    private String filterCategory = null;
    private String filterOperator = null;
    private String filterValue    = null;

    /* Initialization */

    /**
     * Invoked by JavaFX upon FXML load.
     * Configures the initial table structure, sort bindings, and inline editing listeners.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Build table using util class TableBuildUtil
        TableBuildUtil.setupTable(trackTable, colNumber, colTitle, colArtist, colGenre, colDuration, playlist, this);
        trackTable.setItems(displayList);

        // Set up the sort combo box
        sortComboBox.getItems().addAll("Oldest Added", "Title A → Z", "Title Z → A", "Longest First", "Shortest First");
        sortComboBox.setValue("Oldest Added");

        // Sync TableView's built-in column arrows with the combo box
        trackTable.setSortPolicy(tv -> {
            if (tv.getSortOrder().isEmpty()) {
                sortComboBox.setValue("Oldest Added");
                refreshPlaylist();
                return true;
            }
            TableColumn<AudioItem, ?> column = tv.getSortOrder().get(0);
            boolean ascending = column.getSortType() == TableColumn.SortType.ASCENDING;

            if (column == colTitle) sortComboBox.setValue(ascending ? "Title A → Z" : "Title Z → A");
            if (column == colDuration) sortComboBox.setValue(ascending ? "Shortest First" : "Longest First");

            refreshPlaylist();
            return true;
        });

        // Set up editing for description
        playlistDesc.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                descField.setText(playlistDesc.getText());
                playlistDesc.setVisible(false);
                playlistDesc.setManaged(false);
                descField.setVisible(true);
                descField.setManaged(true);
                descField.requestFocus();
            }
        });

        descField.setOnAction(e -> saveEdit());

        descField.focusedProperty().addListener((obs, oldV, focused) -> {
            if(!focused) {
                saveEdit();
            }
        });
    }

    /* Set up dependencies */

    /** Injects the core audio storage for resolving track IDs into full items. */
    public void setAudioStorage(AudioStorage audioStorage) {
        this.audioStorage = audioStorage;
    }

    /** Injects the playback controller to handle media commands. */
    public void setPlaybackBarController(PlaybackBarController pbc){
        this.playbackBarController = pbc;
    }

    /**
     * Injects a specific {@link Playlist} data model into the view and populates the table.
     *
     * @param playlist The playlist to display.
     */
    public void loadPlaylist(Playlist playlist){
        this.playlist = playlist;
        playlistTitle.setText(playlist.getTitle());
        playlistDesc.setText(playlist.getDescription());
        playlistDateLabel.setText("Created: " + playlist.getDateCreated());

        masterList.clear();
        for(String trackID : playlist.getTracks()){
            masterList.add(audioStorage.getItem(trackID));
        }
        refreshPlaylist();
    }

    /* UI action handlers */

    /** Saves the modified playlist description from the inline text field back to the model. */
    private void saveEdit() {
        String newText = descField.getText();

        if (!newText.isBlank()) {
            playlistDesc.setText(newText);
            playlist.setDescription(newText);
        }

        descField.setVisible(false);
        descField.setManaged(false);

        playlistDesc.setVisible(true);
        playlistDesc.setManaged(true);
    }

    /** Pushes the entire currently displayed list to the playback queue. */
    @FXML
    private void handlePlayAll() {
        if(masterList.isEmpty()){
            return;
        }
        playbackBarController.playPlaylist(displayList, 0);
    }

    /**
     * Sorts the table data based on the newly-selected ComboBox value.
     * Manually syncs the TableView column arrows to match.
     */
    @FXML
    private void handleSort() {
        String selected = sortComboBox.getValue();
        if (selected == null) return;

        // Sync the TableView arrows to match the ComboBox change
        trackTable.getSortOrder().clear();
        if (selected.contains("Title")) {
            colTitle.setSortType(selected.contains("A → Z") ? TableColumn.SortType.ASCENDING : TableColumn.SortType.DESCENDING);
            trackTable.getSortOrder().add(colTitle);
        } else if (selected.contains("First")) {
            colDuration.setSortType(selected.contains("Shortest") ? TableColumn.SortType.ASCENDING : TableColumn.SortType.DESCENDING);
            trackTable.getSortOrder().add(colDuration);
        }

        refreshPlaylist();
    }

    /** Opens the filter dialogue menu. */
    @FXML
    private void handleFilter() {
        FilterDialog.showFilterDialog(masterList, this);
    }

    /** Clears all active filters and forces a full table re-render. */
    @FXML
    private void handleClearFilter() {
        filterCategory = null; filterValue = null; filterOperator = null;
        btnClearFilter.setVisible(false);
        btnClearFilter.setManaged(false);
        btnFilter.getStyleClass().remove("active");
        refreshPlaylist();
    }

    /** Updates the active search query and triggers a table re-render. */
    public void applySearch(String query) {
        currentQuery = query;
        refreshPlaylist();
    }

    /* Rendering logic */

    /**
     * Processes the master playlist tracklist through the active search query,
     * the active filter condition, and the active sorting criteria, before updating
     * the bound {@link ObservableList} to automatically refresh the JavaFX TableView.
     */
    private void refreshPlaylist() {
        List<AudioItem> result = new ArrayList<>(masterList);

        // Apply search
        if(!currentQuery.isBlank()){
            result = LibraryLogic.search(masterList, currentQuery);
        }

        // Apply filter
        if(filterCategory!=null){
            result = LibraryLogic.filter(masterList, filterCategory, filterOperator, filterValue);
        }

        // Apply sort
        String sortVal = sortComboBox.getValue();
        result = LibraryLogic.sort(result, sortVal);

        displayList.setAll(result);
        updateTrackCount(result.size());
    }

    /** Updates the label tracking the number of displayed results in the table. */
    private void updateTrackCount(int displayed) {
        int total = masterList.size();
        if (displayed == total) {
            trackCountLabel.setText(total + (total == 1 ? " song" : " songs"));
        } else {
            trackCountLabel.setText(displayed + " of " + total + " songs");
        }
    }

    /* TableInteractionListener interface implementations */

    @Override
    public void onTrackDoubleClicked(AudioItem item, int index) {
        if (playbackBarController != null)
            playbackBarController.playPlaylist(displayList, index);
    }

    @Override
    public void onRemoveRequested(AudioItem item) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "The track stays in your library.");
        confirm.setTitle("Remove Track");
        confirm.setHeaderText("Remove \"" + item.getTitle() + "\" from playlist?");

        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                playlist.removeTrack(item);
                masterList.remove(item);
                refreshPlaylist();
            }
        });
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

        refreshPlaylist();
    }
}
