package com.yourname.controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import module1.audioModel.AudioItem;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.UUID;

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
        cardGrid.getChildren().clear();
        if (query.isBlank()) {
            rebuildGrid();
            return;
        }
        List<AudioItem> results = masterList.stream()
                .filter(item ->
                        item.getTitle().toLowerCase().contains(query.toLowerCase()) ||
                                item.getAuthor().toLowerCase().contains(query.toLowerCase())
                )
                .toList();

        for (AudioItem item : results) {
            cardGrid.getChildren().add(buildCard(item));
        }
        updateTrackCount();
    }

    public void cycleSortOrder()   { }
    public void showFilterDialog() { }

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

    private void updateTrackCount() {
        int count = masterList.size();
        trackCountLabel.setText(count + (count == 1 ? " song" : " songs"));
    }
}
