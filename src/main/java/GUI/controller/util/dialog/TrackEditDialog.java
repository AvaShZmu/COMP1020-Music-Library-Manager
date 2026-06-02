package GUI.controller.util.dialog;

import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import module1.audioModel.AudioItem;

/**
 * A custom JavaFX {@link Dialog} used for viewing and modifying track metadata.
 * <p>
 *     This dialog dynamically adjusts its headers, button text, and intent based on the
 *     provided operational mode (Importing new files vs. Editing existing library files).
 *     Returns the modified {@link AudioItem} upon confirmation.
 * </p>
 */

public class TrackEditDialog extends Dialog<AudioItem> {
    /**
     * Defines the context in which this dialog is called
     */
    public enum Mode { IMPORT, EDIT }

    /**
     * Constructs a metadata editing dialog.
     *
     * @param draftItem The initial {@link AudioItem} data to populate the text fields.
     * @param mode      The {@link Mode} determining the dialog's phrasing and button labels.
     */
    public TrackEditDialog(AudioItem draftItem, Mode mode) {
        if (mode == Mode.IMPORT) {
            setTitle("Review Track Info");
            setHeaderText("Confirm or edit the metadata before importing.");
        } else {
            setTitle("Edit Track Info");
            setHeaderText("Update the metadata for this track.");
        }

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField titleField = new TextField(draftItem.getTitle());
        TextField artistField = new TextField(draftItem.getAuthor());
        TextField genreField = new TextField(draftItem.getGenre() != null ? draftItem.getGenre() : "Unknown");
        TextField releaseDateField = new TextField(draftItem.getReleaseDate() != null ? draftItem.getReleaseDate() : "1970");

        grid.add(new Label("Title:"), 0, 0);
        grid.add(titleField, 1, 0);
        grid.add(new Label("Artist:"), 0, 1);
        grid.add(artistField, 1, 1);
        grid.add(new Label("Genre:"), 0, 2);
        grid.add(genreField, 1, 2);
        grid.add(new Label("Release Date:"), 0, 3);
        grid.add(releaseDateField, 1, 3);

        getDialogPane().setContent(grid);

        // Change text based on mode
        String buttonText = (mode == Mode.IMPORT) ? "Import" : "Save";
        ButtonType actionButtonType = new ButtonType(buttonText, ButtonBar.ButtonData.OK_DONE);

        getDialogPane().getButtonTypes().addAll(actionButtonType, ButtonType.CANCEL);

        setResultConverter(dialogButton -> {
            if (dialogButton == actionButtonType) {
                draftItem.setTitle(titleField.getText().trim());
                draftItem.setAuthor(artistField.getText().trim());
                draftItem.setGenre(genreField.getText().trim());
                draftItem.setReleaseDate(releaseDateField.getText().trim());
                return draftItem;
            }
            return null;
        });
    }
}