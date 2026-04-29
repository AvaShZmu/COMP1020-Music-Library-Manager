package module4.playback;
import module1.audioModel.AudioItem;
import javafx.util.Duration;
import java.util.List;

public class Controller {
    protected PlaybackQueue queue;
    protected AudioPlayer player;

    public Controller() {
        queue = new PlaybackQueue();
        player = new AudioPlayer();
    }

    /* Methods for loading items onto the queue (queue-side) */

    public void loadSingle(AudioItem item) {
        queue.enqueue(item);
    }

    public void loadItems(List<AudioItem> items) {
        queue.buildQueueFromList(items);
    }

    public int getQueueSize() {
        return queue.getQueueSize();
    }

    public int getCurrIndex() {
        return queue.getCurrIndex();
    }

    /* Methods for audio playback (player-side) */

    public void startPlayBack(AudioItem item) {
        if (item == null || item.getFileLocation() == null) {
            player.stop();
            return;
        }
        player.play(item.getFileLocation(), this::playNext);
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

    // GUI showing stuff
    public AudioItem getCurrentTrack() {
        return queue.getCurrent();
    }
}
