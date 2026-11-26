package com.example.musicplaylist.shuffle.strategy;

import com.example.musicplaylist.song.domain.Song;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SmartShuffleStrategyTest {

    private final SmartShuffleStrategy strategy = new SmartShuffleStrategy();

    @Test
    void shouldPreserveAllSongsWithoutModifyingOriginalList() {
        Song firstSong = createSong("Yellow", "Coldplay");
        Song secondSong = createSong("Clocks", "Coldplay");
        Song thirdSong = createSong("Hello", "Adele");
        Song fourthSong = createSong("Take Five", "Dave Brubeck");

        List<Song> songs = List.of(firstSong, secondSong, thirdSong, fourthSong);
        List<Song> shuffledSongs = strategy.shuffle(songs);

        assertThat(shuffledSongs)
                .isNotSameAs(songs)
                .containsExactlyInAnyOrderElementsOf(songs);

        assertThat(songs)
                .containsExactly(firstSong, secondSong, thirdSong, fourthSong);
    }

    @Test
    void shouldAvoidConsecutiveSongsBySameArtistWhenPossible() {
        List<Song> songs = List.of(
                createSong("Yellow", "Coldplay"),
                createSong("Clocks", "Coldplay"),
                createSong("Fix You", "Coldplay"),
                createSong("The Scientist", "Coldplay"),
                createSong("Hello", "Adele"),
                createSong("Rolling in the Deep", "Adele"),
                createSong("Bohemian Rhapsody", "Queen")
        );

        List<Song> shuffledSongs = strategy.shuffle(songs);

        assertThat(shuffledSongs).containsExactlyInAnyOrderElementsOf(songs);
        assertThat(countConsecutiveArtistRepetitions(shuffledSongs)).isZero();
    }

    @Test
    void shouldMinimizeConsecutiveSongsBySameArtistWhenUnavoidable() {
        List<Song> songs = List.of(
                createSong("Yellow", "Coldplay"),
                createSong("Clocks", "Coldplay"),
                createSong("Fix You", "Coldplay"),
                createSong("The Scientist", "Coldplay"),
                createSong("Viva La Vida", "Coldplay"),
                createSong("Hello", "Adele"),
                createSong("Rolling in the Deep", "Adele")
        );

        List<Song> shuffledSongs = strategy.shuffle(songs);

        assertThat(shuffledSongs).containsExactlyInAnyOrderElementsOf(songs);
        assertThat(countConsecutiveArtistRepetitions(shuffledSongs)).isEqualTo(2);
    }

    @Test
    void shouldHandleSongsBySingleArtist() {
        List<Song> songs = List.of(
                createSong("Yellow", "Coldplay"),
                createSong("Clocks", "Coldplay"),
                createSong("Fix You", "Coldplay")
        );

        List<Song> shuffledSongs = strategy.shuffle(songs);

        assertThat(shuffledSongs).containsExactlyInAnyOrderElementsOf(songs);
        assertThat(countConsecutiveArtistRepetitions(shuffledSongs)).isEqualTo(2);
    }

    @Test
    void shouldHandleEmptyAndSingleSongPlaylists() {
        Song song = createSong("Yellow", "Coldplay");

        assertThat(strategy.shuffle(List.of())).isEmpty();
        assertThat(strategy.shuffle(List.of(song))).containsExactly(song);
    }

    private int countConsecutiveArtistRepetitions(List<Song> songs) {
        int repetitions = 0;

        for (int i = 1; i < songs.size(); i++) {
            if (songs.get(i - 1).getArtist().equals(songs.get(i).getArtist())) {
                repetitions++;
            }
        }

        return repetitions;
    }

    private Song createSong(String title, String artist) {
        return Song.builder()
                .title(title)
                .artist(artist)
                .genre("Rock")
                .durationSeconds(240)
                .uri("music/sample.mp3")
                .build();
    }
}