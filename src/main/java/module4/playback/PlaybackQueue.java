package module4.playback;
import java.util.ArrayList;
import java.util.List;
import module1.audioModel.AudioItem;

public class PlaybackQueue {
    protected List<AudioItem> queue;
    protected int currIndex;

    public PlaybackQueue() {
        queue = new ArrayList<>();
        currIndex = -1; // When starting, since theres no element yet, currIndex is -1
    }

    public int getQueueSize() {
        return queue.size();
    }

    public int getCurrIndex() {
        return currIndex;
    }

    public void enqueue(AudioItem item) {
        if (item == null) {
            return;
        }
        queue.add(item);
        if (currIndex == -1) {
            currIndex = 0;
        }
    }

    // When user clicks on a track outside playlist, insert next element in queue
    public void enqueueNext(AudioItem item) {
        if (item == null) {
            return;
        }

        if (currIndex >= 0) {
            queue.add(currIndex + 1, item);
        }
        else {
            enqueue(item);
        }
    }

    public AudioItem getNext() {
        if (queue.isEmpty() || currIndex >= queue.size() - 1) {
            return null;
        }
        return queue.get(++currIndex);
    }

    // Get info on current track to display on GUI
    public AudioItem getCurrent() {
        if (queue.isEmpty() || currIndex >= queue.size()) {
            return null;
        }
        return queue.get(currIndex);
    }

    public AudioItem getPrevious() {
        if (queue.isEmpty() || currIndex <= 0) {
            return null;
        }
        return queue.get(--currIndex);
    }

    public AudioItem getIndex(int index) {
        if (index < 0 || index >= queue.size()) {
            return null;
        }
        currIndex = index;
        return queue.get(index);
    }

    public void clear() {
        queue.clear();
        currIndex = -1;
    }

    public void buildQueueFromList(List<AudioItem> itemList) {
        if (itemList == null || itemList.isEmpty()) {
            return;
        }
        clear();
        queue.addAll(itemList);
        currIndex = 0;
    }

    public List<AudioItem> getQueue() {
        // only shows audio tracks from the current pointer to end of queue
        return queue.subList(currIndex, queue.size());
    }
}
