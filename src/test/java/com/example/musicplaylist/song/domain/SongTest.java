package com.example.musicplaylist.song.domain;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

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
    static void tearDown() {
        validatorFactory.close();
    }

    @Test
    void shouldCreateValidSong() {
        Song song = validSongBuilder().build();

        assertThat(validator.validate(song)).isEmpty();
    }

    @Test
    void shouldRejectInvalidSong() {
        Song song = validSongBuilder()
                .title("")
                .artist(null)
                .genre(" ")
                .durationSeconds(0)
                .uri("")
                .build();

        assertThat(validator.validate(song))
                .extracting(v -> v.getPropertyPath().toString())
                .containsExactlyInAnyOrder(
                        "title", "artist", "genre", "durationSeconds", "uri"
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