package GUI.controller.playlist;

import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.control.*;

import javafx.scene.input.MouseButton;
import module1.audioModel.AudioItem;
import module2.playlistModel.Playlist;
import static module5.util.LibraryLogic.formatTime;

public class TableBuildUtil {
    public interface TableInteractionListener {
        void onTrackDoubleClicked(AudioItem item, int index);
        void onRemoveRequested(AudioItem item);
    }

    public static void setupTable(TableView<AudioItem> table,
                                  TableColumn<AudioItem, Integer> colNumber,
                                  TableColumn<AudioItem, String> colTitle,
                                  TableColumn<AudioItem, String> colArtist,
                                  TableColumn<AudioItem, String> colGenre,
                                  TableColumn<AudioItem, String> colDuration,
                                  Playlist playlist,
                                  TableInteractionListener listener) {


        // Responsive column width
        table.widthProperty().addListener((obs, oldVal, newVal) -> {
            double width = newVal.doubleValue();
            colNumber.setPrefWidth(width * 0.06);
            colTitle.setPrefWidth(width * 0.30);
            colArtist.setPrefWidth(width * 0.28);
            colGenre.setPrefWidth(width * 0.20);
            colDuration.setPrefWidth(width * 0.16);
        });

        // Data binding
        colNumber.setCellValueFactory(data -> new SimpleIntegerProperty(table.getItems().indexOf(data.getValue()) + 1).asObject());
        colTitle.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getTitle()));
        colArtist.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getAuthor()));
        colGenre.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getGenre()));

        colDuration.setCellValueFactory(data -> {
            int totalSeconds = data.getValue().getDuration();
            return new SimpleStringProperty(formatTime(totalSeconds));
        });

        // Row click, contextmenu
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
