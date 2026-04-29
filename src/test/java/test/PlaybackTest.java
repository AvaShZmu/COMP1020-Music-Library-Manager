package test;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;
import module1.audioModel.AudioItem;
import module4.playback.Controller;

import java.io.File;

public class PlaybackTest extends Application {

    // Controller
    private Controller controller;

    // UI Elements
    private Label nowPlayingLabel;
    private Label timeLabel;
    private Button btnPlayPause; // Unified toggle button

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        System.out.println("--- Booting Refined UI Test ---");

        // 1. Initialize the Controller (Queue is empty by default)
        controller = new Controller();

        // 2. Create Mock AudioItems
        AudioItem song1 = createItem("Whispering about life", 538, "src/test/resources/musicFolder/test1.mp3");
        AudioItem song2 = createItem("Qimranut", 256, "src/test/resources/musicFolder/test2.mp3");
        AudioItem song3 = createItem("g444444444g", 209, "src/test/resources/musicFolder/test3.mp3");

        /* UI */

        nowPlayingLabel = new Label("Queue is Empty");
        nowPlayingLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        timeLabel = new Label("00:00 / 00:00");
        timeLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: gray;");
        timeLabel.setPadding(new Insets(0, 0, 15, 0)); // Added padding below the time

        // Add to queue options
        Label queueControlsLabel = new Label("Add Songs to Queue:");
        queueControlsLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

        Button btnAddSong1 = new Button("+ Whispering about life");
        btnAddSong1.setOnAction(e -> {
            controller.loadSingle(song1);
            System.out.println("Added: " + song1.getTitle());
        });

        Button btnAddSong2 = new Button("+ Qimranut");
        btnAddSong2.setOnAction(e -> {
            controller.loadSingle(song2);
            System.out.println("Added: " + song2.getTitle());
        });

        Button btnAddSong3 = new Button("+ g444444444g");
        btnAddSong3.setOnAction(e -> {
            controller.loadSingle(song3);
            System.out.println("Added: " + song3.getTitle());
        });

        VBox addQueueBox = new VBox(10, queueControlsLabel, btnAddSong1, btnAddSong2, btnAddSong3);
        addQueueBox.setAlignment(Pos.CENTER);
        addQueueBox.setPadding(new Insets(0, 0, 20, 0)); // Padding before playback controls

        // Playback
        Button btnPrev = new Button("|<");
        btnPrev.setStyle("-fx-font-size: 16px;");
        btnPrev.setOnAction(e -> {
            controller.playPrevious();
            btnPlayPause.setText("⏸");
        });

        btnPlayPause = new Button("▶");
        btnPlayPause.setStyle("-fx-font-size: 16px; -fx-min-width: 50px;");
        btnPlayPause.setOnAction(e -> {
            if (btnPlayPause.getText().equals("▶")) {

                // 1. Safety check: Do nothing if queue is entirely empty
                if (controller.getQueueSize() == 0) {
                    return;
                }

                if (controller.getCurrentTime().toSeconds() == 0.0) {
                    controller.playIndex(controller.getCurrIndex());
                } else {
                    controller.resume();
                }

                btnPlayPause.setText("⏸");
            } else {
                controller.pause();
                btnPlayPause.setText("▶");
            }
        });

        Button btnNext = new Button(">|");
        btnNext.setStyle("-fx-font-size: 16px;");
        btnNext.setOnAction(e -> {
            controller.playNext();
            btnPlayPause.setText("⏸");
        });

        HBox playbackControls = new HBox(15, btnPrev, btnPlayPause, btnNext);
        playbackControls.setAlignment(Pos.CENTER);

        // Volume slider
        Label volumeLabel = new Label("Volume: 50%");
        Slider volumeSlider = new Slider(0.0, 1.0, 0.5);
        volumeSlider.setShowTickMarks(true);
        volumeSlider.setMajorTickUnit(0.25);
        volumeSlider.setBlockIncrement(0.1);
        volumeSlider.setMaxWidth(200);

        volumeSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            controller.setVolume(newValue.doubleValue());
            volumeLabel.setText(String.format("Volume: %.0f%%", newValue.doubleValue() * 100));
        });

        VBox volumeBox = new VBox(5, volumeLabel, volumeSlider);
        volumeBox.setAlignment(Pos.CENTER);
        volumeBox.setPadding(new Insets(20, 0, 0, 0));

        // New time line to update UI elements
        Timeline uiUpdater = new Timeline(new KeyFrame(Duration.millis(100), e -> {
            updateLabels();
        }));
        uiUpdater.setCycleCount(Timeline.INDEFINITE);
        uiUpdater.play();

        // Add everything to layout
        VBox layout = new VBox(10);
        layout.setPadding(new Insets(20));
        layout.setAlignment(Pos.CENTER);

        layout.getChildren().addAll(
                nowPlayingLabel,      // Top Header
                timeLabel,            // Sub-Header
                addQueueBox,          // Middle Content
                playbackControls,     // Audio Controls
                volumeBox             // Bottom Slider
        );

        Scene scene = new Scene(layout, 400, 400);
        primaryStage.setTitle("Playback Test");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    // update label helper method
    private void updateLabels() {
        AudioItem currentTrack = controller.getCurrentTrack();
        Duration currentTime = controller.getCurrentTime();

        int queueSize = controller.getQueueSize();
        int currIndex = controller.getCurrIndex() + 1; // +1 so the UI reads "1/3" instead of "0/3"

        if (queueSize == 0) {
            nowPlayingLabel.setText("Queue is Empty");
            timeLabel.setText("00:00 / 00:00");
        } else if (currentTrack != null) {
            nowPlayingLabel.setText(String.format("Now Playing: %s [%d/%d]", currentTrack.getTitle(), currIndex, queueSize));

            if (currentTime != null) {
                double currentSeconds = currentTime.toSeconds();
                double totalSeconds = currentTrack.getDuration();
                timeLabel.setText(formatTime(currentSeconds) + " / " + formatTime(totalSeconds));
            }

            if (currentTime != null && currentTime.toSeconds() >= currentTrack.getDuration() - 0.5) {
                if (currIndex == queueSize) {
                    btnPlayPause.setText("▶");
                }
            }

        } else {
            nowPlayingLabel.setText(String.format("Stopped [%d/%d]", currIndex, queueSize));
            timeLabel.setText("00:00 / 00:00");
        }
    }

    private String formatTime(double totalSeconds) {
        int minutes = (int) (totalSeconds / 60);
        int seconds = (int) (totalSeconds % 60);
        return String.format("%02d:%02d", minutes, seconds);
    }

    private AudioItem createItem(String title, int duration, String filePath) {
        File file = new File(filePath);
        if (!file.exists()) {
            System.err.println("WARNING: File not found: " + file.getAbsolutePath());
        }

        return new AudioItem(
                title,
                "Mock Artist",
                "2026",
                duration,
                "Mock Genre",
                file.getAbsolutePath()
        );
    }
}