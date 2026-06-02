package GUI.controller.playlist;

import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import module1.audioModel.AudioItem;
import module2.playlistModel.Playlist;
import static module5.util.LibraryLogic.formatTime;

/**
 * A UI utility class which is responsible for constructing and binding data to playlist track tables.
 * <p>
 *     This class isolates the verbose JavaFX {@link TableView} configuration logic,
 *     managing responsive column widths, data formatting, and row-level mouse interaction (playing music).
 * </p>
 */

public class TableBuildUtil {

    /**
     * Defines the callback for handling user interaction with table rows.
     */
    public interface TableInteractionListener {
        void onTrackDoubleClicked(AudioItem item, int index);
        void onRemoveRequested(AudioItem item);
    }

    /**
     * Configures the layout, data bindings, and event handlers for the playlist table.
     *
     * @param table The root {@link TableView} to configure.
     * @param colNumber The column displaying the track's index number.
     * @param colTitle The column displaying the track's title.
     * @param colArtist The column displaying the track's artist.
     * @param colGenre The column displaying the track's genre.
     * @param colDuration The column displaying the track's formatted duration.
     * @param playlist The {@link Playlist} object currently being viewed.
     * @param listener The {@link TableInteractionListener} to handle row click events.
     */
    public static void setupTable(TableView<AudioItem> table,
                                  TableColumn<AudioItem, Integer> colNumber,
                                  TableColumn<AudioItem, String> colTitle,
                                  TableColumn<AudioItem, String> colArtist,
                                  TableColumn<AudioItem, String> colGenre,
                                  TableColumn<AudioItem, String> colDuration,
                                  Playlist playlist,
                                  TableInteractionListener listener) {

        /* Responsive column width */

        table.widthProperty().addListener((obs, oldVal, newVal) -> {
            double width = newVal.doubleValue();
            colNumber.setPrefWidth(width * 0.08);
            colTitle.setPrefWidth(width * 0.28);
            colArtist.setPrefWidth(width * 0.28);
            colGenre.setPrefWidth(width * 0.20);
            colDuration.setPrefWidth(width * 0.16);
        });

        /* Data factory binding */

        colNumber.setCellValueFactory(data -> new SimpleIntegerProperty(table.getItems().indexOf(data.getValue()) + 1).asObject());
        colTitle.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getTitle()));
        colArtist.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getAuthor()));
        colGenre.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getGenre()));

        colDuration.setCellValueFactory(data -> {
            int totalSeconds = data.getValue().getDuration();
            return new SimpleStringProperty(formatTime(totalSeconds));
        });

        /* Row interaction and Context Menu */

        table.setRowFactory(tv -> {
            TableRow<AudioItem> row = new TableRow<>();

            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && event.getButton() == MouseButton.PRIMARY && !row.isEmpty()) {
                    listener.onTrackDoubleClicked(row.getItem(), row.getIndex());
                }
            });

            ContextMenu menu = new ContextMenu();
            menu.getStyleClass().add("right-click-menu");
            MenuItem remove = new MenuItem("Remove from playlist");
            remove.setOnAction(event -> listener.onRemoveRequested(row.getItem()));
            menu.getItems().add(remove);

            row.contextMenuProperty().bind(
                    Bindings.when(row.emptyProperty()).then((ContextMenu) null).otherwise(menu)
            );
            return row;
        });
    }
}
