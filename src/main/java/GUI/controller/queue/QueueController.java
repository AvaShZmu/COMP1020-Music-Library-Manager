package GUI.controller.queue;
import GUI.controller.playback.PlaybackBarController;
import GUI.controller.util.AsyncImageLoader;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import module1.audioModel.AudioItem;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;


public class QueueController implements Initializable {
    // Now Playing
    @FXML private HBox nowPlayingBox;
    @FXML private ImageView nowPlayingCover;
    @FXML private Label nowPlayingFallback;
    @FXML private Label nowPlayingTitle;
    @FXML private Label nowPlayingArtist;

    // Queue
    @FXML private ListView<AudioItem> queueListView;
    private ObservableList<AudioItem> queueObservableList;

    // Controller
    private PlaybackBarController playbackBarController;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Set up now playing image
        Rectangle clip = new Rectangle(48, 48);
        clip.setArcWidth(16);
        clip.setArcHeight(16);
        nowPlayingCover.setClip(clip);

        // Set up queue observable list
        queueObservableList = FXCollections.observableArrayList();
        queueListView.setItems(queueObservableList);
        queueListView.setCellFactory(param -> createCellFactory());
    }

    public void setPlaybackBarController(PlaybackBarController pbc) {
        this.playbackBarController = pbc;
    }

    public void updateQueue(List<AudioItem> queue) {
        queueObservableList.setAll(queue);
    }

    public void updateNowPlaying(AudioItem current) {
        if (current != null) {
            // Show the box and update text
            nowPlayingBox.setVisible(true);
            nowPlayingBox.setManaged(true);
            nowPlayingTitle.setText(current.getTitle());
            nowPlayingArtist.setText(current.getAuthor());
            AsyncImageLoader.playbarImageLoad(current.getFileLocation(), nowPlayingCover, nowPlayingFallback, current);
        } else {
            nowPlayingBox.setVisible(false);
            nowPlayingBox.setManaged(false);
        }
    }

    @FXML
    private void handleClearQueue() {
        playbackBarController.clearUpcoming();
        updateQueue(queueObservableList);
    }

    // Helper method to create cell factory
    private ListCell<AudioItem> createCellFactory() {
        return new ListCell<>() {
            private final HBox root = new HBox(12);

            private final StackPane artContainer = new StackPane();
            private final Label fallbackIcon = new Label("♪");

            private final ImageView coverArt = new ImageView();
            private final Label titleLabel = new Label();
            private final Label artistLabel = new Label();

            private final VBox textContainer = new VBox();

            {
                root.setMaxWidth(230);

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

                    AsyncImageLoader.cellImageLoad(item.getFileLocation(), coverArt, fallbackIcon, this, item);

                    setGraphic(root);
                }
            }
        };
    }
}
