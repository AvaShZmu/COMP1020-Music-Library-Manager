module com.yourname {
        requires com.google.gson;
        requires jaudiotagger;

        requires javafx.base;
        requires javafx.controls;
        requires javafx.fxml;
        requires javafx.graphics;
        requires javafx.media;

        opens GUI to javafx.fxml;
        opens GUI.controller to javafx.fxml;
        exports GUI;
        exports GUI.controller;
        exports module1.audioModel;
        exports module2.playlistModel;
        exports module3.storage;
        exports module4.playback;
        exports module5.util;
}