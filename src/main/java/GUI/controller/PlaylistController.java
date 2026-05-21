package GUI.controller;

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
import module3.storage.PlaylistStorage;
import module5.util.LibraryLogic;

import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

public class PlaylistController implements Initializable {

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

    // Sort
    private enum SortOrder { TITLE_ASC, TITLE_DESC, DATE_ASC, DATE_DESC }
    private SortOrder currentSort = SortOrder.TITLE_ASC;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Bind each column width to a percentage of the table width
        trackTable.widthProperty().addListener((obs, oldVal, newVal) -> {
            double width = newVal.doubleValue();
            colNumber.setPrefWidth(width * 0.06);
            colTitle.setPrefWidth(width * 0.36);
            colArtist.setPrefWidth(width * 0.28);
            colGenre.setPrefWidth(width * 0.20);
            colDuration.setPrefWidth(width * 0.10);
        });

        // Bind columns to AudioItem getters
        colNumber.setCellValueFactory(data ->
                new SimpleIntegerProperty(trackTable.getItems().indexOf(data.getValue()) + 1)
                        .asObject()
        );
        colTitle.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getTitle())
        );
        colArtist.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getAuthor())
        );
        colGenre.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getGenre())
        );
        colDuration.setCellValueFactory(data ->
                new SimpleStringProperty(formatTime(data.getValue().getDuration()))
        );

        // set sort button
        sortComboBox.getItems().addAll(
                "Title A → Z",
                "Title Z → A",
                "Newest first",
                "Oldest first"
        );
        sortComboBox.setValue("Title A → Z");  // default selection

        // click on the row
        trackTable.setRowFactory(tv -> {
            TableRow<AudioItem> row = new TableRow<>();

            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && event.getButton() == MouseButton.PRIMARY) {
                    AudioItem selected = trackTable.getSelectionModel()
                            .getSelectedItem();
                    if (selected != null && playbackBarController != null) {
                        playbackBarController.playPlaylist(displayList, displayList.indexOf(selected));
                    }
                }
            });

            ContextMenu menu = new ContextMenu();
            menu.getStyleClass().add("right-click-menu");
            MenuItem remove = new MenuItem("Remove from playlist "+playlist.getTitle());
            remove.setOnAction(e -> handleRemove(row.getItem()));

            menu.getItems().addAll(remove);

            row.contextMenuProperty().bind(
                    Bindings.when(row.emptyProperty())
                            .then((ContextMenu) null)
                            .otherwise(menu)
            );
            return row;
        });

        applyAll();
        trackTable.setItems(displayList);
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
                applyAll();
            }
        });
    }

    public void loadPlaylist(Playlist playlist){
        this.playlist = playlist;
        playlistTitle.setText(playlist.getTitle());

        masterList.clear();
        for(String trackID : playlist.getTracks()){
            masterList.add(audioStorage.getItem(trackID));
        }
        applyAll();
    }

    @FXML
    private void handlePlayAll() {
        if(masterList.isEmpty()){
            return;
        }
        playbackBarController.playPlaylist(displayList, 0);
    }

    private void applyAll() {
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

        // apply sort using compareTo()
        switch (currentSort){
            case TITLE_ASC -> Collections.sort(result);
            case TITLE_DESC -> result.sort((a,b) -> b.compareTo(a));
            case DATE_ASC -> result.sort(Comparator.comparing(AudioItem::getReleaseDate));
            case DATE_DESC -> result.sort(Comparator.comparing(AudioItem::getReleaseDate).reversed());
        }

        // rebuild table
        displayList.clear();
        displayList.addAll(result);

        updateTrackCount(result.size());

        //if (playingID != null) {
        //    playingCard = null;
        //    highlightPlayingCard(playingID);
        //}
    }

    /** Called by MainController when the search field changes. */
    public void applySearch(String query) {
        currentQuery = query;
        applyAll();
    }

    // Sort
    @FXML
    private void handleSort() {
        String selected = sortComboBox.getValue();
        if (selected == null) return;

        applySortOrder(selected);
    }

    public void applySortOrder(String order)   {
        currentSort = switch (order) {
            case "Title A → Z" -> SortOrder.TITLE_ASC;
            case "Title Z → A" -> SortOrder.TITLE_DESC;
            case "Newest first" -> SortOrder.DATE_DESC;
            case "Oldest first" -> SortOrder.DATE_ASC;
            default             -> SortOrder.TITLE_ASC;
        };
        applyAll();
    }

    // Filter
    @FXML
    private void handleFilter() {
        showFilterDialog();
    }

    public void showFilterDialog() {
        ChoiceDialog<String> categoryDialog = new ChoiceDialog<>(
                "Genre", "Genre", "Date"
        );
        categoryDialog.setTitle("Filter");
        categoryDialog.setHeaderText("Filter by:");
        categoryDialog.showAndWait().ifPresent(category -> {
            if(category.equals("Genre")) {
                showGenreFilter();
            }
            else {
                showDateFilter();
            }
        });
    }

    private void showGenreFilter() {

        // Build list of genres that actually exist in masterList
        List<String> genres = masterList.stream()
                .map(AudioItem::getGenre)
                .filter(g -> g != null && !g.isBlank())
                .distinct()
                .sorted()
                .collect(Collectors.toList());

        if (genres.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Filter");
            alert.setHeaderText("No genre data available");
            alert.setContentText(
                    "Import tracks with genre metadata to use this filter."
            );
            alert.showAndWait();
            return;
        }

        ChoiceDialog<String> genreDialog =
                new ChoiceDialog<>(genres.get(0), genres);
        genreDialog.setTitle("Filter by Genre");
        genreDialog.setHeaderText("Select genre:");

        genreDialog.showAndWait().ifPresent(genre -> {
            filterCategory = "genre";
            filterOperator = "=";
            filterValue    = genre;
            applyAll();

            showClearFilter("Genre: " + genre);
        });
    }

    private void showDateFilter() {

        // Step 1 — pick operator
        ChoiceDialog<String> operatorDialog = new ChoiceDialog<>(
                "=", "=", ">=", "<="
        );
        operatorDialog.setTitle("Filter by Date");
        operatorDialog.setHeaderText("Show tracks released:");

        // Rename choices to readable labels
        operatorDialog.getItems().setAll("Exactly in year", "From year onwards", "Up to year");
        operatorDialog.setSelectedItem("Exactly in year");

        operatorDialog.showAndWait().ifPresent(operatorLabel -> {

            // Map readable label back to operator symbol
            String operator = switch (operatorLabel) {
                case "Exactly in year"   -> "=";
                case "From year onwards" -> ">=";
                case "Up to year"        -> "<=";
                default                  -> "=";
            };

            // Step 2 — enter the year
            TextInputDialog yearDialog = new TextInputDialog("2020");
            yearDialog.setTitle("Filter by Date");
            yearDialog.setHeaderText("Enter year (" + operatorLabel + "):");
            yearDialog.setContentText("Year:");

            yearDialog.showAndWait().ifPresent(year -> {
                if (year.isBlank()) return;

                filterCategory = "date";
                filterOperator = operator;
                filterValue    = year.trim();
                System.out.println("Filter value: " + filterValue);
                applyAll();

                String label = switch (operator) {
                    case "="  -> "Year: " + year;
                    case ">=" -> "From: " + year;
                    case "<=" -> "Until: " + year;
                    default   -> "Date filter";
                };

                showClearFilter(label);
            });
        });
    }

    public void showClearFilter(String label){
        btnClearFilter.setText("✕ " + label);
        btnClearFilter.setVisible(true);
        btnClearFilter.setManaged(true);

        btnFilter.getStyleClass().add("active");
    }

    // clear filter
    @FXML
    public void handleClearFilter() {
        clearFilter();
        btnClearFilter.setVisible(false);
        btnClearFilter.setManaged(false);
        btnFilter.getStyleClass().remove("active");
    }

    public void clearFilter(){
        filterCategory = null;
        filterOperator = null;
        filterValue    = null;
        applyAll();
    }

    // Helpers
    private String formatTime(int totalSeconds) {
        int minutes = totalSeconds / 60;
        int seconds = totalSeconds % 60;
        return String.format("%d:%02d", minutes, seconds);
    }

    private void updateTrackCount(int displayed) {
        int total = masterList.size();
        if (displayed == total) {
            trackCountLabel.setText(total + (total == 1 ? " song" : " songs"));
        } else {
            trackCountLabel.setText(displayed + " of " + total + " songs");
        }
    }

}
