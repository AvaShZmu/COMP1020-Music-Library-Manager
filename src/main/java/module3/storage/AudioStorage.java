package module3.storage;
import module1.audioModel.AudioItem;

import java.util.Collection;
import java.util.HashMap;

public class AudioStorage {
    protected HashMap<String, AudioItem> audioLibrary;

    public AudioStorage() {
        audioLibrary = new HashMap<>();
    }

    public void addItem(AudioItem item) {
        audioLibrary.put(item.getTrackID(), item);
    }

    public void removeItem(AudioItem item) {
        audioLibrary.remove(item.getTrackID());
    }

    public AudioItem getItem(String trackID) {
        return audioLibrary.get(trackID);
    }

    public Collection<AudioItem> getAllItems() {
        return audioLibrary.values();
    }

    public void importAudio(Collection<AudioItem> items) {
        for (AudioItem item : items) {
            audioLibrary.put(item.getTrackID(), item);
        }
    }
}
