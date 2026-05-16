package GUI.controller;

import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import javafx.util.Duration;
import module1.audioModel.AudioItem;
import javafx.scene.image.Image;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.net.URL;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import module3.storage.AudioStorage;
import module5.util.MetadataExtractor;
import module5.util.LibraryLogic;

public class LibraryController implements Initializable{

    @FXML private FlowPane cardGrid;
    @FXML private Label trackCountLabel;
    @FXML private ComboBox<String> sortComboBox;
    @FXML private Button btnFilter;
    @FXML private Button btnClearFilter;

    private final List<AudioItem> masterList = new ArrayList<>();

    // Currently selected card and its data
    private AudioItem selectedItem = null;
    private VBox selectedCard = null;

    // Currently playing card — for green border highlight
    private VBox playingCard  = null;
    private String playingID = null;

    // Reference back to parent so double-click can update the bottom bar
    private MainController mainController;
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

    // Backend — uncomment once wired in Phase 7
    // private storage.AudioStorage audioStorage;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        //btnRemove.setDisable(true);
        // Load from backend once wired:
        // masterList.addAll(audioStorage.getAllItems());
        // rebuildGrid();
        cardGrid.setOnMouseClicked(event -> {
            if (selectedCard != null) {

                selectedCard.getStyleClass().remove("selected");

                selectedCard = null;
                selectedItem = null;

            }
        });

