package test;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import java.io.File;

public class ImageLoaderTest extends Application {

    @Override
    public void start(Stage primaryStage) {
        // 1. Point directly to the debug file we saved earlier
        File file = new File("debug_art.jpg");
        String uri = file.toURI().toString();

        System.out.println("Attempting to load: " + uri);

        // 2. Force JavaFX to decode the image natively
        Image image = new Image(uri);

        // 3. The Interrogation: Print exactly what JavaFX thinks happened
        if (image.isError()) {
            System.err.println("Verdict: JavaFX explicitly rejected the file format.");
            System.err.println("Exception: " + image.getException().getMessage());
        } else {
            System.out.println("Verdict: JavaFX claims it loaded the image successfully.");
            System.out.println("Reported Dimensions: " + image.getWidth() + " x " + image.getHeight());
        }

        // 4. The Visual Proof
        ImageView imageView = new ImageView(image);
        imageView.setFitWidth(140);
        imageView.setFitHeight(140);
        imageView.setPreserveRatio(true);

        StackPane root = new StackPane(imageView);
        root.setStyle("-fx-background-color: #333333;"); // Dark background to see the edges

        Scene scene = new Scene(root, 500, 500);
        primaryStage.setTitle("JavaFX Decoder Trial");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}