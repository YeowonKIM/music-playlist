package com.example.musicplaylist.song.domain;

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

public class SongTest {
    private static ValidatorFactory validatorFactory;
    private static Validator validator;

    @BeforeAll
    static void setUp() {
        validatorFactory = Validation.buildDefaultValidatorFactory();
        validator = validatorFactory.getValidator();
    }

    @AfterAll
    static void teatDown() {
        validatorFactory.close();
    }

    @Test
    void shouldCreateValidSong() {
        Song song = validSongBuilder().build();

        assertThat(validator.validate(song)).isEmpty();
        assertThat(song.getId()).isNull();
        assertThat(song.getTitle()).isEqualTo("Yellow");
        assertThat(song.getArtist()).isEqualTo("Coldplay");
        assertThat(song.getGenre()).isEqualTo("Rock");
        assertThat(song.getDurationSeconds()).isEqualTo(240);
        assertThat(song.getUri()).isEqualTo("music/yellow.mp3");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "   "})
    void shouldRejectBlankTitle(String title) {
        Song song = validSongBuilder()
                .title(title)
                .build();

        assertThat(validator.validate(song))
                .extracting(v -> v.getPropertyPath().toString())
                .containsExactly("title");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "   "})
    void shouldRejectBlankArtist(String artist) {
        Song song = validSongBuilder()
                .artist(artist)
                .build();

        assertThat(validator.validate(song))
                .extracting(v -> v.getPropertyPath().toString())
                .containsExactly("artist");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "   "})
    void shouldRejectBlankGenre(String genre) {
        Song song = validSongBuilder()
                .genre(genre)
                .build();

        assertThat(validator.validate(song))
                .extracting(v -> v.getPropertyPath().toString())
                .containsExactly("genre");
    }

    @ParameterizedTest
    @ValueSource(ints = {0, -1, -100})
    void shouldRejectNonPositiveDuration(int durationSeconds) {
        Song song = validSongBuilder()
                .durationSeconds(durationSeconds)
                .build();

        assertThat(validator.validate(song))
                .extracting(v -> v.getPropertyPath().toString())
                .containsExactly("durationSeconds");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "   "})
    void shouldRejectBlankUri(String uri) {
        Song song = validSongBuilder()
                .uri(uri)
                .build();

        assertThat(validator.validate(song))
                .extracting(v -> v.getPropertyPath().toString())
                .containsExactly("uri");
    }

    @Test
    void shouldRejectOversizedFields() {
        Song song = validSongBuilder()
                .title("a".repeat(256))
                .artist("a".repeat(256))
                .genre("a".repeat(256))
                .uri("a".repeat(2049))
                .build();

        assertThat(validator.validate(song))
                .extracting(v -> v.getPropertyPath().toString())
                .containsExactlyInAnyOrder(
                        "title", "artist", "genre", "uri"
                );
    }

    private Song.SongBuilder validSongBuilder() {
        return Song.builder()
                .title("Yellow")
                .artist("Coldplay")
                .genre("Rock")
                .durationSeconds(240)
                .uri("music/yellow.mp3");
    }
}
