package com.yourname.controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * PlaylistController — STUB for Phase 2.
 *
 * The real implementation is built in Phase 6.
 */
public class PlaylistController implements Initializable {

    @FXML private VBox root;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        System.out.println("PlaylistController stub initialized");
    }

    /**
     * Called by MainController after the view is loaded.
     * Tells this controller which playlist to display.
     */
    public void loadPlaylist(String playlistTitle) {
        // Phase 6 implementation
        System.out.println("Loading playlist: " + playlistTitle);
    }
}
