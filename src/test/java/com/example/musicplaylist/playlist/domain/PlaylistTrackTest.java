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

    @Test
    void shouldRejectInvalidPlaylistTrack() {
        PlaylistTrack track = PlaylistTrack.of(null, null, -1);

        assertThat(validator.validate(track))
                .extracting(v -> v.getPropertyPath().toString())
                .containsExactlyInAnyOrder(
                        "playlist", "song", "position"
                );
    }

    // Update position tests
    @Test
    void shouldUpdatePositionToZero() {
        Playlist playlist = Playlist.of("My Playlist", "Description");
        PlaylistTrack track = PlaylistTrack.of(playlist, createSong(), 3);

        track.updatePosition(0);

        assertThat(track.getPosition()).isZero();
    }

    private Song createSong() {
        return Song.builder()
                .title("Yellow")
                .artist("Coldplay")
                .genre("Rock")
                .durationSeconds(240)
                .uri("music/yellow.mp3")
                .build();
    }
}