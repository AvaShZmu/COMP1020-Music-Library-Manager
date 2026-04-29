package test;

import module1.audioModel.AudioItem;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import module5.util.LibraryLogic;

public class AudioItemTest {

    public static void main(String[] args) {
        System.out.println("--- Audioitem test ---\n");

        // initiate random new AudioItems

        AudioItem track1 = new AudioItem("Bohemian Rhapsody", "Queen", "31/10/1975", 354, "Rock", "bohemianrhapsody.mp3");
        AudioItem track2 = new AudioItem("Hotel California", "Eagles", "1976/12/08", 390, "Rock", "hotelcalifornia.mp3");
        AudioItem track3 = new AudioItem("Somebody To Love", "Queen", "1976/11/12", 296, "Rock", "somebodytolove.mp3");
        AudioItem track4 = new AudioItem("Don't Stop Me Now", "Queen", "26/01/1979", 209, "Rock", "dontstopmenow.mp3");
        AudioItem track5 = new AudioItem("Stairway to Heaven", "Led Zeppelin", "1971/11/08", 482, "Rock", "stairwaytoheaven.mp3");
        AudioItem track6 = new AudioItem("Blinding Lights", "The Weeknd", "29/11/2019", 200, "Synthpop", "blindinglights.mp3");
        AudioItem track7 = new AudioItem("Starboy", "The Weeknd", "2016/09/21", 230, "Pop", "starboy.mp3");
        AudioItem track8 = new AudioItem("Levitating", "Dua Lipa", "01/10/2020", 203, "Pop", "levitating.mp3");
        AudioItem track9 = new AudioItem("Cruel Summer", "Taylor Swift", "2019/08/23", 178, "Pop", "cruelsummer.mp3");
        AudioItem track10 = new AudioItem("HUMBLE.", "Kendrick Lamar", "30/03/2017", 177, "Hip Hop", "humble.mp3");
        AudioItem track11 = new AudioItem("Still D.R.E.", "Dr. Dre", "1999/11/02", 270, "Hip Hop", "stilldre.mp3");
        AudioItem track12 = new AudioItem("Superstition", "Stevie Wonder", "24/10/1972", 266, "R&B", "superstition.mp3");
        AudioItem track13 = new AudioItem("Get Lucky", "Daft Punk", "2013/04/19", 369, "Electronic", "getlucky.mp3");
        AudioItem track14 = new AudioItem("Levels", "Avicii", "28/10/2011", 199, "Electronic", "levels.mp3");
        AudioItem track15 = new AudioItem("Take Five", "Dave Brubeck", "1959/07/01", 324, "Jazz", "takefive.mp3");
        AudioItem track16 = new AudioItem("Time", "Hans Zimmer", "16/07/2010", 275, "Soundtrack", "time.mp3");

        // add to library
        List<AudioItem> library = new ArrayList<>();
        Collections.addAll(library,
                track12, track3, track15, track7, track1, track9, track14, track2,
                track11, track5, track8, track16, track4, track10, track13, track6
        ); // add out of order to ensure sorting works

        // Test: matchesQuery (from Searchable interface)
        String query = "sta";
        System.out.printf("Test: search for %s\n", query);
        for (AudioItem track : LibraryLogic.search(library, query)) {
            System.out.println("Found: " + track.getTitle() + " (" + track.getAuthor() + ")");
        }
        System.out.println();

        // --- TEST 2: passesFilter (Filterable - Genre) ---
        System.out.println("Test: filter by genre == 'Rock'");
        String genre = "Rock";
        for (AudioItem track : LibraryLogic.filter(library, "genre", "=", genre)) {
            System.out.println("Found: " + track.getTitle() + " (" + track.getGenre() + ")");
        }
        System.out.println();

        // --- TEST 3: getFileLocation (Playable) ---
        System.out.println("Test: Check Playable File Location");
        System.out.println("Track 1 Location: " + track1.getFileLocation());
        System.out.println();

        // --- TEST 4: compareTo (Comparable - Alphabetical Sorting) ---
        System.out.println("Test 4: Alphabetical Sort");
        System.out.println("Before Sort:");
        library.forEach(item -> System.out.println(" - " + item.getTitle()));

        Collections.sort(library);

        System.out.println("\nAfter Sort:");
        library.forEach(item -> System.out.println(" - " + item.getTitle()));
        System.out.println();

        // --- TEST 5: passesFilter (Filterable - Complex Dates) ---
        // Testing if the logic correctly parses both formats and compares them.
        System.out.println("Test 5: Filter by Date > '2010/01/01']");
        String date = "2010/01/01";
        for (AudioItem track : LibraryLogic.filter(library, "date", ">", date)) {
            System.out.println("Found: " + track.getTitle() + " (" + track.getReleaseDate() + ")");
        }
    }
}