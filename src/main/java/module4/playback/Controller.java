package module4.playback;

import module1.audioModel.AudioItem;
import javafx.util.Duration;
import java.util.List;

/**
 * Acts as the central coordinator for the entire playback engine.
 * <p>
 *     This class coordinates the interaction between {@link PlaybackQueue} and {@link AudioPlayer}.
 *     The GUI will only interact with this central class, as {@link PlaybackQueue} and
 *     {@link AudioPlayer} are both files that will act under the hood.
 * </p>
 */

public class Controller {
    protected PlaybackQueue queue;
    protected AudioPlayer player;
    private Runnable onTrackChanged; // For UI update

    /**
     * Constructs a new playback controller, which initializes an empty queue and an audio player.
     */
    public Controller() {
        queue = new PlaybackQueue();
        player = new AudioPlayer();
    }

    /**
     * Registers a callback listener to trigger UI updates whenever the playing track changes.
     *
     * @param listener The {@link Runnable} to execute on track transitions.
     */
    public void setOnTrackChangedListener(Runnable listener) {
        this.onTrackChanged = listener;
    }

    /* Queue management */

    /** Appends a single track to the end of queue. */
    public void loadSingle(AudioItem item) {
        queue.enqueue(item);
    }

    /** Populates the queue with an entire list of audio tracks (typical for playlist usage) */
    public void loadItems(List<AudioItem> items) {
        queue.buildQueueFromList(items);
    }

    /** Completely clears the entire playback queue and resets the index. */
    public void clearQueue() { queue.clear(); }

    /** Clears only the tracks scheduled after the current track. */
    public void clearUpcoming() { queue.clearUpcoming(); }

    /** Returns the total number of tracks currently in the queue. */
    public int getQueueSize() {
        return queue.getQueueSize();
    }

    /** Returns the current active index inside the queue. */
    public int getCurrIndex() {
        return queue.getCurrIndex();
    }

    /** Returns the list of upcoming {@link AudioItem}s inside the queue. */
    public List<AudioItem> getQueue() {
        return queue.getQueue();
    }

    /* Playback handling */

    /**
     * Immediately initiates playback of a specific audio item.
     * @param item The {@link AudioItem} to play.
     */
    public void startPlayBack(AudioItem item) {
        if (item == null || item.getFileLocation() == null) {
            player.stop();
            return;
        }
        player.play(item.getFileLocation(), this::playNext);

        // Trigger UI update
        if (this.onTrackChanged != null) {
            this.onTrackChanged.run();
        }
    }

    /**
     * Reverts the queue to the previous track and initiates playback.
     */
    public void playPrevious() {
        AudioItem previousTrack = queue.getPrevious();
        if (previousTrack == null) {
            return;
        }
        startPlayBack(previousTrack);
    }

    /**
     * Jumps directly to a specific track in the queue.
     *
     * @param index The target array index.
     */
    public void playIndex(int index) {
        AudioItem selectedTrack = queue.getIndex(index);
        if (selectedTrack == null) {
            return;
        }
        startPlayBack(selectedTrack);
    }

    /**
     * Advances the queue to the next track.
     * Is called after a song ends, or when user clicks on "next" button in navigation.
     */
    public void playNext() {
        AudioItem nextTrack = queue.getNext();
        if (nextTrack == null) {
            if (this.onTrackChanged != null) {
                this.onTrackChanged.run();
            }
            return;
        }
        startPlayBack(nextTrack);
    }

    /**
     * Immediately injects a selected track as the next item in the queue and plays it.
     * Initially was used when clicking on a track in the library with a populated queue,
     * but was later changed and is now a backup option.
     *
     * @param item The {@link AudioItem} to "force play".
     */
    public void playNext(AudioItem item) {
        if (item == null) {
            return;
        }
        queue.enqueueNext(item);
        playNext();
    }

    /* Controls */

    /** Pauses active playback. */
    public void pause() {
        player.pause();
    }

    /** Resumes paused playback. */
    public void resume() {
        player.resume();
    }

    /** Stops playback entirely. */
    public void stop() {
        player.stop();
    }

    /** Sets the system volume (0.0 to 1.0). */
    public void setVolume(double volume) {
        player.setVolume(volume);
    }

    /** Jumps to a specific timestamp in seconds. */
    public void setTime(double time) {player.setTime(Duration.seconds(time));}

    /** Retrieves the current elapsed playback time. */
    public Duration getCurrentTime() {
        return player.getCurrentTime();
    }

    /** Retrieves the total duration of the active track. */
    public Duration getDuration() {
        return player.getDuration();
    }

    /** Retrieves the currently playing {@link AudioItem} for UI rendering. */
    public AudioItem getCurrentTrack() {
        return queue.getCurrent();
    }
}
