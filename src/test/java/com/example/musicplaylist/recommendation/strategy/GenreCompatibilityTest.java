package com.example.musicplaylist.recommendation.strategy;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class GenreCompatibilityTest {

    private final GenreCompatibility genreCompatibility =
            new GenreCompatibility();

    @Test
    void shouldReturnFullCompatibilityForSameGenre() {
        assertThat(genreCompatibility.getCompatibility("Rock", "Rock")).isEqualTo(1.0);
        assertThat(genreCompatibility.getCompatibility("Reggae", "Reggae")).isEqualTo(1.0);
    }

    @Test
    void shouldReturnSymmetricCompatibilityForRelatedGenres() {
        assertThat(genreCompatibility.getCompatibility("Rock", "Pop")).isEqualTo(0.8);
        assertThat(genreCompatibility.getCompatibility("Pop", "Rock")).isEqualTo(0.8);
    }

    @Test
    void shouldReturnZeroForUndefinedGenreRelationships() {
        assertThat(genreCompatibility.getCompatibility("Rock", "Classical")).isZero();
        assertThat(genreCompatibility.getCompatibility("Rock", "Reggae")).isZero();
    }
}