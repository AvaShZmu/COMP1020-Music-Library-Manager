package GUI.controller.library;

import GUI.controller.util.dialog.FilterDialog;
import GUI.controller.util.dialog.TrackEditDialog;
import GUI.controller.main.MainController;
import GUI.controller.playback.PlaybackBarController;
import javafx.animation.FadeTransition;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.stage.Window;
import module1.audioModel.AudioItem;

import java.net.URL;
import java.util.*;

import module2.playlistModel.Playlist;
import module3.storage.AudioStorage;
import module3.storage.PlaylistStorage;
import module5.util.LibraryLogic;

public class LibraryController implements Initializable, CardBuildUtil.CardInteractionListener, FilterDialog.FilterListener {

    @FXML private VBox libraryView;
    @FXML private FlowPane cardGrid;
    @FXML private Label trackCountLabel;
    @FXML private ComboBox<String> sortComboBox;
    @FXML private Button btnFilter;
    @FXML private Button btnClearFilter;

    private final List<AudioItem> masterList = new ArrayList<>();

    // Currently selected card and its data
    private AudioItem selectedItem = null;
    private VBox selectedCard = null;

    // Currently playing card — for green border highlight
    private VBox playingCard  = null;
    private String playingID = null;

    // Reference back to parent so double-click can update the bottom bar
    private MainController mainController;
    private AudioStorage audioStorage;
    private PlaylistStorage playlistStorage;
    private PlaybackBarController playbackBarController;

    // Search
    private String currentQuery = "";

    // Filter — matches passesFilter(category, operator, value) signature
    private String filterCategory = null;  // "genre" or "releaseDate"
    private String filterOperator = null;  // "=" for genre, ">=" "<=" "=" for date
    private String filterValue    = null;  // the actual value

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        libraryView.setOnMouseClicked(event -> {
            if (selectedCard != null) {

                selectedCard.getStyleClass().remove("selected");

                selectedCard = null;
                selectedItem = null;

            }
        });

