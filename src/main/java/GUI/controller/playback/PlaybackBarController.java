package GUI.controller.playback;

import GUI.controller.queue.QueueController;
import GUI.controller.library.LibraryController;
import GUI.controller.main.MainController;
import GUI.controller.util.AsyncImageLoader;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.image.ImageView;
import javafx.util.Duration;
import module1.audioModel.AudioItem;
import module4.playback.Controller;
import static module5.util.LibraryLogic.formatTime;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

/**
 * The UI controller managing the persistent bottom playback bar.
 * <p>
 *     This class serves as the visual frontend for the Module 4 playback engine.
 *     It manages media controls (play, pause, skip), dynamic progress/volume sliders,
 *     and synchronizes the global "Now Playing" state across the library and queue views.
 * </p>
 */

public class PlaybackBarController implements Initializable {

    /* FXML bindings, state variables */

    /** The fallback icon in case image does not load */
    @FXML private Label fallbackIcon;

    /** The cover image of current track */
    @FXML private ImageView nowPlayingImage;

    /** The title of current track */
    @FXML private Label nowPlayingTitle;

    /** The artist of current track */
    @FXML private Label  nowPlayingArtist;

    /** Main play button on the playback bar below */
    @FXML private Button btnPlayPause;

    /** The queue button to open queue interface */
    @FXML private Button btnQueue;

    /** The progress slider below the play button */
    @FXML private Slider progressSlider;

    /** The volume slider next to the queue button */
    @FXML private Slider volumeSlider;

    /** The left label indicating elapsed time of the current track */
    @FXML private Label  currentTimeLabel;

    /** The right label indicating total length of the current track */
    @FXML private Label  totalTimeLabel;

    /** Timeline object used to display elapsed time visually on the slider */
    private Timeline progressTimeline;

    /* Controllers */

    private MainController mainController;
    private LibraryController libraryController;
    private QueueController queueController;
    private Controller playbackController;

    /* Initialization */

    /**
     * Automatically invoked by JavaFX after the FXML files have been loaded.
     * Initializes default UI states of the playback bar and the sliders, now-playing image.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Initialize playback controller
        this.playbackController = new Controller();
        playbackController.setOnTrackChangedListener(() -> Platform.runLater(this::updateNowPlaying));

        // Volume slider initial sync
        volumeSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            // Update volume
            if (playbackController != null) {
                playbackController.setVolume(newVal.doubleValue());
            }
            // Update gradient
            updateVolumeGradient();
        });

        // Listener to color track as it progresses
        progressSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            updateProgressGradient();
        });

        // initialize gradient after ui renders
        javafx.application.Platform.runLater(() -> {
            updateVolumeGradient();
            updateProgressGradient();
        });

        // Rounded corners for image
        javafx.scene.shape.Rectangle clip = new javafx.scene.shape.Rectangle(60, 60);
        clip.setArcWidth(16);
        clip.setArcHeight(16);
        nowPlayingImage.setClip(clip);
    }

    /* Dependencies */

    /** Injects the library controller to sync active track highlights. */
    public void setLibraryController(LibraryController lc) {this.libraryController = lc;}

    /** Injects the queue controller to update the "Up Next" sidebar. */
    public void setQueueController(QueueController qc) {this.queueController = qc;}

    /** Injects the main facade controller for layout toggling. */
    public void setMainController(MainController mc) {this.mainController = mc;}

    /* Media Controls */

    /** Toggles the playback state between play and pause, syncing the UI buttons. */
    @FXML
    public void handlePlayPause() {
        if(btnPlayPause.getText().equals("⏸")){
            playbackController.pause();
            btnPlayPause.setText("▶");
        }
        else {
            playbackController.resume();
            btnPlayPause.setText("⏸");
        }

        if (libraryController != null) {
            libraryController.syncActiveCardButton(btnPlayPause.getText());
        }
    }

    /**
     * Reverts to the previous track, or restarts the current track if it is the first item
     * in the queue.
     */
    @FXML
    private void handlePrevious() {
        if (playbackController.getCurrIndex() == 0) {
            playbackController.startPlayBack(playbackController.getCurrentTrack());
            return;
        }
        playbackController.playPrevious();
    }

