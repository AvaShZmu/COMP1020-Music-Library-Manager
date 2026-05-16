package GUI.controller;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import module1.audioModel.AudioItem;
import module5.util.MetadataExtractor;

import java.io.ByteArrayInputStream;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.CompletableFuture;


public class QueueController implements Initializable {
    @FXML private ListView<AudioItem> nowPlayingListView;
    @FXML private ListView<AudioItem> queueListView;

    private ObservableList<AudioItem> nowPlayingObservableList;
    private ObservableList<AudioItem> queueObservableList;

    private PlaybackBarController playbackBarController;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // bg list containing audio items
        nowPlayingObservableList = FXCollections.observableArrayList();
        queueObservableList = FXCollections.observableArrayList();

        // the ListView is to display on UI
        queueListView.setItems(queueObservableList);
        nowPlayingListView.setItems(nowPlayingObservableList);

        // Defining a custom listcell (each element of a listview)
        nowPlayingListView.setCellFactory(param -> createCellFactory(true));
        queueListView.setCellFactory(param -> createCellFactory(false));
    }

    public void setPlaybackBarController(PlaybackBarController pbc) {
        this.playbackBarController = pbc;
    }

    public void updateQueue(List<AudioItem> queue) {
        queueObservableList.setAll(queue);
    }

    public void updateNowPlaying(AudioItem current) {
        if (current != null) {
            nowPlayingObservableList.setAll(current);
        } else {
            nowPlayingObservableList.clear();
        }
    }

    @FXML
    private void handleClearQueue() {
        playbackBarController.clearUpcoming();
        updateQueue(queueObservableList);
    }

    // Helper method to create cell factory
    private ListCell<AudioItem> createCellFactory(boolean isPlaying) {
        return new ListCell<>() {
            private final HBox root = new HBox(12);

            private final StackPane artContainer = new StackPane();
            private final Label fallbackIcon = new Label("♪");

            private final ImageView coverArt = new ImageView();
            private final Label titleLabel = new Label();
            private final Label artistLabel = new Label();

            private final VBox textContainer = new VBox();

            {
                // Set up grey container
                artContainer.setPrefSize(48, 48);
                artContainer.setMinSize(48, 48);
                artContainer.setMaxSize(48, 48);
                artContainer.getStyleClass().add("queue-art-container");
                fallbackIcon.getStyleClass().add("queue-fallback-icon");

                // Set up cover art
                coverArt.setFitWidth(48);
                coverArt.setFitHeight(48);

                javafx.scene.shape.Rectangle clip = new javafx.scene.shape.Rectangle(48, 48);
                clip.setArcWidth(8);
                clip.setArcHeight(8);
                coverArt.setClip(clip);

                artContainer.getChildren().addAll(fallbackIcon, coverArt);

                titleLabel.getStyleClass().add("queue-title-text");

                if (isPlaying) {titleLabel.getStyleClass().add("active");}

                artistLabel.getStyleClass().add("queue-artist-text");

                textContainer.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
                textContainer.getChildren().addAll(titleLabel, artistLabel);

                root.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

                // Ensure the HBox has 0 left padding (Top, Right, Bottom, Left)
                root.setPadding(new javafx.geometry.Insets(0, 0, 0, 0));

                root.getChildren().addAll(artContainer, textContainer);
            }

            @Override
            protected void updateItem(AudioItem item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || item == null) {
                    setGraphic(null);
                    setText(null);
                } else {
                    titleLabel.setText(item.getTitle());
                    artistLabel.setText(item.getAuthor());
                    coverArt.setImage(null);

                    CompletableFuture.supplyAsync(() -> MetadataExtractor.getImage(item.getFileLocation()))
                            .thenAccept(imageBytes -> Platform.runLater(() -> {
                                if (getItem() == item && imageBytes != null && imageBytes.length > 0) {
                                    coverArt.setImage(new Image(new ByteArrayInputStream(imageBytes)));
                                }
                            }));

                    setGraphic(root);
                }
            }
        };
    }
}
