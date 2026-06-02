package GUI.controller.library;

import GUI.controller.util.dialog.TrackEditDialog;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import module1.audioModel.AudioItem;
import module3.storage.AudioStorage;
import module5.util.MetadataExtractor;
import java.io.File;
import java.util.List;

/**
 * A UI utility class that manages importing audio files.
 * <p>
 *     This class abstracts the JavaFX {@link FileChooser} logic and delivers metadata extraction
 *     to the backend before prompting user to edit or finalize the new track.
 * </p>
 */

public class ImportUtil {

    /**
     * Opens a system file chooser to allow the user to select local audio files.
     * Extracts metadata from selected files and updates the library.
     *
     * @param window The parent JavaFX window.
     * @param masterList The active GUI list tracking all loaded library items.
     * @param audioStorage The core database repo to persist new tracks.
     * @param onImportSuccess A {@link Runnable} callback triggered to refresh UI after import.
     */
    public static void handleImport(Window window, List<AudioItem> masterList, AudioStorage audioStorage, Runnable onImportSuccess) {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Import Audio Files");
        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Audio Files", "*.mp3", "*.wav", "*.aac")
        );

        List<File> files = chooser.showOpenMultipleDialog(window);

        if (files != null && !files.isEmpty()) {
            if (files.size() == 1) {
                File singleFile = files.get(0);
                AudioItem draftItem = MetadataExtractor.extract(singleFile.getPath());

                TrackEditDialog dialog = new TrackEditDialog(draftItem, TrackEditDialog.Mode.IMPORT);
                dialog.showAndWait().ifPresent(finalizedItem -> {
                    masterList.add(finalizedItem);
                    audioStorage.addItem(finalizedItem);
                    onImportSuccess.run();
                });
            } else {
                for (File file : files) {
                    AudioItem item = MetadataExtractor.extract(file.getPath());
                    if (item != null) {
                        masterList.add(item);
                        audioStorage.addItem(item);
                    }
                }
                onImportSuccess.run();
            }
        }
    }
}
