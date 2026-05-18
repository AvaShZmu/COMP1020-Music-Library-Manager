package GUI.controller.util;

import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.control.Cell;
import module5.util.MetadataExtractor;

import java.io.ByteArrayInputStream;
import java.util.concurrent.CompletableFuture;
import java.util.function.BooleanSupplier;

public class AsyncImageLoader {
    public static void libraryImageLoad(String fileLocation, ImageView imageView, Node fallbackIcon) {
        CompletableFuture.supplyAsync(() -> MetadataExtractor.getImage(fileLocation))
                .thenAccept(imageBytes -> applyImage(imageBytes, imageView, fallbackIcon, () -> true));
    }

    public static <T> void cellImageLoad(String fileLocation, ImageView imageView, Node fallbackIcon, Cell<T> cell, T expectedItem) {
        imageView.setImage(null);
        if (fallbackIcon != null) fallbackIcon.setVisible(true);

        CompletableFuture.supplyAsync(() -> MetadataExtractor.getImage(fileLocation))
                .thenAccept(imageBytes -> applyImage(imageBytes, imageView, fallbackIcon, () -> cell.getItem() == expectedItem));
    }

    public static void playbarImageLoad(String fileLocation, ImageView imageView, Node fallbackIcon, Object current) {
        imageView.setUserData(current);
        imageView.setImage(null);
        if (fallbackIcon != null) fallbackIcon.setVisible(true);

        CompletableFuture.supplyAsync(() -> MetadataExtractor.getImage(fileLocation))
                .thenAccept(imageBytes -> applyImage(imageBytes, imageView, fallbackIcon, () -> current.equals(imageView.getUserData())));
    }

    private static void applyImage(byte[] imageBytes, ImageView imageView, Node fallbackIcon, BooleanSupplier isValid) {
        if (imageBytes != null && imageBytes.length > 0) {
            Image image = new Image(new ByteArrayInputStream(imageBytes));
            Platform.runLater(() -> {
                if (isValid.getAsBoolean() && !image.isError() && image.getWidth() > 0) {
                    imageView.setImage(image);
                    if (fallbackIcon != null) {
                        fallbackIcon.setVisible(false);
                    }
                }
            });
        }
    }
}
