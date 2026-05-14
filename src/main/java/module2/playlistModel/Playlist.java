package module2.playlistModel;
import java.util.UUID;
import java.util.List;
import java.util.ArrayList;
import module1.audioModel.AudioItem;
import interfaces.Filterable;
import interfaces.Searchable;
import module5.util.LibraryLogic;


public class Playlist implements Searchable, Filterable, Comparable<Playlist> {
    protected String playlistID;
    protected String title;
    protected String description;
    protected String dateCreated;

    protected List<String> trackIDs;

    public Playlist(String title, String description,  String dateCreated) {
        this.playlistID = UUID.randomUUID().toString();
        this.trackIDs = new ArrayList<>();
        this.title = title;
        this.description = description;
        this.dateCreated = dateCreated;
    }

    // Adding items
    public void addTrack(String trackID) {
        if (trackID != null && !trackIDs.contains(trackID)) {
            trackIDs.add(trackID);
        }
    }

    public void addTrack(AudioItem item) {
        if (item != null) {
            addTrack(item.getTrackID());
        }
    }

    // Deleting items
    public void removeTrack(String trackID) {
        if (trackID != null) {
            trackIDs.remove(trackID);
        }
    }

    public void removeTrack(AudioItem item) {
        if (item != null) {
            removeTrack(item.getTrackID());
        }
    }

    // Move items
    public void moveTrack(int from, int to) {
        if (from < 0 || to < 0 || from >= trackIDs.size() || to >= trackIDs.size()) {
            return;
        }

        String trackID = trackIDs.remove(from);
        trackIDs.add(to, trackID);
    }

    // Interface methods:
    public boolean matchesQuery(String query) {
        if (query == null)
            return true;

        String lowerCaseQuery = query.toLowerCase();
        return title.toLowerCase().contains(lowerCaseQuery);
    }

    public boolean passesFilter(String category, String operator, String value) {
        if (category == null || operator == null || value == null)
            return false;

        if (category.equals("date")) {
            long actualDate = LibraryLogic.dateToTime(this.dateCreated);
            long targetDate = LibraryLogic.dateToTime(value);
            return LibraryLogic.compareNumbers(actualDate, operator, targetDate);
        }
        return false;
    }

    public int compareTo(Playlist other) {
        return this.title.compareToIgnoreCase(other.title);
    }

    // Getters
    public List<String> getTracks() { return trackIDs; }
    public String getPlaylistID() { return playlistID; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }

    // Setters
    public void setTitle(String title) { this.title = title; }
    public void setDescription(String description) { this.description = description; }
}