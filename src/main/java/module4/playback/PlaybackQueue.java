package module4.playback;

import java.util.ArrayList;
import java.util.List;
import module1.audioModel.AudioItem;

/**
 * Manages the playback queue and scheduling of audio tracks.
 * <p>
 *     This class utilizes an {@link ArrayList} to handle track insertions and
 *     random access when users skip to arbitrary index positions. It maintains an internal
 *     pointer {@code currIndex} to track the queue state.
 * </p>
 */

public class PlaybackQueue {
    protected List<AudioItem> queue;
    protected int currIndex;

    /**
     * Constructs an empty playback queue.
     * The internal pointer is initialized to -1 to denote an empty state.
     */
    public PlaybackQueue() {
        queue = new ArrayList<>();
        currIndex = -1; // When starting, since theres no element yet, currIndex is -1
    }

    /* Queue Appending */

    /**
     * Appends an audio track to the very end of the queue.
     * If the queue is empty, the pointer initializes to 0.
     *
     * @param item The {@link AudioItem} to append.
     */
    public void enqueue(AudioItem item) {
        if (item == null) {
            return;
        }
        queue.add(item);
        if (currIndex == -1) {
            currIndex = 0;
        }
    }

    /**
     * Injects a track directly after the current playing track, ignoring the queue after.
     *
     * @param item The {@link AudioItem} to insert next.
     */
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

    /**
     * Replaces the entire queue with a new list of tracks and resets the pointer to 0.
     * Typical for playing from a playlist.
     *
     * @param itemList A {@link List} of new {@link AudioItem}s to be inserted in queue.
     */
    public void buildQueueFromList(List<AudioItem> itemList) {
        if (itemList == null || itemList.isEmpty()) {
            return;
        }
        queue.addAll(itemList);
        currIndex = 0;
    }

    /* Queue Navigation */

    /**
     * Decreases the queue pointer and returns the previous track
     *
     * @return The previous {@link AudioItem}, or {@code null} if the end of the queue is reached.
     */
    public AudioItem getPrevious() {
        if (queue.isEmpty() || currIndex <= 0) {
            return null;
        }
        return queue.get(--currIndex);
    }

    /**
     * Advances the queue pointer and returns the next track
     *
     * @return The next {@link AudioItem}, or {@code null} if the end of the queue is reached.
     */
    public AudioItem getNext() {
        if (queue.isEmpty() || currIndex >= queue.size() - 1) {
            currIndex = queue.size();
            return null;
        }
        return queue.get(++currIndex);
    }

    /**
     * Jumps the queue pointer directly to a specific track index.
     *
     * @param index The target integer index within the array bounds.
     * @return The {@link AudioItem} at the target index, or {@code null} if out of bounds.
     */
    public AudioItem getIndex(int index) {
        if (index < 0 || index >= queue.size()) {
            return null;
        }
        currIndex = index;
        return queue.get(index);
    }

    /* Clearing methods */



    /**
     * Clears all tracks from the queue and completely resets the index pointer.
     */
    public void clear() {
        queue.clear();
        currIndex = -1;
    }

    /**
     * Truncates the queue by removing all tracks scheduled after the currently playing track.
     * The history (previous tracks) and current track remain unaffected.
     */
    public void clearUpcoming() {
        if (queue.isEmpty() || currIndex < 0) {
            return;
        }
        if (queue.size() > currIndex + 1) {
            queue.subList(currIndex + 1, queue.size()).clear();
        }
    }

    /* Getters */

    /**
     * Retrieves the currently active track based on the internal pointer.
     *
     * @return The current {@link AudioItem}, or {@code null} if the queue is finished or empty.
     */
    public AudioItem getCurrent() {
        if (queue.isEmpty() || currIndex >= queue.size()) {
            return null;
        }
        return queue.get(currIndex);
    }

    /** Returns the current integer index of the playing track. */
    public int getCurrIndex() {
        return currIndex;
    }

    /** Returns the total size of the active queue. */
    public int getQueueSize() {
        return queue.size();
    }

    /**
     * Retrieves the currently active track based on the internal pointer.
     *
     * @return The current {@link AudioItem}, or {@code null} if the queue is finished or empty.
     */
    public List<AudioItem> getQueue() {
        if (currIndex + 1 >= queue.size()) { return new ArrayList<>(); }

        // Only shows audio tracks from the current pointer to end of queue
        return new ArrayList<>(queue.subList(currIndex + 1, queue.size()));
    }
}
