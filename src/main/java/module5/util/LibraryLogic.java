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

public class LibraryLogic {
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

    public static String formatTime(int totalSeconds) {
        int minutes = totalSeconds / 60;
        int seconds = totalSeconds % 60;
        return String.format("%d:%02d", minutes, seconds);
    }

    public static boolean compareNumbers(long n1, String operator, long n2) {
        return switch (operator) {
            case "<" -> n1 < n2;
            case "<=" -> n1 <= n2;
            case ">" -> n1 > n2;
            case ">=" -> n1 >= n2;
            default -> n1 == n2;
        };
    }

    public static <T extends Searchable> List<T> search(List<T> data, String query) {
        List<T> result = new ArrayList<>();
        for (T item : data) {
            if (item.matchesQuery(query)) {
                result.add(item);
            }
        }
        return result;
    }

    public static <T extends Filterable> List<T> filter(List<T> data, String filterType, String operator, String targetValue) {
        List<T> result = new ArrayList<>();
        for (T item : data) {
            if (item.passesFilter(filterType, operator, targetValue)) {
                result.add(item);
            }
        }
        return result;
    }

    public static final List<String> SORT_LABELS = List.of(
            "Title A → Z",
            "Title Z → A",
            "Newest first",
            "Oldest first"
    );

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
