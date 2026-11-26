package com.example.musicplaylist.playlist.domain;

import com.example.musicplaylist.common.exception.BusinessException;
import com.example.musicplaylist.common.exception.ErrorCode;
import com.example.musicplaylist.song.domain.Song;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Nested;
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

    @Nested
    class BeanValidationTests {

        @Test
        void shouldCreateValidPlaylist() {
            Playlist playlist = Playlist.of("My Favorites", "My favorite songs");

            assertThat(validator.validate(playlist)).isEmpty();
            assertThat(playlist.getId()).isNull();
            assertThat(playlist.getName()).isEqualTo("My Favorites");
            assertThat(playlist.getDescription()).isEqualTo("My favorite songs");
            assertThat(playlist.getTracks()).isEmpty();
        }

        @Test
        void shouldAllowNullDescription() {
            Playlist playlist = createPlaylist();

            assertThat(validator.validate(playlist)).isEmpty();
            assertThat(playlist.getDescription()).isNull();
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {" ", "   "})
        void shouldRejectBlankName(String name) {
            Playlist playlist = Playlist.of(name, "My favorite songs");

            assertThat(validator.validate(playlist))
                    .extracting(v -> v.getPropertyPath().toString())
                    .containsExactly("name");
        }

        @Test
        void shouldRejectOversizedName() {
            Playlist playlist = Playlist.of("a".repeat(256), "My favorite songs");

            assertThat(validator.validate(playlist))
                    .extracting(v -> v.getPropertyPath().toString())
                    .containsExactly("name");
        }

        @Test
        void shouldRejectOversizedDescription() {
            Playlist playlist = Playlist.of("My Favorites", "a".repeat(1001));

            assertThat(validator.validate(playlist))
                    .extracting(v -> v.getPropertyPath().toString())
                    .containsExactly("description");
        }
    }

    @Nested
    class MetadataUpdateTests {

        @Test
        void shouldRenamePlaylist() {
            Playlist playlist = Playlist.of("Old Name", "Description");

            playlist.rename("New Name");

            assertThat(playlist.getName()).isEqualTo("New Name");
            assertThat(playlist.getDescription()).isEqualTo("Description");
            assertThat(validator.validate(playlist)).isEmpty();
        }

        @Test
        void shouldUpdatePlaylistDescription() {
            Playlist playlist = Playlist.of("My Favorites", "Old Description");

            playlist.updateDescription("New Description");

            assertThat(playlist.getName()).isEqualTo("My Favorites");
            assertThat(playlist.getDescription()).isEqualTo("New Description");
            assertThat(validator.validate(playlist)).isEmpty();
        }

        @Test
        void shouldAllowRemovingPlaylistDescription() {
            Playlist playlist = Playlist.of("My Favorites", "Description");

            playlist.updateDescription(null);

            assertThat(playlist.getDescription()).isNull();
            assertThat(validator.validate(playlist)).isEmpty();
        }

        @Test
        void shouldPreserveTracksWhenUpdatingMetadata() {
            Playlist playlist = createPlaylist();
            Song song = createSong();

            playlist.addSong(song);
            PlaylistTrack originalTrack = playlist.getTracks().get(0);

            playlist.rename("New Name");
            playlist.updateDescription("New Description");

            assertThat(playlist.getTracks()).hasSize(1);

            PlaylistTrack track = playlist.getTracks().get(0);

            assertThat(track).isSameAs(originalTrack);
            assertThat(track.getPlaylist()).isSameAs(playlist);
            assertThat(track.getSong()).isSameAs(song);
            assertThat(track.getPosition()).isZero();
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {" ", "   "})
        void shouldDetectBlankNameAfterRename(String name) {
            Playlist playlist = createPlaylist();

            playlist.rename(name);

            assertThat(validator.validate(playlist))
                    .extracting(v -> v.getPropertyPath().toString())
                    .containsExactly("name");
        }

        @Test
        void shouldDetectOversizedNameAfterRename() {
            Playlist playlist = createPlaylist();

            playlist.rename("a".repeat(256));

            assertThat(validator.validate(playlist))
                    .extracting(v -> v.getPropertyPath().toString())
                    .containsExactly("name");
        }

        @Test
        void shouldDetectOversizedDescriptionAfterUpdate() {
            Playlist playlist = createPlaylist();

            playlist.updateDescription("a".repeat(1001));

            assertThat(validator.validate(playlist))
                    .extracting(v -> v.getPropertyPath().toString())
                    .containsExactly("description");
        }
    }

    @Nested
    class TrackManagementTests {

        @Test
        void shouldAddSongToPlaylist() {
            Playlist playlist = createPlaylist();
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
            Playlist playlist = createPlaylist();

            Song firstSong = createSong();
            Song secondSong = new Song("Hello", "Adele", "Pop", 295, "music/hello.mp3");
            Song thirdSong = new Song("Fix You", "Coldplay", "Rock", 275, "music/fix-you.mp3");

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
            Playlist playlist = createPlaylist();

            assertThatThrownBy(() -> playlist.addSong(null))
                    .isInstanceOfSatisfying(
                            BusinessException.class,
                            exception -> {
                                assertThat(exception.getErrorCode())
                                        .isEqualTo(ErrorCode.INVALID_SONG);
                                assertThat(exception.getMessage())
                                        .isEqualTo("Song must not be null");
                            }
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
                            exception -> {
                                assertThat(exception.getErrorCode())
                                        .isEqualTo(ErrorCode.DUPLICATE_SONG);
                                assertThat(exception.getMessage())
                                        .isEqualTo(
                                                "Song already exists in the playlist"
                                        );
                            }
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
    }

    private Playlist createPlaylist() {
        return Playlist.of("My Favorites", null);
    }

    private Song createSong() {
        return new Song("Yellow", "Coldplay", "Rock", 240, "music/yellow.mp3");
    }
}