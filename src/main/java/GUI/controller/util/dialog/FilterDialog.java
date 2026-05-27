package GUI.controller.util.dialog;

import javafx.scene.control.Alert;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.TextInputDialog;
import module1.audioModel.AudioItem;

import java.util.List;
import java.util.stream.Collectors;

public class FilterDialog {

    public interface FilterListener {
        void onFilterApplied(String category, String operator, String value, String clearLabelText);
    }

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