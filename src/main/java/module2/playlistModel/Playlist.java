package module2.playlistModel;
import java.util.UUID;
import java.util.List;
import java.util.ArrayList;
import module1.audioModel.AudioItem;
import interfaces.Filterable;
import interfaces.Searchable;
import module5.util.LibraryLogic;

/**
 * Represents a user-curated collection of audio tracks, or a playlist.
 * <p>
 *     This class contains the metadata of a playlist (UUID, title, description, date created),
 *     along with an {@code ArrayList<String>} which holds pointers (trackIDs) to AudioItems.
 * </p>
 * <p>
 *     This class implements the interfaces {@code Filterable}, and {@code Searchable},
 *  *     which require implementation of methods {@code passesFilter}, and {@code matchesQuery}, respectively.
 * </p>
 */

public class Playlist implements Searchable, Filterable {
    protected String playlistID;
    protected String title;
    protected String description;
    protected String dateCreated;
    protected List<String> trackIDs;

    /**
     * Constructs a new {@code Playlist} with the specified metadata.
     * An unique playlist ID and an empty track list are automatically generated upon instantiation/
     *
     * @param title The display name of the playlist.
     * @param description A brief user-provided description of the playlist.
     * @param dateCreated The creation date of the playlist (e.g., "dd/MM/yyyy").
     */
    public Playlist(String title, String description,  String dateCreated) {
        this.playlistID = UUID.randomUUID().toString();
        this.trackIDs = new ArrayList<>();
        this.title = title;
        this.description = description;
        this.dateCreated = dateCreated;
    }

    /* Track management methods */

    /**
     * Adds a track to the playlist using its unique ID.
     * Prevents duplicate entries; if the track ID is already in the playlist, the addition is ignored.
     *
     * @param trackID The unique UUID string of the track to add.
     */
    public void addTrack(String trackID) {
        if (trackID != null && !trackIDs.contains(trackID)) {
            trackIDs.add(trackID);
        }
    }

    /**
     * Adds a track to the playlist using an {@code AudioItem} object.
     * Extracts the track ID from the object and delegates the operation to {@link #addTrack(String)}.
     *
     * @param item The {@link AudioItem} to add to the playlist.
     */
    public void addTrack(AudioItem item) {
        if (item != null) {
            addTrack(item.getTrackID());
        }
    }

    /**
     * Removes a track from the playlist using its unique ID.
     *
     * @param trackID The unique UUID string of the track to remove.
     */
    public void removeTrack(String trackID) {
        if (trackID != null) {
            trackIDs.remove(trackID);
        }
    }

    /**
     * Removes a track from the playlist using an {@code AudioItem} object.
     * Extracts the track ID from the object and delegates the operation to {@link #removeTrack(String)}.
     *
     * @param item The {@link AudioItem} to remove from the playlist.
     */
    public void removeTrack(AudioItem item) {
        if (item != null) {
            removeTrack(item.getTrackID());
        }
    }

    /* Interface Implementations */

    /**
     * Evaluates whether this playlist matches a given search query.
     * This is an abstract method of the {@code Searchable} interface.
     * A match is successful if the query is a case-insensitive substring of
     * the playlist's title.
     *
     * @param query The search string provided by the user.
     * @return {@code true} if the playlist title matches the query, or if the query is null; {@code false} otherwise.
     */
    @Override
    public boolean matchesQuery(String query) {
        if (query == null)
            return true;

        String lowerCaseQuery = query.toLowerCase();
        return title.toLowerCase().contains(lowerCaseQuery);
    }

    /**
     * Determines if the playlist passes a logical filter, based on the given category.
     * This is an abstract method of the {@code Filterable} interface.
     * Supports filtering by "genre" (exact string match) and "date" (numerical comparison).
     *
     * @param category The metadata category to filter by (e.g., "genre", "date")
     * @param operator The logical operator for comparison
     * @param value The target value to compare against the playlist's metadata
     * @return {@code true} if the playlist satisfies the filter condition; {@code false} otherwise.
     */
    @Override
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

    /* Getters */

    /** Returns the underlying list of track IDs contained in this playlist. */
    public List<String> getTracks() { return trackIDs; }

    /** Returns the unique UUID of the playlist. */
    public String getPlaylistID() { return playlistID; }

    /** Returns the display title of the playlist. */
    public String getTitle() { return title; }

    /** Returns the user-provided description of the playlist. */
    public String getDescription() { return description; }

    /** Returns the creation date of the playlist. */
    public String getDateCreated() { return dateCreated; }

    /* Setters */

    /** Updates the display title of the playlist. */
    public void setTitle(String title) { this.title = title; }

    /** Updates the user-provided description of the playlist. */
    public void setDescription(String description) { this.description = description; }

}