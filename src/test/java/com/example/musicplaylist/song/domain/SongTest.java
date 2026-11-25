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
        Song song = new Song("Yellow", "Coldplay", "Rock", 240, "music/yellow.mp3");

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
        Song song = new Song(title, "Coldplay", "Rock", 240, "music/yellow.mp3");

        assertThat(validator.validate(song))
                .extracting(v -> v.getPropertyPath().toString())
                .containsExactly("title");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "   "})
    void shouldRejectBlankArtist(String artist) {
        Song song = new Song("Yellow", artist, "Rock", 240, "music/yellow.mp3");

        assertThat(validator.validate(song))
                .extracting(v -> v.getPropertyPath().toString())
                .containsExactly("artist");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "   "})
    void shouldRejectBlankGenre(String genre) {
        Song song = new Song("Yellow", "Coldplay", genre, 240, "music/yellow.mp3");

        assertThat(validator.validate(song))
                .extracting(v -> v.getPropertyPath().toString())
                .containsExactly("genre");
    }

    @ParameterizedTest
    @ValueSource(ints = {0, -1, -100})
    void shouldRejectNonPositiveDuration(int durationSeconds) {
        Song song = new Song("Yellow", "Coldplay", "Rock", durationSeconds, "music/yellow.mp3");

        assertThat(validator.validate(song))
                .extracting(v -> v.getPropertyPath().toString())
                .containsExactly("durationSeconds");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "   "})
    void shouldRejectBlankUri(String uri) {
        Song song = new Song("Yellow", "Coldplay", "Rock", 240, uri);

        assertThat(validator.validate(song))
                .extracting(v -> v.getPropertyPath().toString())
                .containsExactly("uri");
    }

    @Test
    void shouldRejectOversizedFields() {
        Song song = new Song(
                "a".repeat(256),
                "a".repeat(256),
                "a".repeat(256),
                240,
                "a".repeat(2049)
        );

        assertThat(validator.validate(song))
                .extracting(v -> v.getPropertyPath().toString())
                .containsExactlyInAnyOrder(
                        "title", "artist", "genre", "uri"
                );
    }
}
