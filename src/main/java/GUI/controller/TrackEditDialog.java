package GUI.controller;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import module1.audioModel.AudioItem;

public class TrackEditDialog extends Dialog<AudioItem> {

    public TrackEditDialog(AudioItem draftItem) {
        setTitle("Review Track Info");
        setHeaderText("Confirm or edit the metadata before importing.");

        // 1. Setup the UI Grid
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        // 2. Create the TextFields and pre-fill them with the draft data
        TextField titleField = new TextField(draftItem.getTitle());
        TextField artistField = new TextField(draftItem.getAuthor());
        TextField genreField = new TextField(draftItem.getGenre() != null ? draftItem.getGenre() : "Unknown");
        TextField releaseDateField = new TextField(draftItem.getReleaseDate() != null ? draftItem.getReleaseDate() : "1970");

        // 3. Add labels and fields to the Grid
        grid.add(new Label("Title:"), 0, 0);
        grid.add(titleField, 1, 0);
        grid.add(new Label("Artist:"), 0, 1);
        grid.add(artistField, 1, 1);
        grid.add(new Label("Genre:"), 0, 2);
        grid.add(genreField, 1, 2);
        grid.add(new Label("Release Date:"), 0, 3);
        grid.add(releaseDateField, 1, 3);

        getDialogPane().setContent(grid);

        // 4. Add standard Save and Cancel buttons
        ButtonType saveButtonType = new ButtonType("Import", ButtonBar.ButtonData.OK_DONE);
        getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        // 5. Convert the user's input back into an AudioItem when they click Save
        setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                // Update the draft item with the user's new text
                draftItem.setTitle(titleField.getText().trim());
                draftItem.setAuthor(artistField.getText().trim());
                draftItem.setGenre(genreField.getText().trim());
                draftItem.setReleaseDate(releaseDateField.getText().trim());
                return draftItem; // Send back to controller
            }
            return null; // If clicked cancel
        });
    }
}
