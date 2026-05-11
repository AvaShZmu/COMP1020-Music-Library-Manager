package com.yourname.controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import module1.audioModel.AudioItem;

import java.io.File;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

public class LibraryController implements Initializable{

    @FXML private FlowPane cardGrid;
    @FXML private Label trackCountLabel;
    @FXML private Button btnRemove;

    private final List<AudioItem> masterList = new ArrayList<>();

    // Currently selected card and its data
    private AudioItem selectedItem = null;
    private VBox selectedCard = null;

    // Currently playing card — for green border highlight
    private VBox playingCard  = null;

    // Reference back to parent so double-click can update the bottom bar
    private MainController mainController;

    // Search
    private String currentQuery = "";

    // Filter — matches passesFilter(category, operator, value) signature
    private String filterCategory = null;  // "genre" or "releaseDate"
    private String filterOperator = null;  // "=" for genre, ">=" "<=" "=" for date
    private String filterValue    = null;  // the actual value

    // Sort
    private enum SortOrder { TITLE_ASC, TITLE_DESC, DATE_ASC, DATE_DESC }
    private SortOrder currentSort = SortOrder.TITLE_ASC;

    // Backend — uncomment once wired in Phase 7
    // private storage.AudioStorage audioStorage;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        btnRemove.setDisable(true);
        // Load from backend once wired:
        // masterList.addAll(audioStorage.getAllItems());
        // rebuildGrid();
    }

    private VBox buildCard(AudioItem item){
        // ── Art square ───────────────────────────────────────────────────
        StackPane artPane = new StackPane();
        artPane.getStyleClass().add("card-art");

        // Placeholder icon — replace with real art in Phase 7
        Label icon = new Label("♪");
        icon.getStyleClass().add("card-art-icon");
        artPane.getChildren().add(icon);

        // ── Title ────────────────────────────────────────────────────────
        Label title = new Label(item.getTitle());
        title.getStyleClass().add("card-title");
        title.setMaxWidth(140);

        // ── Artist ───────────────────────────────────────────────────────
        Label artist = new Label(item.getAuthor());
        artist.getStyleClass().add("card-artist");
        artist.setMaxWidth(140);

        VBox card = new VBox();
        card.getStyleClass().add("track-card");
        card.getChildren().addAll(artPane, title, artist);

        card.setUserData(item.getTrackID());

        card.setOnMouseClicked(event ->{
            if(selectedCard != null){
                selectedCard.getStyleClass().remove("selected");
            }

            card.getStyleClass().add("selected");
            selectedCard = card;
            selectedItem = item;
            btnRemove.setDisable(false);

            if(event.getClickCount() == 2 && mainController != null){
                if(playingCard != null){
                    playingCard.getStyleClass().remove("playing");
                }
                card.getStyleClass().add("playing");
                playingCard = card;

                mainController.playTrack(item.getTrackID(), item.getTitle(), item.getAuthor());
            }
        });
        return card;
    }
    private void rebuildGrid(){
        cardGrid.getChildren().clear();
        for(AudioItem item: masterList){
            cardGrid.getChildren().add(buildCard(item));
        }
        updateTrackCount();
    }
    @FXML
    private void handleImport(){
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Import Audio Files");
        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Audio Files", "*.mp3", "*.wav", "*.flac", "*.aac", "*.ogg")
        );

        Window window = cardGrid.getScene().getWindow();
        List<File> files = chooser.showOpenMultipleDialog(window);

        if(files != null){
            for(File file : files){
                // Replace with real metadata extraction in Phase 7:
                // AudioItem item = metadataExtractor.extract(file.getPath());
                // audioStorage.addItem(item);
                AudioItem placeholder = new AudioItem(
                        UUID.randomUUID().toString(),
                        "Unknown Artist",
                        "11 May",
                        100,
                        "indie",
                        file.getPath()
                );
                masterList.add(placeholder);
            }
            rebuildGrid();
        }
    }

    @FXML
    private void handleRemove(){
        if (selectedItem == null) return;

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Remove Track");
        confirm.setHeaderText("Remove \"" + selectedItem.getTitle() + "\"?");
        confirm.setContentText(
                "This removes the track from your library. " +
                        "The original file will not be deleted."
        );

        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                masterList.remove(selectedItem);
                // audioStorage.removeItem(selectedItem); ← Phase 7
                selectedItem = null;
                selectedCard = null;
                // playingCard = null;
                btnRemove.setDisable(true);
                rebuildGrid();
            }
        });
    }

    public void applySearch(String query) {
        currentQuery = query;
        applyAll();
    }
    public void cycleSortOrder()   {
        currentSort = switch (currentSort){
            case TITLE_ASC -> SortOrder.TITLE_DESC;
            case TITLE_DESC -> SortOrder.DATE_ASC;
            case DATE_ASC -> SortOrder.DATE_DESC;
            case DATE_DESC -> SortOrder.TITLE_ASC;
        };
        String label = switch (currentSort){
            case TITLE_ASC -> "Sort: A -> Z";
            case TITLE_DESC -> "Sort Z -> A";
            case DATE_ASC -> "Sort: Newest";
            case DATE_DESC -> "Sort: Oldest";
        };

        if(mainController!=null){
            mainController.updateSortLabel(label);
        }
        applyAll();
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

            if (mainController != null) {
                mainController.showClearFilter("Genre: " + genre);
            }
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

                filterCategory = "releaseDate";
                filterOperator = operator;
                filterValue    = year.trim();
                applyAll();

                String label = switch (operator) {
                    case "="  -> "Year: " + year;
                    case ">=" -> "From: " + year;
                    case "<=" -> "Until: " + year;
                    default   -> "Date filter";
                };

                if (mainController != null) {
                    mainController.showClearFilter(label);
                }
            });
        });
    }

    public void clearFilter(){
        String filterCategory = null;
        String filterOperator = null;
        String filterValue    = null;
        applyAll();
    }

    private void applyAll() {
        // Step 1 — start from full master list
        List<AudioItem> result = new ArrayList<>(masterList);

        // Step 2 — apply search using matchesQuery()
        if(!currentQuery.isBlank()){
            result = result.stream()
                    .filter(item -> item.matchesQuery(currentQuery) )
                    .collect(Collectors.toList());
        }

        // Step 3 — apply filter using passesFilter()
        if(filterCategory!=null){
            result = result.stream()
                    .filter(item -> item.passesFilter(
                            filterCategory,
                            filterOperator,
                            filterValue))
                    .collect(Collectors.toList());
        }

        // Step 4 — apply sort using compareTo()
        switch (currentSort){
            case TITLE_ASC -> Collections.sort(result);
            case TITLE_DESC -> result.sort((a,b) -> b.compareTo(a));
            case DATE_ASC -> result.sort(Comparator.comparing(AudioItem::getReleaseDate));
            case DATE_DESC -> result.sort(Comparator.comparing(AudioItem::getReleaseDate).reversed());
        }
        rebuildGrid();
    }

    public void setMainController(MainController mc) {
        this.mainController = mc;
    }

    public void highlightPlayingCard(String trackID) {
        if (playingCard != null) {
            playingCard.getStyleClass().remove("playing");
        }
        cardGrid.getChildren().stream()
                .filter(node -> trackID.equals(node.getUserData()))
                .findFirst()
                .ifPresent(node -> {
                    node.getStyleClass().add("playing");
                    playingCard = (VBox) node;
                });
    }

    private void updateTrackCount(int displayed) {
        int total = masterList.size();
        if (displayed == total) {
            trackCountLabel.setText(total + (total == 1 ? " song" : " songs"));
        } else {
            trackCountLabel.setText(displayed + " of " + total + " songs");
        }
    }

    private void updateTrackCount() {
        updateTrackCount(masterList.size());
    }
    // test this updateTrackCount
}
