package com.yourname.controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * LibraryController — STUB for Phase 2.
 *
 * This file only exists so MainController can reference it without
 * a compile error. The real implementation is built in Phase 3.
 */
public class LibraryController implements Initializable {

    // Stub FXML field — LibraryView.fxml will have a real TableView here
    @FXML private VBox root;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Phase 3 will populate the TableView here
        System.out.println("LibraryController stub initialized");
    }

    /** Called by MainController when the search field changes. */
    public void applySearch(String query) {
        // Phase 4 implementation
    }

    /** Called by MainController when the Sort button is clicked. */
    public void cycleSortOrder() {
        // Phase 4 implementation
    }

    /** Called by MainController when the Filter button is clicked. */
    public void showFilterDialog() {
        // Phase 4 implementation
    }
}