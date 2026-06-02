package module3.storage;

import module1.audioModel.AudioItem;
import java.util.Collection;
import java.util.HashMap;

/**
 * Provides an in-memory repository for managing the library of audio tracks.
 * <p>
 *     Uses a {@link HashMap} to store {@link AudioItem} objects,
 *     mapping each track's UUID to its instance.
 *     This helps ensure O(1) average lookups for track retrieval during playback.
 * </p>
 */

public class AudioStorage {
    protected HashMap<String, AudioItem> audioLibrary;

    /**
     * Constructs a new, empty {@code AudioStorage} repository.
     */
    public AudioStorage() {
        audioLibrary = new HashMap<>();
    }

    /**
     * Adds a new audio track to the library.
     *
     * @param item The {@link AudioItem} to insert into the repository.
     */
    public void addItem(AudioItem item) {
        audioLibrary.put(item.getTrackID(), item);
    }

    /**
     * Removes an existing audio track from the library.
     *
     * @param item The {@link AudioItem} to remove.
     */
    public void removeItem(AudioItem item) {
        audioLibrary.remove(item.getTrackID());
    }

    /**
     * Updates the stored state of an existing audio track.
     * If the track does not exist, it will be added to the library.
     *
     * @param updatedItem The modified {@link AudioItem} to save.
     */
    public void updateItem(AudioItem updatedItem) {
        audioLibrary.put(updatedItem.getTrackID(), updatedItem);
    }

    /**
     * Retrieves an audio track from the library using its unique ID.
     *
     * @param trackID The UUID string of the desired track.
     * @return The corresponding {@link AudioItem}, or {@code null} if not found.
     */
    public AudioItem getItem(String trackID) {
        return audioLibrary.get(trackID);
    }

    /**
     * Retrieves all audio tracks currently stored in the repository.
     *
     * @return A {@link Collection} of all stored {@link AudioItem} objects.
     */
    public Collection<AudioItem> getAllItems() {
        return audioLibrary.values();
    }

    /**
     * Performs a bulk import of audio tracks into the repository.
     *
     * @param items A {@link Collection} of {@link AudioItem} objects to import.
     */
    public void importAudio(Collection<AudioItem> items) {
        for (AudioItem item : items) {
            audioLibrary.put(item.getTrackID(), item);
        }
    }
}
