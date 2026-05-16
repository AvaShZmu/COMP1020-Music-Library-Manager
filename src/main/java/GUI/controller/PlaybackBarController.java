package GUI.controller;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.util.Duration;

import module1.audioModel.AudioItem;
import module4.playback.Controller;

import java.net.URL;
import java.util.ResourceBundle;

public class PlaybackBarController implements Initializable {
    @FXML private Label nowPlayingTitle;
    @FXML private Label  nowPlayingArtist;
    @FXML private Button btnPlayPause;
    @FXML private Button btnQueue;

    @FXML private Slider progressSlider;
    @FXML private Slider volumeSlider;
    @FXML private Label  currentTimeLabel;
    @FXML private Label  totalTimeLabel;
    private Timeline progressTimeline;

    private MainController mainController;
    private LibraryController libraryController;
    private QueueController queueController;
    private Controller playbackController;

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
    }

    // Set up controller
    public void setLibraryController(LibraryController lc) {this.libraryController = lc;}

    public void setQueueController(QueueController qc) {this.queueController = qc;}

    public void setMainController(MainController mc) {this.mainController = mc;}

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

    @FXML
    private void handlePrevious() {
        playbackController.playPrevious();
    }

    @FXML
    private void handleNext() {
        playbackController.playNext();
    }

    /*
    @FXML
    private void handleShuffle() {
        playbackController.shuffle();
    }

    @FXML
    private void handleRepeat() {
        // toggle repeat mode
    }
     */

    @FXML
    private void handleProgressPressed() {
        if(progressTimeline != null){
            progressTimeline.pause();
        }
    }

    @FXML
    private void handleProgressReleased() {
        double total  = playbackController.getDuration().toSeconds();
        System.out.println(progressSlider.getValue());
        double seekTo = (progressSlider.getValue() / 100.0) * total;
        System.out.println(seekTo);
        playbackController.setTime(seekTo);

        // Resume timeline after seek
        if (progressTimeline != null) {
            progressTimeline.play();
        }
    }

    @FXML
    private void handleVolumeChange() {
        playbackController.setVolume(volumeSlider.getValue());
    }


    /**
     * Called by LibraryController or PlaylistController when the user
     * double-clicks a track to start playing it.
     */
    public void playTrack(AudioItem item) {
        // Look up the AudioItem from AudioStorage directly
        if(item == null) {
            return;
        }

        // 3. Tell playback Controller to load and play
        //    Replace loadSingle with your actual method name
        // Clear whatever was in the queue
        playbackController.clearQueue();

        // Load the item and play it directly
        playbackController.loadSingle(item);
        playbackController.startPlayBack(item);

    }

    public void addToQueue(AudioItem item){
        if (playbackController.getCurrentTrack() == null) {
            playTrack(item);
        }
        else{
            playbackController.loadSingle(item);
            queueController.updateQueue(playbackController.getQueue());
        }
    }

    public void clearUpcoming() {
        playbackController.clearUpcoming();
    }

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

    private void updateNowPlaying() {
        AudioItem current = playbackController.getCurrentTrack();
        if (current == null) return;

        nowPlayingTitle.setText(current.getTitle());
        nowPlayingArtist.setText(current.getAuthor());
        btnPlayPause.setText("⏸");

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

    private void tickProgress() {
        double current = playbackController.getCurrentTime().toSeconds();
        double total   = playbackController.getDuration().toSeconds();

        if (total <= 0) return;

        // Update slider position
        progressSlider.setValue((current / total) * 100);

        // Update time labels
        currentTimeLabel.setText(formatTime((int) current));
        totalTimeLabel.setText(formatTime((int) total));
    }

    private String formatTime(int totalSeconds) {
        int minutes = totalSeconds / 60;
        int seconds = totalSeconds % 60;
        return String.format("%d:%02d", minutes, seconds);
    }

    // Setup methods to apply gradient to progress bar and volume bar
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
