package com.example.musicplaylist.shuffle.strategy;

import com.example.musicplaylist.song.domain.Song;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class RandomShuffleStrategyTest {

    private final RandomShuffleStrategy strategy = new RandomShuffleStrategy();

    @Test
    void shouldPreserveAllSongsWithoutModifyingOriginalList() {
        Song firstSong = createSong("Yellow", "Coldplay", "Rock");
        Song secondSong = createSong("Hello", "Adele", "Pop");
        Song thirdSong = createSong("Take Five", "Dave Brubeck", "Jazz");
        Song fourthSong = createSong("Clocks", "Coldplay", "Rock");

        List<Song> songs = List.of(firstSong, secondSong, thirdSong, fourthSong);
        List<Song> shuffledSongs = strategy.shuffle(songs);

        assertThat(shuffledSongs).isNotSameAs(songs).containsExactlyInAnyOrderElementsOf(songs);
        assertThat(songs).containsExactly(firstSong, secondSong, thirdSong, fourthSong);
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