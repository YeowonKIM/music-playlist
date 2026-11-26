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

            assertThat(response).isEqualTo(new PlaylistResponse(1L, "My Favorites", "My favorite songs"));

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
        }

        @Test
        void shouldGetPlaylist() {
            Playlist playlist = createPlaylist();

            when(playlistRepository.findById(1L))
                    .thenReturn(Optional.of(playlist));

            PlaylistResponse response = playlistService.getPlaylist(1L);

            assertThat(response).isEqualTo(new PlaylistResponse(1L, "My Favorites", "My favorite songs"));
        }

        @Test
        void shouldThrowExceptionWhenPlaylistNotFound() {
            when(playlistRepository.findById(999L))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> playlistService.getPlaylist(999L))
                    .isInstanceOfSatisfying(
                            BusinessException.class,
                            exception -> assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.PLAYLIST_NOT_FOUND)
                    );
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

            assertThat(response).isEqualTo(new PlaylistResponse(1L, "Updated Favorites", "Updated description"));

            assertThat(playlist.getName()).isEqualTo("Updated Favorites");
            assertThat(playlist.getDescription()).isEqualTo("Updated description");
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

            verify(playlistRepository).delete(playlist);
        }
    }

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
                            exception -> assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.PLAYLIST_NOT_FOUND)
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
                            exception -> assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.SONG_NOT_FOUND)
                    );

            assertThat(playlist.getTracks()).isEmpty();
        }
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

            verifyNoInteractions(songRepository);
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