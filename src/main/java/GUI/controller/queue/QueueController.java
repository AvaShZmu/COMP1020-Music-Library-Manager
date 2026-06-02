package GUI.controller.queue;

import GUI.controller.playback.PlaybackBarController;
import GUI.controller.util.AsyncImageLoader;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import module1.audioModel.AudioItem;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

/**
 * The UI controller that manages the "Up Next" sidebar queue.
 * <p>
 *     This class handles the rendering of the currently playing track's artwork and
 *     maintains an interactive {@link ListView} of upcoming tracks. Uses a custom
 *     cell factory to build styled list items that support double-click playback and
 *     context-menu removal.
 * </p>
 */

public class QueueController implements Initializable {

    /* FXML Bindings */

    // Now playing UI
    /** The container holding the currently playing track's metadata and cover art. */
    @FXML private HBox nowPlayingBox;

    /** The image view displaying the active track's cover. */
    @FXML private ImageView nowPlayingCover;

    /** The fallback icon label displayed if the active track lacks cover art. */
    @FXML private Label nowPlayingFallback;

    /** The label displaying the active track's title. */
    @FXML private Label nowPlayingTitle;

    /** The label displaying the active track's artist. */
    @FXML private Label nowPlayingArtist;

    // Queue UI
    /** The main list view displaying the upcoming tracks. */
    @FXML private ListView<AudioItem> queueListView;

    /** The dynamically bound list that automatically updates the ListView when changed. */
    private ObservableList<AudioItem> queueObservableList;

    private PlaybackBarController playbackBarController;

    /* Initialization */

    /**
     * Initializes the queue view, configures the clipping for "now-playing" image,
     * binds custom cell factoru to the Listview.
     */
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

    /* Dependencies */

    /** Injects the playback controller to allow the queue to send media commands. */
    public void setPlaybackBarController(PlaybackBarController pbc) {
        this.playbackBarController = pbc;
    }

    /* UI updates */

    /**
     * Refreshes the upcoming tracks list with the latest queue.
     *
     * @param queue A list of {@link AudioItem}s representing the upcoming tracks.
     */
    public void updateQueue(List<AudioItem> queue) {
        queueObservableList.setAll(queue);
    }

    /**
     * Updates the "Now Playing" banner at the top of the sidebar.
     * If no track is playing, the banner is hidden completely.
     *
     * @param current The currently playing {@link AudioItem}, or {@code null} if empty.
     */
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

    /** Clears all upcoming tracks from the queue and refreshes the UI. */
    @FXML
    private void handleClearQueue() {
        playbackBarController.clearUpcoming();
        updateQueue(queueObservableList);
    }

    /* Custom cell factory */

    /**
     * Generates a custom {@link ListCell} for the queue ListView.
     * <p>
     * Features in each cell:
     * - Asynchronous album art loading.
     * - Double-click listener to jump to track.
     * - Right-click context menu to remove the track from the upcoming queue.
     * </p>
     *
     * @return A styled, interactive {@link ListCell}.
     */
    private ListCell<AudioItem> createCellFactory() {
        return new ListCell<>() {
            private final HBox root = new HBox(12);
            private final StackPane artContainer = new StackPane();
            private final Label fallbackIcon = new Label("♪");
            private final ImageView coverArt = new ImageView();
            private final Label titleLabel = new Label();
            private final Label artistLabel = new Label();
            private final VBox textContainer = new VBox();

            // Initialization block for the cell layout and event listeners
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
                root.setPadding(new javafx.geometry.Insets(0, 0, 0, 0));
                root.getChildren().addAll(artContainer, textContainer);

                // Double click to play
                this.setOnMouseClicked(event -> {
                    if (event.getClickCount() == 2 && event.getButton() == MouseButton.PRIMARY && !isEmpty()) {
                        if (playbackBarController != null) {
                            // Pass the index of the cell
                            playbackBarController.playQueueItem(getIndex());
                        }
                    }
                });

                // Context menu for removing
                ContextMenu menu = new ContextMenu();
                menu.getStyleClass().add("right-click-menu");
                MenuItem remove = new MenuItem("Remove from Queue");

                remove.setOnAction(event -> {
                    if (playbackBarController != null) {
                        // Pass the index of cell
                        playbackBarController.removeQueueItem(getIndex());
                    }
                });

                menu.getItems().add(remove);

                // Bind context menu so it only appears if the cell actually contains a track
                this.contextMenuProperty().bind(
                        Bindings.when(this.emptyProperty()).then((ContextMenu) null).otherwise(menu)
                );
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
