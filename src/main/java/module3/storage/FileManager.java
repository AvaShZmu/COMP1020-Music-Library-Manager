package module3.storage;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import module1.audioModel.AudioItem;
import module2.playlistModel.Playlist;

/**
 * Handles the persistent storage of system data by saving and loading
 * objects to and from local JSON files.
 * <p>
 *     This class leverages the Google Gson library to safely save the
 *     status of the active audio library and user playlists between
 *     application sessions. Automatically returns empty collections if
 *     the target files do not yet exist or suffers from read errors.
 * </p>
 */

public class FileManager {
    private String audioFilePath;
    private String playlistFilePath;

    /**
     * Constructs a new {@code FileManager} with specific target file paths.
     * @param audioFilePath The relative or absolute path to the audio library JSON file.
     * @param playlistFilePath The relative or absolute path to the playlist JSON file.
     */
    public FileManager(String audioFilePath, String playlistFilePath) {
        this.audioFilePath = audioFilePath;
        this.playlistFilePath = playlistFilePath;
    }

    /* AudioItem */

    /**
     * Saves the active audio library to a local JSON file.
     *
     * @param allTracks A {@link Collection} of all current {@link AudioItem} objects.
     */
    public void saveAudio(Collection<AudioItem> allTracks) {
        // Create new gson obj
        Gson gson = new GsonBuilder().setPrettyPrinting().create();

        try (FileWriter writer = new FileWriter(audioFilePath)) {
            gson.toJson(allTracks, writer);
            System.out.println("Saved audio file to: " + audioFilePath);
        }
        catch (IOException e) {
            System.err.println("Error saving audio file to: " + audioFilePath);
        }
    }

    /**
     * Loads the audio library from a local JSON file.
     * If the file does not exist or fails to load, an empty list is returned.
     *
     * @return A {@link Collection} of loaded {@link AudioItem} objects.
     */
    public Collection<AudioItem> loadAudio() {
        File file = new File(audioFilePath);

        // If file doesn't exist yet, return empty
        if (!file.exists()) {
            System.out.println("No existing library found. Starting fresh.");
            return new ArrayList<>();
        }

        Gson gson = new Gson();
        try (FileReader reader = new FileReader(audioFilePath)) {
            Type collectionType = new TypeToken<ArrayList<AudioItem>>(){}.getType();
            Collection<AudioItem> loadedMusic = gson.fromJson(reader, collectionType);

            if (loadedMusic == null) {
                return new ArrayList<>();
            }

            System.out.println("Library successfully loaded from " + audioFilePath);
            return loadedMusic;
        }
        catch  (IOException e) {
            System.err.println("Error loading library from " + audioFilePath);
            return new ArrayList<>();
        }
    }

    /**
     * Updates the target file path for saving and loading audio data.
     *
     * @param audioFilePath The new file path string.
     */
    public void setAudioFilePath(String audioFilePath) {
        this.audioFilePath = audioFilePath;
    }

    /* Playlist */

    /**
     * Saves the active playlist repository to a local JSON file.
     *
     * @param allPlaylists A {@link Collection} of all current {@link Playlist} objects.
     */
    public void savePlaylist(Collection<Playlist> allPlaylists) {
        // Create new gson obj
        Gson gson = new GsonBuilder().setPrettyPrinting().create();

        try (FileWriter writer = new FileWriter(playlistFilePath)) {
            gson.toJson(allPlaylists, writer);
            System.out.println("Saved playlist file to: " + playlistFilePath);
        }
        catch (IOException e) {
            System.err.println("Error saving playlist file to: " + playlistFilePath);
        }
    }

    /**
     * Loads the playlist repository from a local JSON file.
     * If the file does not exist or fails to load, an empty list is returned.
     *
     * @return A {@link Collection} of loaded {@link Playlist} objects.
     */
    public Collection<Playlist> loadPlaylist() {
        File file = new File(playlistFilePath);

        // If file doesnt exist yet, return empty
        if (!file.exists()) {
            System.out.println("No existing library found. Starting fresh.");
            return new ArrayList<>();
        }

        Gson gson = new Gson();
        try (FileReader reader = new FileReader(playlistFilePath)) {
            Type collectionType = new TypeToken<ArrayList<Playlist>>(){}.getType();
            Collection<Playlist> loadedPlaylist = gson.fromJson(reader, collectionType);

            if (loadedPlaylist == null) {
                return new ArrayList<>();
            }

            System.out.println("Library successfully loaded from " + playlistFilePath);
            return loadedPlaylist;
        }
        catch  (IOException e) {
            System.err.println("Error loading library from " + playlistFilePath);
            return new ArrayList<>();
        }
    }

    /**
     * Updates the target file path for saving and loading playlist data.
     *
     * @param playlistFilePath The new file path string.
     */
    public void setPlaylistFilePath(String playlistFilePath) {
        this.playlistFilePath = playlistFilePath;
    }
}
