package module4.playback;
import module1.audioModel.AudioItem;
import javafx.util.Duration;
import java.util.List;

public class Controller {
    protected PlaybackQueue queue;
    protected AudioPlayer player;

    private Runnable onTrackChanged; // For UI update

    public Controller() {
        queue = new PlaybackQueue();
        player = new AudioPlayer();
    }

    public void setOnTrackChangedListener(Runnable listener) {
        this.onTrackChanged = listener;
    }

    /* Methods for loading items onto the queue (queue-side) */

    public void loadSingle(AudioItem item) {
        queue.enqueue(item);
    }

    public void loadItems(List<AudioItem> items) {
        queue.buildQueueFromList(items);
    }

    public void clearQueue() { queue.clear(); }

    public void clearUpcoming() { queue.clearUpcoming(); }

    public int getQueueSize() {
        return queue.getQueueSize();
    }

    public int getCurrIndex() {
        return queue.getCurrIndex();
    }

    public List<AudioItem> getQueue() {
        return queue.getQueue();
    }

    /* Methods for audio playback (player-side) */

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

    // When song ends or user clicks on "next" icon, natural progression
    public void playNext() {
        AudioItem nextTrack = queue.getNext();
        if (nextTrack == null) {
            return;
        }
        startPlayBack(nextTrack);
    }

    // When user clicks on a separate track, inject it into latest position in queue
    public void playNext(AudioItem item) {
        if (item == null) {
            return;
        }
        queue.enqueueNext(item);
        playNext();
    }

    public void playPrevious() {
        AudioItem previousTrack = queue.getPrevious();
        if (previousTrack == null) {
            return;
        }
        startPlayBack(previousTrack);
    }

    public void playIndex(int index) {
        AudioItem selectedTrack = queue.getIndex(index);
        if (selectedTrack == null) {
            return;
        }
        startPlayBack(selectedTrack);
    }

    /* Controls */
    public void pause() {
        player.pause();
    }

    public void resume() {
        player.resume();
    }

    public void stop() {
        player.stop();
    }

    public void setVolume(double volume) {
        player.setVolume(volume);
    }

    public Duration getCurrentTime() {
        return player.getCurrentTime();
    }

    public void setTime(double time) {player.setTime(Duration.seconds(time));}

    public Duration getDuration() {
        return player.getDuration();
    }

    // GUI showing stuff
    public AudioItem getCurrentTrack() {
        return queue.getCurrent();
    }
}
