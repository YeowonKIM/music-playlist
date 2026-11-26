package com.example.musicplaylist.shuffle.strategy;

import com.example.musicplaylist.song.domain.Song;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class GenreBalancedShuffleStrategyTest {

    private final GenreBalancedShuffleStrategy strategy = new GenreBalancedShuffleStrategy();

    @Test
    void shouldPreserveAllSongsWithoutModifyingOriginalList() {
        Song firstSong = createSong("Yellow", "Coldplay", "Rock");
        Song secondSong = createSong("Clocks", "Coldplay", "Rock");
        Song thirdSong = createSong("Hello", "Adele", "Pop");
        Song fourthSong = createSong("Take Five", "Dave Brubeck", "Jazz");

        List<Song> songs = List.of(firstSong, secondSong, thirdSong, fourthSong);
        List<Song> shuffledSongs = strategy.shuffle(songs);

        assertThat(shuffledSongs).isNotSameAs(songs).containsExactlyInAnyOrderElementsOf(songs);
        assertThat(songs).containsExactly(firstSong, secondSong, thirdSong, fourthSong);
    }

    @Test
    void shouldDistributeGenresEqually() {
        List<Song> songs = List.of(
                createSong("Yellow", "Coldplay", "Rock"),
                createSong("Clocks", "Coldplay", "Rock"),
                createSong("Hello", "Adele", "Pop"),
                createSong("Shape of You", "Ed Sheeran", "Pop"),
                createSong("Take Five", "Dave Brubeck", "Jazz"),
                createSong("So What", "Miles Davis", "Jazz")
        );

        List<Song> shuffledSongs = strategy.shuffle(songs);

        assertThat(shuffledSongs).containsExactlyInAnyOrderElementsOf(songs);

        List<String> genres = shuffledSongs.stream()
                .map(Song::getGenre)
                .toList();

        assertThat(genres.subList(0, 3)).containsExactlyInAnyOrder("Rock", "Pop", "Jazz");
        assertThat(genres.subList(3, 6)).containsExactlyElementsOf(genres.subList(0, 3));
    }

    @Test
    void shouldContinueWithRemainingGenresWhenOthersAreExhausted() {
        List<Song> songs = List.of(
                createSong("Yellow", "Coldplay", "Rock"),
                createSong("Clocks", "Coldplay", "Rock"),
                createSong("Fix You", "Coldplay", "Rock"),
                createSong("Hello", "Adele", "Pop"),
                createSong("Shape of You", "Ed Sheeran", "Pop"),
                createSong("Take Five", "Dave Brubeck", "Jazz")
        );

        List<Song> shuffledSongs = strategy.shuffle(songs);

        assertThat(shuffledSongs).containsExactlyInAnyOrderElementsOf(songs);

        List<String> genres = shuffledSongs.stream()
                .map(Song::getGenre)
                .toList();

        assertThat(genres.subList(0, 3)).containsExactlyInAnyOrder("Rock", "Pop", "Jazz");
        assertThat(genres.subList(3, 5)).containsExactlyInAnyOrder("Rock", "Pop");
        assertThat(genres.get(5)).isEqualTo("Rock");
    }

    @Test
    void shouldHandleSongsBySingleGenre() {
        List<Song> songs = List.of(
                createSong("Yellow", "Coldplay", "Rock"),
                createSong("Clocks", "Coldplay", "Rock"),
                createSong("Bohemian Rhapsody", "Queen", "Rock")
        );

        List<Song> shuffledSongs = strategy.shuffle(songs);

        assertThat(shuffledSongs).containsExactlyInAnyOrderElementsOf(songs);
    }

    @Test
    void shouldHandleEmptyAndSingleSongPlaylists() {
        Song song = createSong("Yellow", "Coldplay", "Rock");

        assertThat(strategy.shuffle(List.of())).isEmpty();
        assertThat(strategy.shuffle(List.of(song))).containsExactly(song);
    }

    private Song createSong(String title, String artist, String genre) {
        return Song.builder()
                .title(title)
                .artist(artist)
                .genre(genre)
                .durationSeconds(240)
                .uri("music/sample.mp3")
                .build();
    }
}