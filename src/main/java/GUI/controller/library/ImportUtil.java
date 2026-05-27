package GUI.controller.library;

import GUI.controller.util.dialog.TrackEditDialog;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import module1.audioModel.AudioItem;
import module3.storage.AudioStorage;
import module5.util.MetadataExtractor;

import java.io.File;
import java.util.List;

public class ImportUtil {
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
