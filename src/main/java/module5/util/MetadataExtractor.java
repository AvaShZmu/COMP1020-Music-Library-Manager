package module5.util;
import module1.audioModel.AudioItem;
import java.io.File;

import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.AudioHeader;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;

public class MetadataExtractor {
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

            // Get Audiohead
            AudioHeader audioHeader = audioFile.getAudioHeader();
            int durationInSeconds = audioHeader.getTrackLength();

            // Edge Case:
            if (title == null || title.isEmpty()) {
                title = audioFileObject.getName().replace(".mp3", "");
            }

            if (artist == null || artist.isEmpty()) {
                artist = "Unknown Artist";
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
            System.err.println("Error reading file: " + filePath);
            return null;
        }
    }
}
