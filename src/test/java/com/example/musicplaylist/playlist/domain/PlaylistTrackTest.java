package com.example.musicplaylist.playlist.domain;

import com.example.musicplaylist.common.exception.BusinessException;
import com.example.musicplaylist.common.exception.ErrorCode;
import com.example.musicplaylist.song.domain.Song;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

public class PlaylistTrackTest {
    private static ValidatorFactory validatorFactory;
    private static Validator validator;

    @BeforeAll
    static void setUp() {
        validatorFactory = Validation.buildDefaultValidatorFactory();
        validator = validatorFactory.getValidator();
    }

    @AfterAll
    static void tearDown() {
        validatorFactory.close();
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1, 10})
    void shouldCreateValidPlaylistTrack(int position) {
        Playlist playlist = Playlist.of("My Favorites", null);
        Song song = createSong();
        PlaylistTrack track = PlaylistTrack.of(playlist, song, position);

        assertThat(validator.validate(track)).isEmpty();
        assertThat(track.getId()).isNull();
        assertThat(track.getPlaylist()).isSameAs(playlist);
        assertThat(track.getSong()).isSameAs(song);
        assertThat(track.getPosition()).isEqualTo(position);
    }

    @Test
    void shouldRejectNullPlaylist() {
        PlaylistTrack track = PlaylistTrack.of(null, createSong(), 0);

        assertThat(validator.validate(track))
                .extracting(v -> v.getPropertyPath().toString())
                .containsExactly("playlist");
    }

    @Test
    void shouldRejectNullSong() {
        Playlist playlist = Playlist.of("My Favorites", null);
        PlaylistTrack track = PlaylistTrack.of(playlist, null, 0);

        assertThat(validator.validate(track))
                .extracting(v -> v.getPropertyPath().toString())
                .containsExactly("song");
    }

    @ParameterizedTest
    @ValueSource(ints = {-1, -10})
    void shouldRejectNegativePosition(int position) {
        Playlist playlist = Playlist.of("My Favorites", null);
        PlaylistTrack track = PlaylistTrack.of(playlist, createSong(), position);

        assertThat(validator.validate(track))
                .extracting(v -> v.getPropertyPath().toString())
                .containsExactly("position");
    }

    // Update position tests
    @Test
    void shouldUpdatePosition() {
        Playlist playlist = Playlist.of("My Playlist", "Description");
        PlaylistTrack track = PlaylistTrack.of(playlist, createSong(), 0);

        track.updatePosition(2);

        assertThat(track.getPosition()).isEqualTo(2);
    }

    @Test
    void shouldUpdatePositionToZero() {
        Playlist playlist = Playlist.of("My Playlist", "Description");
        PlaylistTrack track = PlaylistTrack.of(playlist, createSong(), 3);

        track.updatePosition(0);

        assertThat(track.getPosition()).isZero();
    }

    @ParameterizedTest
    @ValueSource(ints = {-1, -10})
    void shouldRejectNegativePositionUpdate(int position) {
        Playlist playlist = Playlist.of("My Playlist", "Description");
        PlaylistTrack track = PlaylistTrack.of(playlist, createSong(), 2);

        assertThatThrownBy(() -> track.updatePosition(position))
                .isInstanceOfSatisfying(
                        BusinessException.class,
                        exception -> {
                            assertThat(exception.getErrorCode())
                                    .isEqualTo(ErrorCode.INVALID_POSITION);
                            assertThat(exception.getMessage())
                                    .isEqualTo(ErrorCode.INVALID_POSITION.getMessage());
                        }
                );

        assertThat(track.getPosition()).isEqualTo(2);
    }

    private Song createSong() {
        return createSong("Yellow", "Coldplay", "Rock", 240, "music/yellow.mp3");
    }

    private Song createSong(String title, String artist, String genre, int durationSeconds, String uri) {
        return Song.builder()
                .title(title)
                .artist(artist)
                .genre(genre)
                .durationSeconds(durationSeconds)
                .uri(uri)
                .build();
    }
}