    /**
     * Advances to the next track in the queue, or restarts the current track if it is the final
     * item in the queue.
     */
    @FXML
    private void handleNext() {
        // If last song ends, clicking "next" wont work
        if (playbackController.getCurrIndex() >= playbackController.getQueueSize()) {
            return;
        }

        // If last song still playing, clicking "next" restarts song
        if (playbackController.getCurrIndex() == playbackController.getQueueSize() - 1) {
            playbackController.startPlayBack(playbackController.getCurrentTrack());
            return;
        }

        playbackController.playNext();
    }

    /** Toggles the visibility of the Queue sidebar pane. */
    @FXML
    public void handleQueueToggle() {
        if (mainController != null) {
            mainController.toggleQueue();

            if (btnQueue.getStyleClass().contains("active")) {
                btnQueue.getStyleClass().remove("active");
            } else {
                btnQueue.getStyleClass().add("active");
            }
        }
    }

    /* Slider Interactions */

    /** Pauses the progress timeline while the user is actively dragging the slider. */
    @FXML
    private void handleProgressPressed() {
        if(progressTimeline != null){
            progressTimeline.pause();
        }
    }

    /** Calculates the target timestamp and seeks the audio player upon slider release. */
    @FXML
    private void handleProgressReleased() {
        double total  = playbackController.getDuration().toSeconds();
        double seekTo = (progressSlider.getValue() / 100.0) * total;
        playbackController.setTime(seekTo);

        // Resume timeline after seek
        if (progressTimeline != null) {
            progressTimeline.play();
        }
    }

    /** Updates the audio engine volume when the volume slider is adjusted. */
    @FXML
    private void handleVolumeChange() {
        playbackController.setVolume(volumeSlider.getValue());
    }

    /* Public playback API */

    /**
     * Clears the current queue and immediately initiates playback of a specific track.
     * Called when a user double-clicks a track in the library view.
     *
     * @param item The {@link AudioItem} to play.
     */
    public void playTrack(AudioItem item) {
        if(item == null) {
            return;
        }
        playbackController.clearQueue();
        playbackController.loadSingle(item);
        playbackController.startPlayBack(item);

    }

    /**
     * Appends a track to the current queue. If nothing is playing, playback begins immediately.
     *
     * @param item The {@link AudioItem} to enqueue.
     */
    public void addToQueue(AudioItem item){
        if (playbackController.getCurrentTrack() == null) {
            playbackController.loadSingle(item);
            playbackController.startPlayBack(playbackController.getCurrentTrack());
        }
        else {
            playbackController.loadSingle(item);
        }
        queueController.updateQueue(playbackController.getQueue());
    }

    /**
     * Replaces the active queue with an entire playlist and begins playback at a specific index.
     *
     * @param playlist The list of {@link AudioItem}s representing the playlist.
     * @param index    The integer index of the track to start playing.
     */
    public void playPlaylist(List<AudioItem> playlist, int index){
        if (playlist == null) {
            return;
        }
        playbackController.clearQueue();
        playbackController.loadItems(playlist);
        playbackController.playIndex(index);
        queueController.updateQueue(playbackController.getQueue());
    }

    /** Clears all tracks scheduled to play after the currently active track. */
    public void clearUpcoming() {
        playbackController.clearUpcoming();
    }

    /**
     * Skips directly to a specific track within the upcoming queue using its exact UI index.
     *
     * @param uiIndex The integer index of the cell clicked in the Queue UI.
     */
    public void playQueueItem(int uiIndex) {
        if (uiIndex < 0) return;

        // Calculate the absolute index in the backend array and play it
        int absoluteIndex = playbackController.getCurrIndex() + 1 + uiIndex;
        playbackController.playIndex(absoluteIndex);

        // Sync the UI queue
        if (queueController != null) {
            queueController.updateQueue(playbackController.getQueue());
        }
    }

    /**
     * Removes a specific track from the upcoming queue using its exact UI index.
     *
     * @param uiIndex The integer index of the cell clicked in the Queue UI.
     */
    public void removeQueueItem(int uiIndex) {
        if (uiIndex < 0) return;

        // Get the upcoming queue
        List<AudioItem> upcoming = playbackController.getQueue();

        if (uiIndex < upcoming.size()) {
            // Remove the exact item by index, ignoring duplicates
            upcoming.remove(uiIndex);

            // Clear the backend upcoming queue
            playbackController.clearUpcoming();

            // Re-enqueue the remaining items to rebuild the queue safely
            for (AudioItem remainingItem : upcoming) {
                playbackController.loadSingle(remainingItem);
            }

            // Sync the UI queue
            if (queueController != null) {
                queueController.updateQueue(playbackController.getQueue());
            }
        }
    }

