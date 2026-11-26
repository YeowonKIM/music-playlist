package com.example.musicplaylist.playlist.domain;

import com.example.musicplaylist.song.domain.Song;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class PlaylistTest {
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

    // Bean validation tests
    @Test
    void shouldCreateValidPlaylist() {
        Playlist playlist = new Playlist("My Favorites", "My favorite songs");

        assertThat(validator.validate(playlist)).isEmpty();
        assertThat(playlist.getId()).isNull();
        assertThat(playlist.getName()).isEqualTo("My Favorites");
        assertThat(playlist.getDescription()).isEqualTo("My favorite songs");
        assertThat(playlist.getTracks()).isEmpty();
    }

    @Test
    void shouldAllowNullDescription() {
        Playlist playlist = new Playlist("My Favorites", null);

        assertThat(validator.validate(playlist)).isEmpty();
        assertThat(playlist.getDescription()).isNull();
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "   "})
    void shouldRejectBlankName(String name) {
        Playlist playlist = new Playlist(name, "My favorite songs");

        assertThat(validator.validate(playlist))
                .extracting(v -> v.getPropertyPath().toString())
                .containsExactly("name");
    }

    @Test
    void shouldRejectOversizedName() {
        Playlist playlist = new Playlist(
                "a".repeat(256),
                "My favorite songs"
        );

        assertThat(validator.validate(playlist))
                .extracting(v -> v.getPropertyPath().toString())
                .containsExactly("name");
    }

    @Test
    void shouldRejectOversizedDescription() {
        Playlist playlist = new Playlist(
                "My Favorites",
                "a".repeat(1001)
        );

        assertThat(validator.validate(playlist))
                .extracting(v -> v.getPropertyPath().toString())
                .containsExactly("description");
    }

    // Domain behavior tests
    @Test
    void shouldAddSongToPlaylist() {
        Playlist playlist = new Playlist("My Favorites", null);
        Song song = createSong();
        playlist.addSong(song);

        assertThat(playlist.getTracks()).hasSize(1);

        PlaylistTrack track = playlist.getTracks().get(0);

        assertThat(track.getPlaylist()).isSameAs(playlist);
        assertThat(track.getSong()).isSameAs(song);
        assertThat(track.getPosition()).isZero();
    }

    @Test
    void shouldAssignSequentialPositions() {
        Playlist playlist = new Playlist("My Favorites", null);

        Song firstSong = createSong();
        Song secondSong = new Song(
                "Hello", "Adele", "Pop", 295, "music/hello.mp3"
        );
        Song thirdSong = new Song(
                "Fix You", "Coldplay", "Rock", 275, "music/fix-you.mp3"
        );

        playlist.addSong(firstSong);
        playlist.addSong(secondSong);
        playlist.addSong(thirdSong);

        assertThat(playlist.getTracks())
                .extracting(PlaylistTrack::getPosition)
                .containsExactly(0, 1, 2);

        assertThat(playlist.getTracks())
                .extracting(PlaylistTrack::getSong)
                .containsExactly(firstSong, secondSong, thirdSong);
    }

    @Test
    void shouldRejectNullSong() {
        Playlist playlist = new Playlist("My Favorites", null);

        assertThatThrownBy(() -> playlist.addSong(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Song must not be null");

        assertThat(playlist.getTracks()).isEmpty();
    }

    @Test
    void shouldRejectDuplicateSong() {
        Playlist playlist = new Playlist("My Favorites", null);
        Song song = createSong();

        playlist.addSong(song);

        assertThatThrownBy(() -> playlist.addSong(song))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Song already exists in the playlist");

        assertThat(playlist.getTracks()).hasSize(1);
    }

    @Test
    void shouldPreventExternalModificationOfTracks() {
        Playlist playlist = new Playlist("My Favorites", null);
        playlist.addSong(createSong());

        assertThatThrownBy(() -> playlist.getTracks().clear())
                .isInstanceOf(UnsupportedOperationException.class);

        assertThat(playlist.getTracks()).hasSize(1);
    }

    private Song createSong() {
        return new Song(
                "Yellow", "Coldplay", "Rock", 240, "music/yellow.mp3"
        );
    }
}
