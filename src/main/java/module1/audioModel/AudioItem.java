package module1.audioModel;
import java.util.UUID;

import interfaces.Filterable;
import interfaces.Searchable;
import module5.util.LibraryLogic;

public class AudioItem implements Comparable<AudioItem>, Filterable, Searchable {
    protected String trackID, title, author, releaseDate, fileLocation, genre;
    protected int duration, playCount;

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

    public String getFileLocation() {
        return fileLocation;
    }

    public boolean matchesQuery(String query) {
        if (query == null)
            return true;

        String lowerCaseQuery = query.toLowerCase();
        return title.toLowerCase().contains(lowerCaseQuery) ||
                author.toLowerCase().contains(lowerCaseQuery);
    }

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

    // Default is alphabetical order
    public int compareTo(AudioItem other) {
        return this.title.compareToIgnoreCase(other.title);
    }

    public void increasePlayCount() {
        this.playCount++;
    }

    // Getters
    public String getTrackID() { return trackID;}
    public String getTitle() { return title; }
    public String getAuthor() { return author; }
    public String getReleaseDate() { return releaseDate; }
    public int getDuration() { return duration; }
    public String getGenre() { return genre; }

    // Setters
    public void setTitle(String title) { this.title = title; }
    public void setAuthor(String author) { this.author = author; }
    public void setReleaseDate(String releaseDate) { this.releaseDate = releaseDate; }
    public void setGenre(String genre) { this.genre = genre; }
}
