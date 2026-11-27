package com.example.musicplaylist.recommendation.strategy;

import com.example.musicplaylist.song.domain.Song;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ContentBasedRecommendationStrategyTest {

    private final RecommendationStrategy recommendationStrategy = new ContentBasedRecommendationStrategy(new GenreCompatibility());

    @Test
    void shouldRankSongsByGenreCompatibilityAndArtistAffinity() {
        Song yellow = createSong(1L, "Yellow", "Coldplay", "Rock");
        Song clocks = createSong(2L, "Clocks", "Coldplay", "Rock");
        Song hello = createSong(3L, "Hello", "Adele", "Pop");

        Song fixYou = createSong(4L, "Fix You", "Coldplay", "Rock");
        Song easyOnMe = createSong(5L, "Easy On Me", "Adele", "Pop");
        Song bohemianRhapsody = createSong(6L, "Bohemian Rhapsody", "Queen", "Rock");
        Song takeFive = createSong(7L, "Take Five", "Dave Brubeck", "Jazz");

        List<Song> playlistSongs = List.of(yellow, clocks, hello);
        List<Song> candidates = List.of(takeFive, bohemianRhapsody, easyOnMe, fixYou);
        List<Song> recommendations = recommendationStrategy.recommend(playlistSongs, candidates, 10);

        assertThat(recommendations).containsExactly(fixYou, easyOnMe, bohemianRhapsody, takeFive);
    }

    @Test
    void shouldPreferMoreFrequentGenres() {
        Song yellow = createSong(1L, "Yellow", "Coldplay", "Rock");
        Song clocks = createSong(2L, "Clocks", "Coldplay", "Rock");
        Song hello = createSong(3L, "Hello", "Adele", "Pop");

        Song wonderwall = createSong(4L, "Wonderwall", "Oasis", "Rock");
        Song shakeItOff = createSong(5L, "Shake It Off", "Taylor Swift", "Pop");

        List<Song> playlistSongs = List.of(yellow, clocks, hello);
        List<Song> candidates = List.of(shakeItOff, wonderwall);
        List<Song> recommendations = recommendationStrategy.recommend(playlistSongs, candidates, 10);

        assertThat(recommendations).containsExactly(wonderwall, shakeItOff);
    }

    @Test
    void shouldExcludeExistingPlaylistSongsAndUnrelatedSongs() {
        Song yellow = createSong(1L, "Yellow", "Coldplay", "Rock");
        Song existingSong = createSong(1L, "Yellow", "Coldplay", "Rock");
        Song fixYou = createSong(2L, "Fix You", "Coldplay", "Rock");
        Song moonlightSonata = createSong(3L, "Moonlight Sonata", "Beethoven", "Classical");

        List<Song> playlistSongs = List.of(yellow);
        List<Song> candidates = List.of(existingSong, fixYou, moonlightSonata);
        List<Song> recommendations = recommendationStrategy.recommend(playlistSongs, candidates, 10);

        assertThat(recommendations).containsExactly(fixYou);
    }

    @Test
    void shouldReturnEmptyRecommendationsWhenPlaylistOrCandidatesAreEmpty() {
        Song yellow = createSong(1L, "Yellow", "Coldplay", "Rock");

        assertThat(recommendationStrategy.recommend(List.of(), List.of(yellow), 10)).isEmpty();
        assertThat(recommendationStrategy.recommend(List.of(yellow), List.of(), 10)).isEmpty();
    }

    @Test
    void shouldRejectNonPositiveLimit() {
        assertThatThrownBy(() -> recommendationStrategy.recommend(List.of(), List.of(), 0)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> recommendationStrategy.recommend(List.of(), List.of(), -1)).isInstanceOf(IllegalArgumentException.class);
    }

    private Song createSong(Long id, String title, String artist, String genre) {
        Song song = Song.builder()
                .title(title)
                .artist(artist)
                .genre(genre)
                .durationSeconds(240)
                .uri("music/" + title + ".mp3")
                .build();

        ReflectionTestUtils.setField(song, "id", id);

        return song;
    }
}