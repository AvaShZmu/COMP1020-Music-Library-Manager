package module4.playback;

import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import java.io.File;
import javafx.util.Duration;

/**
 * A wrapper around the JavaFX's {@link MediaPlayer} object.
 * May handle other audio players in the future (especially FLAC)
 * <p>
 *     This class isolates the direct execution of audio files, managing
 *     media instantiation, resource disposal, and basic playback utilities.
 * </p>
 */

public class AudioPlayer {
    protected MediaPlayer mediaPlayer;
    protected double userVolume = 0.5;

    /**
     * Constructs an empty {@link AudioPlayer} object for loading media.
     */
    public AudioPlayer() {
    }

    /* Core Playback */

    /**
     * Initializes and plays an audio file from a given local path.
     * Automatically stops and disposes of current playing media to prevent
     * memory leaks and overlapping audio streams.
     *
     * @param filePath The path to the local audio file.
     * @param onSongEnd A {@link Runnable} callback triggered when the song finishes naturally.
     */
    public void play(String filePath, Runnable onSongEnd) {
        // Stop previous MediaPlayer to initialize new one
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

    /**
     * Pauses the current audio track.
     */
    public void pause() {
        if (mediaPlayer != null) {
            mediaPlayer.pause();
        }
    }

    /**
     * Resumes the current audio track from a paused state.
     */
    public void resume() {
        if (mediaPlayer != null) {
            mediaPlayer.play();
        }
    }

    /**
     * Stops the audio track.
     */
    public void stop() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
        }
    }

    /* Other controls */

    /**
     * Adjust the playback volume, ranging from 0.0 to 1.0.
     * @param volume The target volume, as a double.
     */
    public void setVolume(double volume) {
        userVolume = volume < 0 ? 0 : volume > 1 ? 1 : volume;
        if  (mediaPlayer != null) {
            mediaPlayer.setVolume(userVolume);
        }
    }

    /**
     * Seeks to a specific timestamp in the current track.
     *
     * @param time The target {@link Duration} to jump to.
     */
    public void setTime(Duration time){
        if (mediaPlayer == null) {
            return;
        }
        mediaPlayer.seek(time);
    }

    /**
     * Retrieves the current elapsed time of the playing track.
     *
     * @return The current time as a {@link Duration}, or zero if no media is loaded.
     */
    public Duration getCurrentTime() {
        if (mediaPlayer == null) {
            return Duration.ZERO;
        }
        return mediaPlayer.getCurrentTime();
    }

    /**
     * Retrieves the total duration of the currently loaded track directly from the media file.
     *
     * @return The total {@link Duration}, or zero if no media is loaded.
     */
    public Duration getDuration(){
        if (mediaPlayer == null){
            return Duration.ZERO;
        }
        return mediaPlayer.getTotalDuration();
    }
}
