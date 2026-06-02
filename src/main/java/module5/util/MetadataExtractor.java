package module5.util;

import module1.audioModel.AudioItem;
import java.io.File;
import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.AudioHeader;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;
import org.jaudiotagger.tag.images.Artwork;

/**
 * A utility class responsible for extracting ID3 tags and metadata from local audio files.
 * <p>
 * This class acts as a wrapper around the external {@code jAudioTagger} library.
 * It automatically reads local files and constructs the application's internal {@link AudioItem} models.
 * </p>
 */

public class MetadataExtractor {

    /* Extraction methods */

    /**
     * Parses a local audio file and extracts its embedded metadata to build an {@code AudioItem}.
     * If certain ID3 fields (like Title or Artist) are missing, this method automatically
     * resolves them to safe fallback strings (e.g., using the file name as the title).
     *
     * @param filePath The path to the local audio file.
     * @return A fully constructed {@link AudioItem} containing the extracted data,
     * or {@code null} if the file cannot be read or processed.
     */
    public static AudioItem extract(String filePath) {
        try {
            // jAudioTagger reads file
            File audioFileObject = new File(filePath);
            AudioFile audioFile = AudioFileIO.read(audioFileObject);

            // Get ID3 Tags
            Tag tag = audioFile.getTag();
            String title = tag.getFirst(FieldKey.TITLE);
            String artist = tag.getFirst(FieldKey.ARTIST);
            String releaseDate = tag.getFirst(FieldKey.YEAR);
            String genre = tag.getFirst(FieldKey.GENRE);

            // Get AudioHeader
            AudioHeader audioHeader = audioFile.getAudioHeader();
            int durationInSeconds = audioHeader.getTrackLength();

            // Edge Case:
            if (title == null || title.isEmpty()) {
                title = audioFileObject.getName().replace(".mp3", "");
            }

            if (artist == null || artist.isEmpty()) {
                artist = "Unknown Artist";
            }

            if (genre == null || genre.isEmpty()) {
                genre = "Unknown";
            }

            return new AudioItem(
                    title,
                    artist,
                    releaseDate,
                    durationInSeconds,
                    genre,
                    filePath
            );
        }
        catch (Exception e) {
            // Fallback
            System.err.println("Error reading file: " + filePath + ". Resort to fallback.");
            return null;
        }
    }

    /**
     * Extracts the embedded cover artwork image from a given audio file.
     *
     * @param filePath The absolute or relative path to the local audio file.
     * @return A {@code byte[]} array containing the raw binary image data,
     * or {@code null} if the file contains no artwork or fails to be read.
     */
    public static byte[] getImage(String filePath) {
        try {
            File audioFileObject = new File(filePath);
            AudioFile audioFile = AudioFileIO.read(audioFileObject);
            Tag tag = audioFile.getTag();

            if (tag == null) return null;

            Artwork artwork = tag.getFirstArtwork();
            if  (artwork == null) return null;
            // return raw bytes of img
            return artwork.getBinaryData();
        }
        catch  (Exception e) {
            System.err.println("Error reading file: " + filePath);
        }

        return null;
    }
}
