package com.example.musicplaylist.playlist.domain;

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
}
