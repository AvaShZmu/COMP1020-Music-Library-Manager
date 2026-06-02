package module3.storage;

import module2.playlistModel.Playlist;
import java.util.Collection;
import java.util.HashMap;

/**
 * Provides an in-memory repository for managing user-curated playlists.
 * <p>
 *     Uses a {@link HashMap} to store {@link Playlist} objects,
 *     mapping each playlist's UUID to its instance.
 *     This helps ensure O(1) average lookups for playlist retrieval.
 * </p>
 */

public class PlaylistStorage {
    protected HashMap<String, Playlist> playlistLibrary;

    /**
     * Constructs a new, empty {@code PlaylistStorage} repository.
     */
    public PlaylistStorage() {
        playlistLibrary = new HashMap<>();
    }

    /**
     * Adds a new playlist to the repository.
     *
     * @param playlist The {@link Playlist} to insert.
     */
    public void addItem(Playlist playlist) {
        playlistLibrary.put(playlist.getPlaylistID(), playlist);
    }

    /**
     * Removes an existing playlist from the repository.
     *
     * @param playlist The {@link Playlist} to remove.
     */
    public void removeItem(Playlist playlist) {
        playlistLibrary.remove(playlist.getPlaylistID());
    }

    /**
     * Retrieves a specific playlist from the repository using its unique ID.
     *
     * @param playlistID The UUID string of the desired playlist.
     * @return The corresponding {@link Playlist}, or {@code null} if not found.
     */
    public Playlist getPlaylists(String playlistID) {
        return playlistLibrary.get(playlistID);
    }

    /**
     * Retrieves all playlists currently stored in the repository.
     *
     * @return A {@link Collection} of all stored {@link Playlist} objects.
     */
    public Collection<Playlist> getAllPlaylists() {
        return playlistLibrary.values();
    }

    /**
     * Performs a bulk import of playlists into the repository.
     *
     * @param playlists A {@link Collection} of {@link Playlist} objects to import.
     */
    public void importPlaylists(Collection<Playlist> playlists) {
        for (Playlist playlist : playlists) {
            playlistLibrary.put(playlist.getPlaylistID(), playlist);
        }
    }
}
