package module5.util;
import interfaces.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
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
                "dd/M/yyyy"
        };

        for (String format : possibleFormats) {
            try {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format);
                return LocalDate.parse(date, formatter).toEpochDay();
            } catch (DateTimeParseException e) {
            }
        }

        throw new IllegalArgumentException();
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

    public static <T extends Comparable<T>> List<T> sort(List<T> data, String sortCriteria) {
        Collections.sort(data);
        return data;
    }


}
