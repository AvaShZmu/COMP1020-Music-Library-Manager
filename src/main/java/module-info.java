module com.yourname {
        requires com.google.gson;
        opens module1.audioModel to com.google.gson;
        opens module2.playlistModel to com.google.gson;
        requires jaudiotagger;

        requires javafx.base;
        requires javafx.controls;
        requires javafx.fxml;
        requires javafx.graphics;
        requires javafx.media;

        opens GUI to javafx.fxml;
        exports GUI;
        exports module1.audioModel;
        exports module2.playlistModel;
        exports module3.storage;
        exports module4.playback;
        exports module5.util;
    exports GUI.controller.library;
    opens GUI.controller.library to javafx.fxml;
        exports GUI.controller.main;
        opens GUI.controller.main to javafx.fxml;
        exports GUI.controller.playback;
        opens GUI.controller.playback to javafx.fxml;
        exports GUI.controller.playlist;
        opens GUI.controller.playlist to javafx.fxml;
        exports GUI.controller.queue;
        opens GUI.controller.queue to javafx.fxml;
        exports GUI.controller.util.dialog;
        opens GUI.controller.util.dialog to javafx.fxml;
}