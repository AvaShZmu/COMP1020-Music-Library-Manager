package module5.util;

import interfaces.Filterable;
import interfaces.Searchable;
import module1.audioModel.AudioItem;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Comparator;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;

/**
 * A stateless utility class providing core algorithms for the music library.
 * <p>
 *     This class handles global operations such as string-to-date parsing,
 *     time formatting, mathematical comparisons, and list processing
 *     (searching, filtering, and sorting).
 * </p>
 */

public class LibraryLogic {
    /**
     * The default sorting labels for UI rendering.
     */
    public static final List<String> SORT_LABELS = List.of(
            "Title A → Z",
            "Title Z → A",
            "Newest first",
            "Oldest first"
    );

    /* Time formatting and conversion */

    /**
     * Parses various string formats into a standardized epoch day representation.
     * Supports isolated years (e.g., "2026") and standard day-month-year formats.
     * Currently supported formats:
     * {@code dd/MM/yyyy}, {@code yyyy/MM/dd}, {@code MM/dd/yyyy}, {@code dd/M/yyyy}, {@code yyyy-MM-dd}, {@code dd-MM-yyyy}
     *
     * @param date The string representation of the date to parse.
     * @return The number of days from the epoch (1970-01-01) to the parsed date.
     * @throws IllegalArgumentException if the date string does not match any known format.
     */
    public static long dateToTime(String date) {
        date = date.trim();

        // Regex for 4 digits (only year given)
        if (date.matches("^\\d{4}$")) {
            return LocalDate.of(Integer.parseInt(date), 1, 1).toEpochDay();
        }

        String[] possibleFormats = {
                "dd/MM/yyyy",
                "yyyy/MM/dd",
                "MM/dd/yyyy",
                "dd/M/yyyy",
                "yyyy-MM-dd",
                "dd-MM-yyyy"
        };

        for (String format : possibleFormats) {
            try {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format);
                return LocalDate.parse(date, formatter).toEpochDay();
            } catch (DateTimeParseException e) {
            }
        }
        System.out.println("Failed date: [" + date + "]");
        throw new IllegalArgumentException();
    }

    /**
     * Converts a raw total of seconds into a standard "MM:SS" time string.
     *
     * @param totalSeconds The total duration in seconds.
     * @return A formatted time string (e.g., "3:45").
     */
    public static String formatTime(int totalSeconds) {
        int minutes = totalSeconds / 60;
        int seconds = totalSeconds % 60;
        return String.format("%d:%02d", minutes, seconds);
    }

    /* Comparison and Logic */

    /**
     * Evaluates a mathematical comparison between two numbers based on a string operator.
     *
     * @param n1       The left-hand numerical operand.
     * @param operator The string representation of a logical operator.
     * @param n2       The right-hand numerical operand.
     * @return {@code true} if the logical evaluation is correct; {@code false} otherwise.
     */
    public static boolean compareNumbers(long n1, String operator, long n2) {
        return switch (operator) {
            case "<" -> n1 < n2;
            case "<=" -> n1 <= n2;
            case ">" -> n1 > n2;
            case ">=" -> n1 >= n2;
            default -> n1 == n2;
        };
    }

    /* Collection operations */

    /**
     * Performs a linear search on a collection of {@link Searchable} items.
     *
     * @param data  The list of items to search through.
     * @param query The search query string provided by the user.
     * @param <T>   The specific type of the objects in the list (must implement {@code Searchable}).
     * @return A new {@link List} containing only the items that match the query.
     */
    public static <T extends Searchable> List<T> search(List<T> data, String query) {
        List<T> result = new ArrayList<>();
        for (T item : data) {
            if (item.matchesQuery(query)) {
                result.add(item);
            }
        }
        return result;
    }

    /**
     * Scans and filters a collection of {@link Filterable} items.
     *
     * @param data        The list of items to filter.
     * @param filterType  The specific metadata category to filter by (e.g., "date", "genre").
     * @param operator    The logical comparison operator.
     * @param targetValue The target value to compare each item against.
     * @param <T>         The specific type of the objects in the list (must implement {@code Filterable}).
     * @return A new {@link List} containing only the items that pass the filter logic.
     */
    public static <T extends Filterable> List<T> filter(List<T> data, String filterType, String operator, String targetValue) {
        List<T> result = new ArrayList<>();
        for (T item : data) {
            if (item.passesFilter(filterType, operator, targetValue)) {
                result.add(item);
            }
        }
        return result;
    }

    /**
     * Sorts a given list of {@link AudioItem} objects based on a specified string criteria.
     * Operates using Java's built-in MergeSort algorithm.
     *
     * @param data         The list of audio items to be sorted.
     * @param sortCriteria The specific ordering instruction (e.g., "Newest first", "Title Z → A").
     * @return The sorted {@link List}.
     */
    public static List<AudioItem> sort(List<AudioItem> data, String sortCriteria) {
        if (data == null || data.isEmpty() ||  sortCriteria == null) return data;

        switch (sortCriteria) {
            // Sorting in library
            case "Title Z → A" -> data.sort((a, b) -> b.compareTo(a));
            case "Newest first" -> data.sort(Comparator.comparing(AudioItem::getReleaseDate).reversed());
            case "Oldest first" -> data.sort(Comparator.comparing(AudioItem::getReleaseDate));

            // Sorting in playlist
            case "Longest First" -> data.sort(Comparator.comparing(AudioItem::getDuration).reversed());
            case "Shortest First" -> data.sort(Comparator.comparing(AudioItem::getDuration));
            case "Custom Order" -> { /* do nothing */ }

            default -> Collections.sort(data);
        }
        return data;
    }
}
