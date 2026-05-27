package GUI.controller.library;

import GUI.controller.playback.PlaybackBarController;
import GUI.controller.util.AsyncImageLoader;
import javafx.animation.FadeTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import module1.audioModel.AudioItem;
import module2.playlistModel.Playlist;
import module3.storage.PlaylistStorage;

import java.util.Collection;

public class CardBuildUtil {

    public interface CardInteractionListener {
        void onPlayClicked(AudioItem item, VBox card);
        void onCardSelected(AudioItem item, VBox card, boolean doubleClicked);
        void onRemoveRequested(AudioItem item);
        void onAddToQueue(AudioItem item);
        void onEditRequested(AudioItem item);
    }

    static protected VBox buildCard(AudioItem item, PlaylistStorage playlistStorage, PlaybackBarController playbackBarController, VBox playingCard, CardInteractionListener listener) {
        // Art square
        StackPane artPane = new StackPane();
        artPane.getStyleClass().add("card-art");

        // Placeholder icon (fallback)
        Label icon = new Label("♪");
        icon.getStyleClass().add("card-art-icon");

        // Actual image:
        ImageView coverView = new ImageView();
        coverView.setFitHeight(155);
        coverView.setFitWidth(155);
        coverView.setPreserveRatio(true);

        // rounded corners
        javafx.scene.shape.Rectangle clip = new javafx.scene.shape.Rectangle(155, 155);
        clip.setArcWidth(10);  // The amount of rounding (increase for more circular)
        clip.setArcHeight(10); // Keep this matching the ArcWidth
        coverView.setClip(clip);

        // Hover play button
        Button playButton = new Button("▶");
        playButton.getStyleClass().add("hover-play-button");

        playButton.setOpacity(0.0);
        playButton.setVisible(false);
        playButton.setFocusTraversable(false);

        // Align to bottom right
        StackPane.setAlignment(playButton, Pos.BOTTOM_RIGHT);
        StackPane.setMargin(playButton, new Insets(0, 15, 15, 0));

        // Animation
        FadeTransition fadeIn = new FadeTransition(Duration.millis(150), playButton);
        fadeIn.setToValue(0.85);

        FadeTransition fadeOut = new FadeTransition(Duration.millis(150), playButton);
        fadeOut.setToValue(0.0);
        fadeOut.setOnFinished(event -> playButton.setVisible(false));

        artPane.getChildren().addAll(icon,  coverView, playButton);

        // ── asynchronous album loading
        AsyncImageLoader.libraryImageLoad(item.getFileLocation(), coverView, icon);

        // ── Title ────────────────────────────────────────────────────────
        Label title = new Label(item.getTitle());
        title.getStyleClass().add("card-title");
        title.setMaxWidth(140);

        // ── Artist ───────────────────────────────────────────────────────
        Label artist = new Label(item.getAuthor());
        artist.getStyleClass().add("card-artist");
        artist.setMaxWidth(140);

        VBox card = new VBox();
        card.getStyleClass().add("track-card");
        card.getChildren().addAll(artPane, title, artist);

        card.setUserData(item.getTrackID());

        // Stash button inside card's properties
        card.getProperties().put("playButton", playButton);
        card.getProperties().put("fadeIn", fadeIn);
        card.getProperties().put("fadeOut", fadeOut);

        // Hover logic
        card.hoverProperty().addListener((obs, wasHovered, isHovered) -> {
            if (playingCard == card) {
                // It's the active track. Stop animations and lock it visible.
                fadeIn.stop();
                fadeOut.stop();
                playButton.setVisible(true);
                playButton.setOpacity(0.85);
            } else {
                if (isHovered) {
                    fadeOut.stop(); // Cancel any outgoing fade
                    playButton.setVisible(true); // Must be true so we can see it fade in
                    fadeIn.playFromStart();
                } else {
                    fadeIn.stop(); // Cancel any incoming fade
                    fadeOut.playFromStart();
                }
            }
        });

        // ContextMenu when right clicking on an item
        ContextMenu menu = new ContextMenu();
        menu.getStyleClass().add("right-click-menu");

        MenuItem edit = new MenuItem("Edit Track Info");

        edit.setOnAction(e -> listener.onEditRequested(item));

        MenuItem remove = new MenuItem("Remove from library");
        MenuItem addToQueue = new MenuItem("Add to queue");
        Menu addToPlaylist = new Menu("Add to Playlist");

        menu.setOnShowing(e ->{
            addToPlaylist.getItems().clear();
            Collection<Playlist> playlists = playlistStorage.getAllPlaylists();

            if(playlists.isEmpty()){
                MenuItem none = new MenuItem("No playlists yet");
                none.setDisable(true);
                addToPlaylist.getItems().add(none);
            }
            else{
                for(Playlist pl: playlists){
                    MenuItem playlistOption = new MenuItem(pl.getTitle());
                    playlistOption.setOnAction(f ->
                            pl.addTrack(item));
                    addToPlaylist.getItems().add(playlistOption);
                }
            }
        });



        remove.setOnAction(e -> listener.onRemoveRequested(item));
        addToQueue.setOnAction(e -> listener.onAddToQueue(item));

        menu.getItems().addAll(addToQueue, addToPlaylist, edit, remove);

        // Play button click logic
        playButton.setOnMouseClicked(e -> {
            if (e.getButton() == MouseButton.PRIMARY) {
                e.consume();
                listener.onPlayClicked(item, card);
            }
        });

        card.setOnMouseClicked(event ->{
            if(event.getButton() == MouseButton.PRIMARY) {
                event.consume();
                listener.onCardSelected(item, card, event.getClickCount() == 2);
            }
        });

        card.setOnContextMenuRequested(event -> {
            menu.show(card, event.getScreenX(), event.getScreenY());
        });

        return card;
    }
}
