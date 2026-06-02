package GUI.controller.util.dialog;

import javafx.scene.control.Alert;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.TextInputDialog;
import module1.audioModel.AudioItem;
import java.util.List;
import java.util.stream.Collectors;

/**
 * A utility class responsible for creating the filter dialog.
 * <p>
 * This class abstracts the creation of JavaFX {@link ChoiceDialog} and {@link TextInputDialog}
 * flows, prompting the user for filter categories, logic operators, and target values
 * before returning the resulting parameters via a callback interface.
 * </p>
 */

public class FilterDialog {
    /**
     * Defines the callback for applying a fully constructed filter query.
     */
    public interface FilterListener {
        /**
         * Triggered when the user successfully completes the filter dialog sequence.
         *
         * @param category       The metadata category (e.g., "genre", "date").
         * @param operator       The logical comparison operator (e.g., "=", ">=").
         * @param value          The target string value to filter against.
         * @param clearLabelText Formatted text to display on the "Clear Filter" UI button.
         */
        void onFilterApplied(String category, String operator, String value, String clearLabelText);
    }

    /* Dialog launchers */

    /**
     * Initiates the master filter dialog, asking the user which category they wish to filter by.
     * Routes to the specific sub-dialog based on the user's selection.
     *
     * @param masterList The active list of items (used to dynamically populate genre options).
     * @param listener   The callback to execute when the sequence finishes.
     */
    public static void showFilterDialog(List<AudioItem> masterList, FilterListener listener) {
        ChoiceDialog<String> categoryDialog = new ChoiceDialog<>("Genre", "Genre", "Date");
        categoryDialog.setTitle("Filter");
        categoryDialog.setHeaderText("Filter by:");

        categoryDialog.showAndWait().ifPresent(category -> {
            if(category.equals("Genre")) {
                showGenreFilter(masterList, listener);
            } else {
                showDateFilter(listener);
            }
        });
    }

    /* Genre and Date filter dialogs */

    /**
     * Presents a dropdown dialog with unique genres
     * present in the current master list.
     *
     * @param masterList The active list of tracks to extract genres from.
     * @param listener   The callback to execute with the selected genre.
     */
    private static void showGenreFilter(List<AudioItem> masterList, FilterListener listener) {
        List<String> genres = masterList.stream()
                .map(AudioItem::getGenre)
                .filter(g -> g != null && !g.isBlank())
                .distinct()
                .sorted()
                .collect(Collectors.toList());

        if (genres.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION, "Import tracks with genre metadata to use this filter.");
            alert.setHeaderText("No genre data available");
            alert.showAndWait();
            return;
        }

        ChoiceDialog<String> genreDialog = new ChoiceDialog<>(genres.get(0), genres);
        genreDialog.setTitle("Filter by Genre");
        genreDialog.setHeaderText("Select genre:");
        genreDialog.showAndWait().ifPresent(genre -> listener.onFilterApplied("genre", "=", genre, "Genre: " + genre));
    }

    /**
     * Presents a two-step dialog sequence allowing the user to select a date comparison
     * operator (e.g., "From year onwards") and input a target year.
     *
     * @param listener The callback to execute with the formulated date constraints.
     */
    private static void showDateFilter(FilterListener listener) {
        ChoiceDialog<String> opDialog = new ChoiceDialog<>("Exactly in year", "Exactly in year", "From year onwards", "Up to year");
        opDialog.setTitle("Filter by Date");
        opDialog.setHeaderText("Show tracks released:");

        opDialog.showAndWait().ifPresent(operatorLabel -> {
            String operator = switch (operatorLabel) {
                case "From year onwards" -> ">=";
                case "Up to year"        -> "<=";
                default                  -> "=";
            };

            TextInputDialog yearDialog = new TextInputDialog("2020");
            yearDialog.setHeaderText("Enter year (" + operatorLabel + "):");
            yearDialog.showAndWait().ifPresent(year -> {
                if (year.isBlank()) return;
                String label = switch (operator) {
                    case "="  -> "Year: " + year;
                    case ">=" -> "From: " + year;
                    case "<=" -> "Until: " + year;
                    default   -> "Date filter";
                };
                listener.onFilterApplied("date", operator, year.trim(), label);
            });
        });
    }
}