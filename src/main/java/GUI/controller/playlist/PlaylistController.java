package GUI.controller.playlist;

import GUI.controller.playback.PlaybackBarController;
import GUI.controller.util.dialog.FilterDialog;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import module1.audioModel.AudioItem;
import module2.playlistModel.Playlist;
import module3.storage.AudioStorage;
import module5.util.LibraryLogic;

import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

public class PlaylistController implements Initializable, TableBuildUtil.TableInteractionListener, FilterDialog.FilterListener {

    // FXML fields
    @FXML private TableView<AudioItem> trackTable;
    @FXML private TableColumn<AudioItem, Integer> colNumber;
    @FXML private TableColumn<AudioItem, String>    colTitle;
    @FXML private TableColumn<AudioItem, String>    colArtist;
    @FXML private TableColumn<AudioItem, String>    colGenre;
    @FXML private TableColumn<AudioItem, String>    colDuration;
    @FXML private Label playlistTitle;
    @FXML private Label trackCountLabel;
    @FXML private Button btnClearFilter;
    @FXML private Button btnFilter;
    @FXML private ComboBox<String> sortComboBox;
    @FXML private Label playlistDesc;
    @FXML private Label playlistDateLabel;
    @FXML private TextField descField;

    private Playlist playlist;
    private List<AudioItem> masterList = new ArrayList<>();
    private ObservableList<AudioItem> displayList
            = FXCollections.observableArrayList();

    private AudioStorage audioStorage;
    private PlaybackBarController playbackBarController;

    // Search
    private String currentQuery = "";

    // Filter — matches passesFilter(category, operator, value) signature
    private String filterCategory = null;  // "genre" or "releaseDate"
    private String filterOperator = null;  // "=" for genre, ">=" "<=" "=" for date
    private String filterValue    = null;  // the actual value

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Build table using util class TableBuildUtil
        TableBuildUtil.setupTable(trackTable, colNumber, colTitle, colArtist, colGenre, colDuration, playlist, this);
        trackTable.setItems(displayList);

        // Setup the sort combobox
        sortComboBox.getItems().addAll("Custom Order", "Title A → Z", "Title Z → A", "Longest First", "Shortest First");
        sortComboBox.setValue("Custom Order");

        // Sync TableView's built-in column arrows with the combobox
        trackTable.setSortPolicy(tv -> {
            if (tv.getSortOrder().isEmpty()) {
                sortComboBox.setValue("Custom Order");
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

        // Setup editing for description
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

    private void saveEdit() {

        String newText = descField.getText();

        if(!newText.isBlank()) {
            playlistDesc.setText(newText);
            playlist.setDescription(newText);
        }

        descField.setVisible(false);
        descField.setManaged(false);

        playlistDesc.setVisible(true);
        playlistDesc.setManaged(true);
    }

    public void setAudioStorage(AudioStorage audioStorage) {
        this.audioStorage = audioStorage;
    }

    public void setPlaybackBarController(PlaybackBarController pbc){
        this.playbackBarController = pbc;
    }

    private void handleRemove(AudioItem item){
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Remove Track");
        confirm.setHeaderText("Remove \"" + item.getTitle() + "\" from playlist?");
        confirm.setContentText("The track stays in your library.");

        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                playlist.removeTrack(item);
                masterList.remove(item);
                refreshPlaylist();
            }
        });
    }

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

    @FXML
    private void handlePlayAll() {
        if(masterList.isEmpty()){
            return;
        }
        playbackBarController.playPlaylist(displayList, 0);
    }

    private void refreshPlaylist() {
        // start from full master list
        List<AudioItem> result = new ArrayList<>(masterList);

        // apply search using matchesQuery()
        if(!currentQuery.isBlank()){
            result = LibraryLogic.search(masterList, currentQuery);
        }

        // apply filter using passesFilter()
        if(filterCategory!=null){
            result = LibraryLogic.filter(masterList, filterCategory, filterOperator, filterValue);
        }

        String sortVal = sortComboBox.getValue();
        result = LibraryLogic.sort(result, sortVal);

        displayList.setAll(result);

        int total = masterList.size();
        trackCountLabel.setText(result.size() == total ? total + (total == 1 ? " song" : " songs") : result.size() + " of " + total + " songs");
    }

    /** Called by MainController when the search field changes. */
    public void applySearch(String query) {
        currentQuery = query;
        refreshPlaylist();
    }

    // Sort
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

    // Filter
    @FXML
    private void handleFilter() {
        FilterDialog.showFilterDialog(masterList, this);
    }

    // clear filter
    @FXML
    private void handleClearFilter() {
        filterCategory = null; filterValue = null; filterOperator = null;
        btnClearFilter.setVisible(false);
        btnClearFilter.setManaged(false);
        btnFilter.getStyleClass().remove("active");
        refreshPlaylist();
    }

    private void updateTrackCount(int displayed) {
        int total = masterList.size();
        if (displayed == total) {
            trackCountLabel.setText(total + (total == 1 ? " song" : " songs"));
        } else {
            trackCountLabel.setText(displayed + " of " + total + " songs");
        }
    }

    // Listeners
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
