package module3.storage;
import module2.playlistModel.Playlist;

import java.util.Collection;
import java.util.HashMap;

public class PlaylistStorage {
    protected HashMap<String, Playlist> playlistLibrary;

    public PlaylistStorage() {
        playlistLibrary = new HashMap<>();
    }

    public void addItem(Playlist playlist) {
        playlistLibrary.put(playlist.getPlaylistID(), playlist);
    }

    public void removeItem(Playlist playlist) {
        playlistLibrary.remove(playlist.getPlaylistID());
    }

    public Playlist getPlaylists(String playlistID) {
        return playlistLibrary.get(playlistID);
    }

    public Collection<Playlist> getAllPlaylists() {
        return playlistLibrary.values();
    }

    public void importPlaylists(Collection<Playlist> playlists) {
        for (Playlist playlist : playlists) {
            playlistLibrary.put(playlist.getPlaylistID(), playlist);
        }
    }
}
