package module4.playback;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import java.io.File;
import javafx.util.Duration;

public class AudioPlayer {
    protected MediaPlayer mediaPlayer;
    protected double userVolume = 0.5;

    public AudioPlayer() {
    }

    public void play(String filePath, Runnable onSongEnd) {
        // Stop previous mediaplayer to initialize new one
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.dispose();
        }

        // Get the audio file
        File audioFile = new File(filePath);
        if (!audioFile.exists()) {
            System.err.println("Error: Audio file does not exist");
            return;
        }

        // Create new Media and mediaPlayer
        Media media = new Media(audioFile.toURI().toString());
        mediaPlayer = new MediaPlayer(media);

        // Set volume for new mediaPlayer
        mediaPlayer.setVolume(userVolume);

        // Set continuous playback
        mediaPlayer.setOnEndOfMedia(onSongEnd);

        // Play
        mediaPlayer.play();
    }

    public void setVolume(double volume) {
        userVolume = volume < 0 ? 0 : volume > 1 ? 1 : volume;
        if  (mediaPlayer != null) {
            mediaPlayer.setVolume(userVolume);
        }
    }

    public void pause() {
        if (mediaPlayer != null) {
            mediaPlayer.pause();
        }
    }

    public void resume() {
        if (mediaPlayer != null) {
            mediaPlayer.play();
        }
    }

    public void stop() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
        }
    }

    public Duration getCurrentTime() {
        if (mediaPlayer == null) {
            return Duration.ZERO;
        }
        return mediaPlayer.getCurrentTime();
    }
}