    /* Helpers */

    /**
     * Updates the UI metadata, album art, and sibling controllers when the active track changes.
     */
    private void updateNowPlaying() {
        AudioItem current = playbackController.getCurrentTrack();

        if (current == null) {
            endQueue();
            return;
        };

        nowPlayingTitle.setText(current.getTitle());
        nowPlayingArtist.setText(current.getAuthor());
        btnPlayPause.setText("⏸");
        AsyncImageLoader.playbarImageLoad(current.getFileLocation(), nowPlayingImage, fallbackIcon, current);

        // Update card highlight in library if visible
        if (libraryController != null) {
            libraryController.highlightPlayingCard(current.getTrackID());
        }

        // Update queue
        if (queueController != null) {
            queueController.updateNowPlaying(current);
            queueController.updateQueue(playbackController.getQueue());
        }

        // Restart timeline for new track
        startProgressTimeline();
    }

    /**
     * Resets the playback bar to a blank, default state once the queue ended.
     */
    private void endQueue() {
        nowPlayingImage.setImage(null);
        nowPlayingImage.setUserData(null); // Clear the identity tag
        fallbackIcon.setVisible(true);

        nowPlayingTitle.setText("No track selected");
        nowPlayingArtist.setText("...");
        btnPlayPause.setText("▶");

        currentTimeLabel.setText("0:00");
        totalTimeLabel.setText("0:00");
        progressSlider.setValue(0);
        updateProgressGradient();

        if (progressTimeline != null) {
            progressTimeline.stop();
        }

        if (libraryController != null) {
            libraryController.highlightPlayingCard(null);
        }

        if (queueController != null) {
            queueController.updateNowPlaying(null);
        }
    }

    /** Initializes the timeline loop that updates the progress slider every second. */
    private void startProgressTimeline() {
        // Stop any existing timeline first
        if (progressTimeline != null) {
            progressTimeline.stop();
        }

        progressTimeline = new Timeline(
                new KeyFrame(Duration.seconds(1), e -> tickProgress())
        );
        progressTimeline.setCycleCount(Timeline.INDEFINITE);
        progressTimeline.play();
    }

    /** Calculates the current media progress and updates the slider UI. */
    private void tickProgress() {
        double current = playbackController.getCurrentTime().toSeconds();
        double total   = playbackController.getDuration().toSeconds();

        if (total <= 0) return;

        progressSlider.setValue((current / total) * 100);
        currentTimeLabel.setText(formatTime((int) current));
        totalTimeLabel.setText(formatTime((int) total));
    }

    /** Recolors the active track portion of the volume slider. */
    private void updateVolumeGradient() {
        javafx.scene.Node track = volumeSlider.lookup(".track");
        if (track != null) {
            // Prevent division by zero or negative max
            double max = volumeSlider.getMax() <= 0 ? 100 : volumeSlider.getMax();
            double percentage = (volumeSlider.getValue() / max) * 100.0;

            // If math results in NaN or infinity, default to 0
            if (Double.isNaN(percentage) || Double.isInfinite(percentage)) {
                percentage = 0.0;
            }

            // Locale.US ensures decimals use a period (.), preventing CSS crashes in other regions
            String style = String.format(java.util.Locale.US,
                    "-fx-background-color: linear-gradient(to right, #ffffff %f%%, #535353 %f%%);",
                    percentage, Math.max(percentage, 0.1)
            );
            track.setStyle(style);
        }
    }

    /** Recolors the active track portion of the progress slider. */
    private void updateProgressGradient() {
        javafx.scene.Node track = progressSlider.lookup(".track");
        if (track != null) {
            double max = progressSlider.getMax() <= 0 ? 100 : progressSlider.getMax();
            double percentage = (progressSlider.getValue() / max) * 100.0;

            // If math results in NaN or infinity, default to 0
            if (Double.isNaN(percentage) || Double.isInfinite(percentage)) {
                percentage = 0.0;
            }

            String style = String.format(java.util.Locale.US,
                    "-fx-background-color: linear-gradient(to right, #ffffff %f%%, #535353 %f%%);",
                    percentage, Math.max(percentage, 0.1)
            );
            track.setStyle(style);
        }
    }
}
