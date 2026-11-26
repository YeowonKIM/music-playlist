package com.example.musicplaylist.playlist.service;

import com.example.musicplaylist.common.exception.BusinessException;
import com.example.musicplaylist.common.exception.ErrorCode;
import com.example.musicplaylist.playlist.domain.Playlist;
import com.example.musicplaylist.playlist.domain.PlaylistTrack;
import com.example.musicplaylist.playlist.dto.AddSongRequest;
import com.example.musicplaylist.playlist.dto.CreatePlaylistRequest;
import com.example.musicplaylist.playlist.dto.PlaylistResponse;
import com.example.musicplaylist.playlist.dto.UpdatePlaylistRequest;
import com.example.musicplaylist.playlist.repository.PlaylistRepository;
import com.example.musicplaylist.song.domain.Song;
import com.example.musicplaylist.song.repository.SongRepository;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PlaylistServiceTest {

    @Mock
    private PlaylistRepository playlistRepository;

    @Mock
    private SongRepository songRepository;


    @InjectMocks
    private PlaylistService playlistService;

    @Nested
    class CreatePlaylistTests {

        @Test
        void shouldCreatePlaylist() {
            CreatePlaylistRequest request = new CreatePlaylistRequest("My Favorites", "My favorite songs");

            when(playlistRepository.save(any(Playlist.class)))
                    .thenAnswer(invocation -> {
                        Playlist playlist = invocation.getArgument(0);
                        ReflectionTestUtils.setField(playlist, "id", 1L);
                        return playlist;
                    });

            PlaylistResponse response = playlistService.createPlaylist(request);

            assertThat(response.id()).isEqualTo(1L);
            assertThat(response.name()).isEqualTo("My Favorites");
            assertThat(response.description()).isEqualTo("My favorite songs");

            verify(playlistRepository).save(any(Playlist.class));
        }
    }

    @Nested
    class GetPlaylistTests {

        @Test
        void shouldGetAllPlaylists() {
            Playlist firstPlaylist = createPlaylist(1L, "My Favorites", "My favorite songs");
            Playlist secondPlaylist = createPlaylist(2L, "Workout", "Workout songs");

            when(playlistRepository.findAll())
                    .thenReturn(List.of(firstPlaylist, secondPlaylist));

            List<PlaylistResponse> responses = playlistService.getAllPlaylists();

            assertThat(responses).containsExactly(
                    new PlaylistResponse(1L, "My Favorites", "My favorite songs"),
                    new PlaylistResponse(2L, "Workout", "Workout songs")
            );

            verify(playlistRepository).findAll();
        }

        @Test
        void shouldReturnEmptyListWhenNoPlaylistsExist() {
            when(playlistRepository.findAll())
                    .thenReturn(List.of());

            List<PlaylistResponse> responses = playlistService.getAllPlaylists();

            assertThat(responses).isEmpty();
            verify(playlistRepository).findAll();
        }

        @Test
        void shouldGetPlaylist() {
            Playlist playlist = createPlaylist();

            when(playlistRepository.findById(1L))
                    .thenReturn(Optional.of(playlist));

            PlaylistResponse response = playlistService.getPlaylist(1L);

            assertThat(response.id()).isEqualTo(1L);
            assertThat(response.name()).isEqualTo("My Favorites");
            assertThat(response.description()).isEqualTo("My favorite songs");

            verify(playlistRepository).findById(1L);
        }

        @Test
        void shouldThrowExceptionWhenPlaylistNotFound() {
            when(playlistRepository.findById(999L))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> playlistService.getPlaylist(999L))
                    .isInstanceOfSatisfying(
                            BusinessException.class,
                            exception -> {
                                assertThat(exception.getErrorCode())
                                        .isEqualTo(ErrorCode.PLAYLIST_NOT_FOUND);
                                assertThat(exception.getMessage())
                                        .isEqualTo("Playlist not found");
                            }
                    );

            verify(playlistRepository).findById(999L);
        }
    }

    @Nested
    class UpdatePlaylistTests {

        @Test
        void shouldUpdatePlaylist() {
            Playlist playlist = createPlaylist();

            UpdatePlaylistRequest request = new UpdatePlaylistRequest("Updated Favorites", "Updated description");

            when(playlistRepository.findById(1L))
                    .thenReturn(Optional.of(playlist));

            PlaylistResponse response = playlistService.updatePlaylist(1L, request);

            assertThat(response.id()).isEqualTo(1L);
            assertThat(response.name()).isEqualTo("Updated Favorites");
            assertThat(response.description()).isEqualTo("Updated description");

            assertThat(playlist.getName()).isEqualTo("Updated Favorites");
            assertThat(playlist.getDescription()).isEqualTo("Updated description");

            verify(playlistRepository).findById(1L);
            verify(playlistRepository, never()).save(any(Playlist.class));
        }

        @Test
        void shouldAllowRemovingPlaylistDescription() {
            Playlist playlist = createPlaylist();

            UpdatePlaylistRequest request = new UpdatePlaylistRequest("My Favorites", null);

            when(playlistRepository.findById(1L))
                    .thenReturn(Optional.of(playlist));

            PlaylistResponse response = playlistService.updatePlaylist(1L, request);

            assertThat(response.id()).isEqualTo(1L);
            assertThat(response.name()).isEqualTo("My Favorites");
            assertThat(response.description()).isNull();

            assertThat(playlist.getDescription()).isNull();

            verify(playlistRepository).findById(1L);
            verify(playlistRepository, never()).save(any(Playlist.class));
        }

        @Test
        void shouldThrowExceptionWhenUpdatingNonexistentPlaylist() {
            UpdatePlaylistRequest request = new UpdatePlaylistRequest("Updated Favorites", "Updated description");

            when(playlistRepository.findById(999L))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() ->
                    playlistService.updatePlaylist(999L, request))
                    .isInstanceOfSatisfying(
                            BusinessException.class,
                            exception -> {
                                assertThat(exception.getErrorCode())
                                        .isEqualTo(ErrorCode.PLAYLIST_NOT_FOUND);
                                assertThat(exception.getMessage())
                                        .isEqualTo("Playlist not found");
                            }
                    );

            verify(playlistRepository).findById(999L);
            verify(playlistRepository, never()).save(any(Playlist.class));
        }
    }

    @Nested
    class DeletePlaylistTests {

        @Test
        void shouldDeletePlaylist() {
            Playlist playlist = createPlaylist();

            when(playlistRepository.findById(1L))
                    .thenReturn(Optional.of(playlist));

            playlistService.deletePlaylist(1L);

            verify(playlistRepository).findById(1L);
            verify(playlistRepository).delete(playlist);
        }

        @Test
        void shouldThrowExceptionWhenDeletingNonexistentPlaylist() {
            when(playlistRepository.findById(999L))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() ->
                    playlistService.deletePlaylist(999L))
                    .isInstanceOfSatisfying(
                            BusinessException.class,
                            exception -> {
                                assertThat(exception.getErrorCode())
                                        .isEqualTo(ErrorCode.PLAYLIST_NOT_FOUND);
                                assertThat(exception.getMessage())
                                        .isEqualTo("Playlist not found");
                            }
                    );

            verify(playlistRepository).findById(999L);
            verify(playlistRepository, never()).delete(any(Playlist.class));
        }
    }

    // Song addition & removal tests
    @Nested
    class AddSongTests {

        @Test
        void shouldAddSongToPlaylist() {
            Playlist playlist = createPlaylist();
            Song song = createSong(1L);
            AddSongRequest request = new AddSongRequest(1L);

            when(playlistRepository.findById(1L))
                    .thenReturn(Optional.of(playlist));

            when(songRepository.findById(1L))
                    .thenReturn(Optional.of(song));

            playlistService.addSongToPlaylist(1L, request);

            assertThat(playlist.getTracks())
                    .extracting(PlaylistTrack::getSong)
                    .containsExactly(song);

            verify(playlistRepository).findById(1L);
            verify(songRepository).findById(1L);
            verify(playlistRepository, never()).save(any(Playlist.class));
        }

        @Test
        void shouldRejectAdditionWhenPlaylistNotFound() {
            AddSongRequest request = new AddSongRequest(1L);

            when(playlistRepository.findById(999L))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() ->
                    playlistService.addSongToPlaylist(999L, request))
                    .isInstanceOfSatisfying(
                            BusinessException.class,
                            exception -> {
                                assertThat(exception.getErrorCode())
                                        .isEqualTo(ErrorCode.PLAYLIST_NOT_FOUND);
                                assertThat(exception.getMessage())
                                        .isEqualTo(ErrorCode.PLAYLIST_NOT_FOUND.getMessage());
                            }
                    );

            verifyNoInteractions(songRepository);
        }

        @Test
        void shouldRejectAdditionWhenSongNotFound() {
            Playlist playlist = createPlaylist();
            AddSongRequest request = new AddSongRequest(999L);

            when(playlistRepository.findById(1L))
                    .thenReturn(Optional.of(playlist));

            when(songRepository.findById(999L))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() ->
                    playlistService.addSongToPlaylist(1L, request))
                    .isInstanceOfSatisfying(
                            BusinessException.class,
                            exception -> {
                                assertThat(exception.getErrorCode())
                                        .isEqualTo(ErrorCode.SONG_NOT_FOUND);
                                assertThat(exception.getMessage())
                                        .isEqualTo(ErrorCode.SONG_NOT_FOUND.getMessage());
                            }
                    );

            assertThat(playlist.getTracks()).isEmpty();
        }

        @Test
        void shouldRejectDuplicateSong() {
            Playlist playlist = createPlaylist();
            Song song = createSong(1L);
            AddSongRequest request = new AddSongRequest(1L);

            playlist.addSong(song);

            when(playlistRepository.findById(1L))
                    .thenReturn(Optional.of(playlist));

            when(songRepository.findById(1L))
                    .thenReturn(Optional.of(song));

            assertThatThrownBy(() ->
                    playlistService.addSongToPlaylist(1L, request))
                    .isInstanceOfSatisfying(
                            BusinessException.class,
                            exception -> {
                                assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.DUPLICATE_SONG);
                                assertThat(exception.getMessage()).isEqualTo(ErrorCode.DUPLICATE_SONG.getMessage());
                            }
                    );

            assertThat(playlist.getTracks())
                    .extracting(PlaylistTrack::getSong)
                    .containsExactly(song);
        }

        @Nested
        class RemoveSongTests {

            @Test
            void shouldRemoveSongFromPlaylist() {
                Playlist playlist = createPlaylist();
                Song song = createSong(1L);

                playlist.addSong(song);

                when(playlistRepository.findById(1L))
                        .thenReturn(Optional.of(playlist));

                playlistService.removeSongFromPlaylist(1L, 1L);

                assertThat(playlist.getTracks()).isEmpty();

                verify(playlistRepository).findById(1L);
                verifyNoInteractions(songRepository);
                verify(playlistRepository, never()).save(any(Playlist.class));
            }

            @Test
            void shouldRejectRemovalWhenPlaylistNotFound() {
                when(playlistRepository.findById(999L))
                        .thenReturn(Optional.empty());

                assertThatThrownBy(() ->
                        playlistService.removeSongFromPlaylist(999L, 1L))
                        .isInstanceOfSatisfying(
                                BusinessException.class,
                                exception -> {
                                    assertThat(exception.getErrorCode())
                                            .isEqualTo(ErrorCode.PLAYLIST_NOT_FOUND);
                                    assertThat(exception.getMessage())
                                            .isEqualTo(ErrorCode.PLAYLIST_NOT_FOUND.getMessage());
                                }
                        );

                verifyNoInteractions(songRepository);
            }

            @Test
            void shouldRejectRemovalWhenSongNotInPlaylist() {
                Playlist playlist = createPlaylist();
                Song song = createSong(1L);

                playlist.addSong(song);

                when(playlistRepository.findById(1L))
                        .thenReturn(Optional.of(playlist));

                assertThatThrownBy(() ->
                        playlistService.removeSongFromPlaylist(1L, 999L))
                        .isInstanceOfSatisfying(
                                BusinessException.class,
                                exception -> {
                                    assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.SONG_NOT_IN_PLAYLIST);
                                    assertThat(exception.getMessage()).isEqualTo(ErrorCode.SONG_NOT_IN_PLAYLIST.getMessage());
                                }
                        );

                assertThat(playlist.getTracks())
                        .extracting(PlaylistTrack::getSong)
                        .containsExactly(song);

                verifyNoInteractions(songRepository);
            }
        }
    }

    private Playlist createPlaylist() {
        return createPlaylist(1L, "My Favorites", "My favorite songs");
    }

    private Playlist createPlaylist(Long id, String name, String description) {
        Playlist playlist = Playlist.of(name, description);
        ReflectionTestUtils.setField(playlist, "id", id);
        return playlist;
    }

    private Song createSong(Long id) {
        Song song = Song.builder()
                .title("Yellow")
                .artist("Coldplay")
                .genre("Rock")
                .durationSeconds(240)
                .uri("music/yellow.mp3")
                .build();

        ReflectionTestUtils.setField(song, "id", id);

        return song;
    }
}