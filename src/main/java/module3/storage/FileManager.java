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

public class FileManager {
    private String audioFilePath;
    private String playlistFilePath;

    public FileManager(String audioFilePath, String playlistFilePath) {
        this.audioFilePath = audioFilePath;
        this.playlistFilePath = playlistFilePath;
    }

    /* AudioItem */

    // Saving
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

    // Loading
    public Collection<AudioItem> loadAudio() {
        File file = new File(audioFilePath);

        // If file doesnt exist yet, return empty
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

    public void setAudioFilePath(String audioFilePath) {
        this.audioFilePath = audioFilePath;
    }

    /* Playlist */

    // Saving
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

    // Loading
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

    public void setPlaylistFilePath(String playlistFilePath) {
        this.playlistFilePath = playlistFilePath;
    }
}
