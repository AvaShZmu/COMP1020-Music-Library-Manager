package test;
import module1.audioModel.AudioItem;
import module5.util.MetadataExtractor;

public class ExtractorTest {
    public static void main(String[] args) {
        String filePath = "src/test/resources/musicFolder/test1.mp3";
        AudioItem result = MetadataExtractor.extract(filePath);
        if (result != null) {
            System.out.println("Title: " + result.getTitle());
            System.out.println("Author: " + result.getAuthor());
            System.out.println("Release Date: " + result.getReleaseDate());
            System.out.println("Duration: " + result.getDuration());
            System.out.println("Genre: " + result.getGenre());
        }
        else {
            System.out.println("Audio Item Not Found");
        }
    }
}
