package module1.audioModel;

import java.util.UUID;
import interfaces.Filterable;
import interfaces.Searchable;
import module5.util.LibraryLogic;

/**
 * Represents a single audio track within the music library.
 * <p>
 *     This class contains the necessary data for an audio file, including the following:
 *     trackID (generated for each item), title, author, release date, genre, duration, play count,
 *     and file location.
 * </p>
 * <p>
 *     This class implements the interfaces {@code Comparable}, {@code Filterable}, and {@code Searchable},
 *     which require implementation of methods {@code compareTo}, {@code passesFilter}, and {@code matchesQuery}, respectively.
 * </p>
 */

public class AudioItem implements Comparable<AudioItem>, Filterable, Searchable {
    protected String trackID, title, author, releaseDate, fileLocation, genre;
    protected int duration, playCount;

    /**
     * Constructs a new {@code AudioItem} with the specified metadata.
     * TrackIDs are automatically generated, and play count is initialized at 0.
     *
     * @param title The title of the given audio track.
     * @param author The author of the given audio track.
     * @param releaseDate The release date of the given audio track in string (ex. 24/01/2025)
     * @param duration The duration of the given audio track, in seconds.
     * @param genre The genre of the given audio track.
     * @param fileLocation The file location of the given audio track on the hardware.
     */
    public AudioItem(String title, String author, String releaseDate, int duration, String genre, String fileLocation) {
        this.trackID = UUID.randomUUID().toString(); // For saving etc.
        this.title = title;
        this.author = author;
        this.releaseDate = releaseDate;
        this.genre = genre;
        this.duration = duration;
        this.playCount = 0;
        this.fileLocation = fileLocation;
    }

    /* Interface Implementations */

    /**
     * Evaluates whether this track matches a given search query.
     * This is an abstract method of the {@code Searchable} interface.
     * A match is successful if the query is a case-insensitive substring of
     * either the track's title or author.
     *
     * @param query The search string provided by the user.
     * @return {@code true} if the track matches the query, or if the query is null; {@code false} otherwise.
     */
    @Override
    public boolean matchesQuery(String query) {
        if (query == null)
            return true;

        String lowerCaseQuery = query.toLowerCase();
        return title.toLowerCase().contains(lowerCaseQuery) ||
                author.toLowerCase().contains(lowerCaseQuery);
    }

    /**
     * Determines if the track passes a logical filter, based on the given category.
     * This is an abstract method of the {@code Filterable} interface.
     * Supports filtering by "genre" (exact string match) and "date" (numerical comparison).
     *
     * @param category The metadata category to filter by (e.g., "genre", "date")
     * @param operator The logical operator for comparison
     * @param value The target value to compare against the track's metadata
     * @return {@code true} if the track satisfies the filter condition; {@code false} otherwise.
     */
    @Override
    public boolean passesFilter(String category, String operator, String value) {
        if (category == null || operator == null || value == null)
            return false;

        switch (category) {
            case "genre":
                return genre.equalsIgnoreCase(value);

            case "date":
                long actualDate = LibraryLogic.dateToTime(this.releaseDate);
                long targetDate = LibraryLogic.dateToTime(value);
                return LibraryLogic.compareNumbers(actualDate, operator, targetDate);

            default:
                return false;
        }
    }

    /**
     * Compares this AudioItem with another for default sorting purposes.
     * This is an abstract method of the {@code Comparable} interface.
     * The natural ordering is defined alphabetically by the track title (case-insensitive).
     *
     * @param other The AudioItem object to be compared.
     * @return A negative integer, zero, or a positive integer, depending on the alphabetical order
     * of the compared titles.
     */
    @Override
    public int compareTo(AudioItem other) {
        return this.title.compareToIgnoreCase(other.title);
    }

    /**
     * Increments the total number of times this track has been played by one.
     */
    public void increasePlayCount() {
        this.playCount++;
    }

    /* Getters */

    /** Returns the unique UUID of the track. */
    public String getTrackID() { return trackID; }

    /** Returns the title of the track. */
    public String getTitle() { return title; }

    /** Returns the author of the track. */
    public String getAuthor() { return author; }

    /** Returns the release date of the track. */
    public String getReleaseDate() { return releaseDate; }

    /** Returns the duration of the track, in seconds. */
    public int getDuration() { return duration; }

    /** Returns the genre of the track. */
    public String getGenre() { return genre; }

    /** Returns the file location of the track. */
    public String getFileLocation() { return fileLocation; }

    /* Setters */

    /** Updates the title of the track. */
    public void setTitle(String title) { this.title = title; }

    /** Updates the author of the track. */
    public void setAuthor(String author) { this.author = author; }

    /** Updates the release date of the track. */
    public void setReleaseDate(String releaseDate) { this.releaseDate = releaseDate; }

    /** Updates the genre of the track. */
    public void setGenre(String genre) { this.genre = genre; }
}
