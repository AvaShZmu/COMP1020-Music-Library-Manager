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

/**
 * A utility class for loading image assets asynchronously to prevent the JavaFX
 * Application Thread from freezing during disk I/O operations.
 * <p>
 *     This class uses {@link CompletableFuture} to read metadata in the background,
 *     and updates the UI components on the main thread using {@code Platform.runLater}.
 * </p>
 *
 * @version 1.0
 */

public class AsyncImageLoader {

    /* Public loaders */

    /**
     * Asynchronously loads an image for static library track cards.
     *
     * @param fileLocation The absolute or relative path to the audio file.
     * @param imageView    The {@link ImageView} target to display the extracted artwork.
     * @param fallbackIcon The {@link Node} (usually a Label) to display if extraction fails.
     */
    public static void libraryImageLoad(String fileLocation, ImageView imageView, Node fallbackIcon) {
        CompletableFuture.supplyAsync(() -> MetadataExtractor.getImage(fileLocation))
                .thenAccept(imageBytes -> applyImage(imageBytes, imageView, fallbackIcon, () -> true));
    }

    /**
     * Asynchronously loads an image for dynamically recycled JavaFX {@link Cell} components.
     * It checks whether the cell still contains the expected item before applying the image to
     * prevent mismatched artwork when scrolling rapidly.
     *
     * @param fileLocation The absolute or relative path to the audio file.
     * @param imageView    The target {@link ImageView}.
     * @param fallbackIcon The fallback icon to display while loading or on failure.
     * @param cell         The UI cell requesting the image.
     * @param expectedItem The specific data model the cell was holding when the request started.
     * @param <T>          The type of the item held by the cell.
     */
    public static <T> void cellImageLoad(String fileLocation, ImageView imageView, Node fallbackIcon, Cell<T> cell, T expectedItem) {
        imageView.setImage(null);
        if (fallbackIcon != null) fallbackIcon.setVisible(true);

        CompletableFuture.supplyAsync(() -> MetadataExtractor.getImage(fileLocation))
                .thenAccept(imageBytes -> applyImage(imageBytes, imageView, fallbackIcon, () -> cell.getItem() == expectedItem));
    }

    /**
     * Asynchronously loads an image for the playback bar.
     * Uses user data tags to ensure the image being applied matches the currently playing track.
     *
     * @param fileLocation The absolute or relative path to the audio file.
     * @param imageView    The target {@link ImageView}.
     * @param fallbackIcon The fallback icon to display while loading or on failure.
     * @param current      The specific track object currently active in the player.
     */
    public static void playbarImageLoad(String fileLocation, ImageView imageView, Node fallbackIcon, Object current) {
        imageView.setUserData(current);
        imageView.setImage(null);
        if (fallbackIcon != null) fallbackIcon.setVisible(true);

        CompletableFuture.supplyAsync(() -> MetadataExtractor.getImage(fileLocation))
                .thenAccept(imageBytes -> applyImage(imageBytes, imageView, fallbackIcon, () -> current.equals(imageView.getUserData())));
    }

    /* Apply image logic */

    /**
     * Applies the extracted image bytes to the JavaFX UI thread.
     *
     * @param imageBytes The raw binary data of the image (can be null).
     * @param imageView  The target {@link ImageView}.
     * @param fallbackIcon The fallback icon to hide upon success.
     * @param isValid    A validation callback that must return true for the image to be applied.
     */
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