        sortComboBox.getItems().addAll(LibraryLogic.SORT_LABELS);
        sortComboBox.setValue(LibraryLogic.SORT_LABELS.get(0));  // default selection
    }

    // Set up the controllers
    public void setMainController(MainController mc) {
        this.mainController = mc;
    }

    public void setAudioStorage(AudioStorage as) {
        this.audioStorage = as;

        // Populate the library as soon as MainController passes the storage down
        if (this.audioStorage != null) {
            masterList.clear();
            masterList.addAll(this.audioStorage.getAllItems());
            refreshLibrary();
        }
    }

    public void setPlaylistStorage(PlaylistStorage ps){
        this.playlistStorage = ps;
    }

    public void setPlaybackBarController(PlaybackBarController pc) { this.playbackBarController = pc; }

    public void syncActiveCardButton(String textState) {
        if (playingCard != null) {
            Button btn = (Button) playingCard.getProperties().get("playButton");
            if (btn != null) {
                btn.setText(textState);
            }
        }
    }

    @FXML
    private void handleImport() {
        Window window = cardGrid.getScene().getWindow();
        ImportUtil.handleImport(window, masterList, audioStorage, this::refreshLibrary);
    }

    @FXML
    private void handleFilter() {
        FilterDialog.showFilterDialog(masterList, this);
    }

    @FXML
    private void handleClearFilter() {
        filterCategory = null; filterOperator = null; filterValue = null;
        btnClearFilter.setVisible(false);
        btnClearFilter.setManaged(false);
        btnFilter.getStyleClass().remove("active");
        refreshLibrary();
    }

    @FXML
    private void handleSort() {
        refreshLibrary();
    }

    public void applySearch(String query) {
        currentQuery = query;
        refreshLibrary();
    }

    private void refreshLibrary() {
        // start from full master list
        List<AudioItem> result = new ArrayList<>(masterList);

        // apply search using matchesQuery()
        if(!currentQuery.isBlank()){
            result = LibraryLogic.search(masterList, currentQuery);
        }

        // apply filter using passesFilter()
        if(filterCategory!=null){
            result = LibraryLogic.filter(result, filterCategory, filterOperator, filterValue);
        }

        result = LibraryLogic.sort(result, sortComboBox.getValue());

        // rebuild grid
        cardGrid.getChildren().clear();
        for (AudioItem item : result) {
            cardGrid.getChildren().add(CardBuildUtil.buildCard(item, playlistStorage, playbackBarController, playingCard, this));
        }
        updateTrackCount(result.size());

        if (playingID != null) {
            playingCard = null;
            highlightPlayingCard(playingID);
        }
    }

    public void highlightPlayingCard(String trackID) {
        // Clean up prev track
        if (playingCard != null) {
            playingCard.getStyleClass().remove("playing");

            Button oldBtn = (Button) playingCard.getProperties().get("playButton");
            FadeTransition oldFadeIn = (FadeTransition) playingCard.getProperties().get("fadeIn");
            FadeTransition oldFadeOut = (FadeTransition) playingCard.getProperties().get("fadeOut");

            if (oldBtn != null) {
                oldBtn.setText("▶");

                if (!playingCard.isHover()) {
                    if (oldFadeIn != null) oldFadeIn.stop();
                    if (oldFadeOut != null) oldFadeOut.playFromStart();
                }
            }
        }

        playingID = trackID;
        playingCard = null;
        if (trackID == null) {
            return;
        }

        // Set up new card
        cardGrid.getChildren().stream()
                .filter(node -> trackID.equals(node.getUserData()))
                .findFirst()
                .ifPresent(node -> {
                    node.getStyleClass().add("playing");
                    playingCard = (VBox) node;

                    Button newBtn = (Button) playingCard.getProperties().get("playButton");
                    FadeTransition newFadeIn = (FadeTransition) playingCard.getProperties().get("fadeIn");
                    FadeTransition newFadeOut = (FadeTransition) playingCard.getProperties().get("fadeOut");
                    if (newBtn != null) {
                        newBtn.setText("⏸");

                        if (newFadeOut != null) newFadeOut.stop();
                        newBtn.setVisible(true);
                        if (newFadeIn != null) newFadeIn.playFromStart();
                    }
                });
    }

    private void updateTrackCount(int displayed) {
        int total = masterList.size();
        if (displayed == total) {
            trackCountLabel.setText(total + (total == 1 ? " song" : " songs"));
        } else {
            trackCountLabel.setText(displayed + " of " + total + " songs");
        }
    }

    // Listeners
    @Override
    public void onFilterApplied(String category, String operator, String value, String clearLabelText) {
        this.filterCategory = category;
        this.filterOperator = operator;
        this.filterValue = value;

        btnClearFilter.setText("✕ " + clearLabelText);
        btnClearFilter.setVisible(true);
        btnClearFilter.setManaged(true);
        btnFilter.getStyleClass().add("active");

        refreshLibrary();
    }

    @Override
    public void onPlayClicked(AudioItem item, VBox card) {
        if (playingCard == card) {
            if (playbackBarController != null) playbackBarController.handlePlayPause();
        } else {
            if (playbackBarController != null) playbackBarController.playTrack(item);
        }
    }

    @Override
    public void onCardSelected(AudioItem item, VBox card, boolean doubleClicked) {
        if (selectedCard != null) {
            selectedCard.getStyleClass().remove("selected");
        }
        card.getStyleClass().add("selected");
        selectedCard = card;
        selectedItem = item;

        if (doubleClicked && playbackBarController != null) {
            playbackBarController.playTrack(item);
        }
    }

    @Override
    public void onRemoveRequested(AudioItem item) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Remove Track");
        confirm.setHeaderText("Remove \"" + item.getTitle() + "\"?");
        confirm.setContentText(
                "This removes the track from your library. " +
                        "The original file will not be deleted."
        );

        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                masterList.remove(item);
                audioStorage.removeItem(item);
                for(Playlist pl : playlistStorage.getAllPlaylists()){
                    pl.removeTrack(item);
                }
                refreshLibrary();
            }
        });
    }

    @Override
    public void onAddToQueue(AudioItem item) {
        if (playbackBarController != null) {
            playbackBarController.addToQueue(item);
        }
    }

    @Override
    public void onEditRequested(AudioItem item) {
        TrackEditDialog dialog = new TrackEditDialog(item, TrackEditDialog.Mode.EDIT);
        dialog.showAndWait().ifPresent(updatedItem -> {
            int index = masterList.indexOf(item);
            if (index != -1) {
                masterList.set(index, item);
            }

            audioStorage.updateItem(updatedItem);
            refreshLibrary();
        });
    }
}