        sortComboBox.getItems().addAll(
                "Title A → Z",
                "Title Z → A",
                "Newest first",
                "Oldest first"
        );
        sortComboBox.setValue("Title A → Z");  // default selection
    }

    // Set up the controllers
    public void setMainController(MainController mc) {
        this.mainController = mc;
    }

    public void setAudioStorage(AudioStorage as) {
        this.audioStorage = as;
    }

    public void setPlaybackBarController(PlaybackBarController pc) {this.playbackBarController = pc;}

    private VBox buildCard(AudioItem item){
        // ── Art square ───────────────────────────────────────────────────
        StackPane artPane = new StackPane();
        artPane.getStyleClass().add("card-art");

        // Placeholder icon (fallback)
        Label icon = new Label("♪");
        icon.getStyleClass().add("card-art-icon");

        // Actual image:
        ImageView coverView = new ImageView();
        coverView.setFitHeight(155);
        coverView.setFitWidth(155);
        coverView.setPreserveRatio(true);

        // rounded corners
        javafx.scene.shape.Rectangle clip = new javafx.scene.shape.Rectangle(155, 155);
        clip.setArcWidth(10);  // The amount of rounding (increase for more circular)
        clip.setArcHeight(10); // Keep this matching the ArcWidth
        coverView.setClip(clip);

        // Hover play button
        Button playButton = new Button("▶");
        playButton.getStyleClass().add("hover-play-button");

        playButton.setOpacity(0.0);
        playButton.setVisible(false);
        playButton.setFocusTraversable(false);

        // Align to bottom right
        StackPane.setAlignment(playButton, Pos.BOTTOM_RIGHT);
        StackPane.setMargin(playButton, new Insets(0, 15, 15, 0));

        // Animation
        FadeTransition fadeIn = new FadeTransition(Duration.millis(150), playButton);
        fadeIn.setToValue(0.85);

        FadeTransition fadeOut = new FadeTransition(Duration.millis(150), playButton);
        fadeOut.setToValue(0.0);
        fadeOut.setOnFinished(event -> playButton.setVisible(false));

        artPane.getChildren().addAll(icon,  coverView, playButton);

        // ── asynchronous album loading
        CompletableFuture.supplyAsync(() -> {
            // Fetch raw bytes in the background
            return MetadataExtractor.getImage(item.getFileLocation());

        }).thenAccept(imageBytes -> {
            // Update UI on the main thread
            Platform.runLater(() -> {
                if (imageBytes != null && imageBytes.length > 0) {

                    // Construct the image
                    Image image = new Image(new ByteArrayInputStream(imageBytes));

                    // If successful, apply to UI
                    if (!image.isError() && image.getWidth() > 0) {
                        coverView.setImage(image);
                        icon.setVisible(false); // Hide placeholder
                    }
                }
            });
        });

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

        // Stash button inside card's properties
        card.getProperties().put("playButton", playButton);
        card.getProperties().put("fadeIn", fadeIn);
        card.getProperties().put("fadeOut", fadeOut);

        // Hover logic
        card.hoverProperty().addListener((obs, wasHovered, isHovered) -> {
            if (playingCard == card) {
                // It's the active track. Stop animations and lock it visible.
                fadeIn.stop();
                fadeOut.stop();
                playButton.setVisible(true);
                playButton.setOpacity(0.85);
            } else {
                if (isHovered) {
                    fadeOut.stop(); // Cancel any outgoing fade
                    playButton.setVisible(true); // Must be true so we can see it fade in
                    fadeIn.playFromStart();
                } else {
                    fadeIn.stop(); // Cancel any incoming fade
                    fadeOut.playFromStart();
                }
            }
        });

        // Play button click logic
        playButton.setOnMouseClicked(e -> {
            e.consume();

            if (playingCard == card) {
                if (playbackBarController != null) {
                    playbackBarController.handlePlayPause();
                }
            }
            else {
                // new track
                if (playbackBarController != null) {
                    playbackBarController.playTrack(item);
                }
            }
        });


        ContextMenu menu = new ContextMenu();
        menu.getStyleClass().add("right-click-menu");
        MenuItem remove = new MenuItem("Remove from library");
        MenuItem addToQueue = new MenuItem("Add to queue");
        MenuItem addToPlaylist = new MenuItem("Add to Playlist");

        remove.setOnAction(e -> handleRemove(item));
        addToQueue.setOnAction(e -> addToQueue(item));

        menu.getItems().addAll(remove,addToQueue,addToPlaylist);

        card.setOnMouseClicked(event ->{
            if(event.getButton() == MouseButton.PRIMARY) {
                event.consume();

                if (selectedCard != null) {
                    selectedCard.getStyleClass().remove("selected");
                }

                card.getStyleClass().add("selected");
                selectedCard = card;
                selectedItem = item;
                //btnRemove.setDisable(false);

                if (event.getClickCount() == 2 && playbackBarController != null) {
                    playbackBarController.playTrack(item);
                }
            }
            if(event.getButton() == MouseButton.SECONDARY) {
                menu.show(card, event.getScreenX(), event.getScreenY());
            }
        });

        return card;
    }

    public void syncActiveCardButton(String textState) {
        if (playingCard != null) {
            Button btn = (Button) playingCard.getProperties().get("playButton");
            if (btn != null) {
                btn.setText(textState);
            }
        }
    }

    private void addToQueue(AudioItem item){
        if (playbackBarController != null) {
            playbackBarController.addToQueue(item);
        }
    }

    private void handleRemove(AudioItem item){
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
                applyAll();
            }
        });
    }

    @FXML
    private void handleImport() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Import Audio Files");
        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Audio Files", "*.mp3", "*.wav", "*.aac")
        );

        Window window = cardGrid.getScene().getWindow();
        List<File> files = chooser.showOpenMultipleDialog(window);

        if (files != null && !files.isEmpty()) {
            // If only importing one track, show the wizard
            if (files.size() == 1) {
                File singleFile = files.get(0);
                AudioItem draftItem = MetadataExtractor.extract(singleFile.getPath());

                // Start the audio checker
                TrackEditDialog dialog = new TrackEditDialog(draftItem);
                dialog.showAndWait().ifPresent(finalizedItem -> {
                    masterList.add(finalizedItem);
                    audioStorage.addItem(finalizedItem);
                    applyAll();
                });
            }
            // If bulk import, silently import them
            else {
                int successCount = 0;

                for (File file : files) {
                    AudioItem item = MetadataExtractor.extract(file.getPath());
                    if (item != null) {
                        masterList.add(item);
                        audioStorage.addItem(item);
                        successCount++;
                    } else {
                        System.err.println("Skipped unreadable file: " + file.getName());
                    }
                }

                applyAll(); // Update UI once after the whole batch is done
                System.out.println("Bulk import complete. Added " + successCount + " tracks.");
            }
        }
    }

    @FXML
    private void handleFilter() {
        showFilterDialog();
    }

    public void showClearFilter(String label){
        btnClearFilter.setText("✕ " + label);
        btnClearFilter.setVisible(true);
        btnClearFilter.setManaged(true);

        btnFilter.getStyleClass().add("active");
    }

    @FXML
    public void handleClearFilter() {
        clearFilter();
        btnClearFilter.setVisible(false);
        btnClearFilter.setManaged(false);
        btnFilter.getStyleClass().remove("active");
    }

    @FXML
    private void handleSort() {
        String selected = sortComboBox.getValue();
        if (selected == null) return;

        applySortOrder(selected);
    }

    public void applySearch(String query) {
        currentQuery = query;
        applyAll();
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

    public void clearFilter(){
        filterCategory = null;
        filterOperator = null;
        filterValue    = null;
        applyAll();
    }

    private void applyAll() {
        // start from full master list
        List<AudioItem> result = new ArrayList<>(masterList);
        System.out.println("Before apply all, size: " + result.size());

        // apply search using matchesQuery()
        if(!currentQuery.isBlank()){
            result = LibraryLogic.search(masterList, currentQuery);
        }
        System.out.println("After search: " + result.size());

        // apply filter using passesFilter()
        if(filterCategory!=null){
            result = LibraryLogic.filter(masterList, filterCategory, filterOperator, filterValue);
        }
        System.out.println("After apply filter: " + result.size());

        // apply sort using compareTo()
        switch (currentSort){
            case TITLE_ASC -> Collections.sort(result);
            case TITLE_DESC -> result.sort((a,b) -> b.compareTo(a));
            case DATE_ASC -> result.sort(Comparator.comparing(AudioItem::getReleaseDate));
            case DATE_DESC -> result.sort(Comparator.comparing(AudioItem::getReleaseDate).reversed());
        }
        System.out.println("After sorting: " + result.size());
        // rebuild grid
        cardGrid.getChildren().clear();
        for (AudioItem item : result) {
            cardGrid.getChildren().add(buildCard(item));
        }
        updateTrackCount(result.size());

        if (playingID != null) {
            playingCard = null;
            highlightPlayingCard(playingID);
        }
    }

    public void highlightPlayingCard(String trackID) {
        // Clean up prev track
        playingID = trackID;
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
}
