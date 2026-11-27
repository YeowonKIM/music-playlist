package com.example.musicplaylist.playlist.domain;

import com.example.musicplaylist.common.exception.BusinessException;
import com.example.musicplaylist.common.exception.ErrorCode;
import com.example.musicplaylist.song.domain.Song;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.*;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

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

    @Nested
    class BeanValidationTests {

        @Test
        void shouldCreateValidPlaylist() {
            Playlist playlist = createPlaylist();

            assertThat(validator.validate(playlist)).isEmpty();
            assertThat(playlist.getTracks()).isEmpty();
        }

        @Test
        void shouldRejectBlankName() {
            Playlist playlist = Playlist.of(" ", "Description");

            assertThat(validator.validate(playlist))
                    .extracting(v -> v.getPropertyPath().toString())
                    .containsExactly("name");
        }
    }

    @Nested
    class MetadataUpdateTests {

        @Test
        void shouldPreserveTracksWhenUpdatingMetadata() {
            Playlist playlist = createPlaylist();
            Song song = createSong();

            playlist.addSong(song);
            PlaylistTrack originalTrack = playlist.getTracks().get(0);

            playlist.rename("New Name");
            playlist.updateDescription("New Description");

            assertThat(playlist.getName()).isEqualTo("New Name");
            assertThat(playlist.getDescription()).isEqualTo("New Description");
            assertThat(playlist.getTracks()).containsExactly(originalTrack);
        }
    }

    @Nested
    class TrackManagementTests {

        @Test
        void shouldAssignSequentialPositions() {
            Playlist playlist = createPlaylist();

            Song firstSong = createSong(1L);
            Song secondSong = createSong(2L);
            Song thirdSong = createSong(3L);

            playlist.addSong(firstSong);
            playlist.addSong(secondSong);
            playlist.addSong(thirdSong);

            assertThat(playlist.getTracks())
                    .extracting(PlaylistTrack::getSong)
                    .containsExactly(firstSong, secondSong, thirdSong);

            assertThat(playlist.getTracks())
                    .extracting(PlaylistTrack::getPosition)
                    .containsExactly(0, 1, 2);
        }

        @Test
        void shouldRejectNullSong() {
            Playlist playlist = createPlaylist();

            assertThatThrownBy(() -> playlist.addSong(null))
                    .isInstanceOfSatisfying(
                            BusinessException.class,
                            exception -> assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.INVALID_SONG)
                    );

            assertThat(playlist.getTracks()).isEmpty();
        }

        @Test
        void shouldRejectDuplicateSong() {
            Playlist playlist = createPlaylist();
            Song song = createSong();

            playlist.addSong(song);

            assertThatThrownBy(() -> playlist.addSong(song))
                    .isInstanceOfSatisfying(
                            BusinessException.class,
                            exception -> assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.DUPLICATE_SONG)
                    );

            assertThat(playlist.getTracks()).hasSize(1);
        }

        @Test
        void shouldPreventExternalModificationOfTracks() {
            Playlist playlist = createPlaylist();
            playlist.addSong(createSong());

            assertThatThrownBy(() -> playlist.getTracks().clear())
                    .isInstanceOf(UnsupportedOperationException.class);

            assertThat(playlist.getTracks()).hasSize(1);
        }

        @Test
        void shouldAppendSongAfterRemoval() {
            Playlist playlist = createPlaylist();

            Song firstSong = createSong(1L);
            Song secondSong = createSong(2L);
            Song thirdSong = createSong(3L);
            Song newSong = createSong(4L);

            playlist.addSong(firstSong);
            playlist.addSong(secondSong);
            playlist.addSong(thirdSong);

            playlist.removeSong(2L);
            playlist.addSong(newSong);

            assertThat(playlist.getTracks())
                    .extracting(PlaylistTrack::getSong)
                    .containsExactly(firstSong, thirdSong, newSong);

            assertThat(playlist.getTracks())
                    .extracting(PlaylistTrack::getPosition)
                    .containsExactly(0, 1, 2);
        }

        @Test
        void shouldRejectSongRemovalNotInPlaylist() {
            Playlist playlist = createPlaylist();
            Song song = createSong(1L);

            playlist.addSong(song);

            assertThatThrownBy(() -> playlist.removeSong(999L))
                    .isInstanceOfSatisfying(
                            BusinessException.class,
                            exception -> assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.SONG_NOT_IN_PLAYLIST)
                    );

            assertThat(playlist.getTracks())
                    .extracting(PlaylistTrack::getSong)
                    .containsExactly(song);

            assertThat(playlist.getTracks())
                    .extracting(PlaylistTrack::getPosition)
                    .containsExactly(0);
        }
    }

    @Nested
    class MoveSongTests {

        private Playlist playlist;

        @BeforeEach
        void setUp() {
            playlist = createPlaylist();

            playlist.addSong(createSong(1L));
            playlist.addSong(createSong(2L));
            playlist.addSong(createSong(3L));
            playlist.addSong(createSong(4L));
            playlist.addSong(createSong(5L));
        }

        @Test
        void shouldMoveSongForward() {
            playlist.moveSong(2L, 4);

            assertTrackOrder(1L, 3L, 4L, 5L, 2L);
        }

        @Test
        void shouldMoveSongBackward() {
            playlist.moveSong(4L, 0);

            assertTrackOrder(4L, 1L, 2L, 3L, 5L);
        }

        @Test
        void shouldKeepOrderWhenPositionIsUnchanged() {
            playlist.moveSong(3L, 2);

            assertTrackOrder(1L, 2L, 3L, 4L, 5L);
        }

        @Test
        void shouldRejectInvalidPosition() {
            assertThatThrownBy(() -> playlist.moveSong(2L, 5))
                    .isInstanceOfSatisfying(
                            BusinessException.class,
                            exception -> assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.INVALID_POSITION)
                    );

            assertTrackOrder(1L, 2L, 3L, 4L, 5L);
        }

        @Test
        void shouldRejectSongNotInPlaylist() {
            assertThatThrownBy(() -> playlist.moveSong(999L, 0))
                    .isInstanceOfSatisfying(
                            BusinessException.class,
                            exception -> assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.SONG_NOT_IN_PLAYLIST)
                    );

            assertTrackOrder(1L, 2L, 3L, 4L, 5L);
        }

        private void assertTrackOrder(Long... expectedSongIds) {
            List<PlaylistTrack> tracks = playlist.getTracks();

            assertThat(tracks)
                    .extracting(track -> track.getSong().getId())
                    .containsExactly(expectedSongIds);

            assertThat(tracks)
                    .extracting(PlaylistTrack::getPosition)
                    .containsExactly(0, 1, 2, 3, 4);
        }
    }

    private Playlist createPlaylist() {
        return Playlist.of("My Favorites", null);
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

    private Song createSong(Long id) {
        Song song = createSong();
        ReflectionTestUtils.setField(song, "id", id);
        return song;
    }
}