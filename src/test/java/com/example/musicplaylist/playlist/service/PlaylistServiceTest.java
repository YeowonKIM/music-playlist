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

            when(playlistRepository.findById(999L)).thenReturn(Optional.empty());

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

            when(playlistRepository.findById(1L)).thenReturn(Optional.of(playlist));
            when(songRepository.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() ->
                    playlistService.addSongToPlaylist(1L, request))
                    .isInstanceOfSatisfying(
                            BusinessException.class,
                            exception -> assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.SONG_NOT_FOUND)
                    );

            assertThat(playlist.getTracks()).isEmpty();
        }
    }

    private Playlist createPlaylist() {
        Playlist playlist = Playlist.of("My Favorites", "My favorite songs");
        ReflectionTestUtils.setField(playlist, "id", 1L);
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