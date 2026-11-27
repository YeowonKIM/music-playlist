package com.example.musicplaylist.player.service;

import com.example.musicplaylist.common.exception.BusinessException;
import com.example.musicplaylist.common.exception.ErrorCode;
import com.example.musicplaylist.player.domain.MusicPlayer;
import com.example.musicplaylist.player.domain.PlaybackStatus;
import com.example.musicplaylist.player.dto.PlaybackStateResponse;
import com.example.musicplaylist.player.dto.PlayRequest;
import com.example.musicplaylist.playlist.domain.Playlist;
import com.example.musicplaylist.playlist.repository.PlaylistRepository;
import com.example.musicplaylist.shuffle.service.ShuffleStrategyRegistry;
import com.example.musicplaylist.shuffle.strategy.ShuffleStrategy;
import com.example.musicplaylist.song.domain.Song;
import com.example.musicplaylist.song.dto.SongResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PlayerServiceTest {

    @Mock
    private PlaylistRepository playlistRepository;

    @Mock
    private ShuffleStrategyRegistry shuffleStrategyRegistry;

    @Mock
    private ShuffleStrategy shuffleStrategy;

    private MusicPlayer musicPlayer;
    private PlayerService playerService;

    private Playlist playlist;
    private Song yellow;
    private Song hello;

    @BeforeEach
    void setUp() {
        musicPlayer = new MusicPlayer();

        playerService = new PlayerService(playlistRepository, shuffleStrategyRegistry, musicPlayer);

        yellow = Song.builder()
                .title("Yellow")
                .artist("Coldplay")
                .genre("Rock")
                .durationSeconds(240)
                .uri("music/yellow.mp3")
                .build();

        hello = Song.builder()
                .title("Hello")
                .artist("Adele")
                .genre("Pop")
                .durationSeconds(295)
                .uri("music/hello.mp3")
                .build();

        ReflectionTestUtils.setField(yellow, "id", 1L);
        ReflectionTestUtils.setField(hello, "id", 2L);

        playlist = Playlist.of("My Favorites", "Favorite songs");

        ReflectionTestUtils.setField(playlist, "id", 1L);

        playlist.addSong(yellow);
        playlist.addSong(hello);
    }

    @Test
    void shouldPlayPlaylistInOriginalOrder() {
        when(playlistRepository.findByIdWithTracks(1L))
                .thenReturn(Optional.of(playlist));

        PlaybackStateResponse response = playerService.play(new PlayRequest(1L, null));

        assertThat(response.playlistId()).isEqualTo(1L);
        assertThat(response.status()).isEqualTo(PlaybackStatus.PLAYING);
        assertThat(response.currentIndex()).isZero();
        assertThat(response.currentSong()).isEqualTo(SongResponse.from(yellow));
        assertThat(response.playbackQueue()).containsExactly(
                SongResponse.from(yellow),
                SongResponse.from(hello)
        );
        assertThat(response.shuffleStrategy()).isNull();

        verifyNoInteractions(shuffleStrategyRegistry);
    }

    @Test
    void shouldPlayPlaylistUsingSelectedShuffleStrategy() {
        when(playlistRepository.findByIdWithTracks(1L))
                .thenReturn(Optional.of(playlist));

        when(shuffleStrategyRegistry.getStrategy("smart"))
                .thenReturn(shuffleStrategy);

        when(shuffleStrategy.shuffle(List.of(yellow, hello)))
                .thenReturn(List.of(hello, yellow));

        PlaybackStateResponse response = playerService.play(
                new PlayRequest(1L, "smart")
        );

        assertThat(response.status()).isEqualTo(PlaybackStatus.PLAYING);
        assertThat(response.currentIndex()).isZero();
        assertThat(response.currentSong()).isEqualTo(SongResponse.from(hello));
        assertThat(response.playbackQueue()).containsExactly(
                SongResponse.from(hello),
                SongResponse.from(yellow)
        );
        assertThat(response.shuffleStrategy()).isEqualTo("smart");

        verify(shuffleStrategyRegistry).getStrategy("smart");
        verify(shuffleStrategy).shuffle(List.of(yellow, hello));
    }

    @Test
    void shouldPreservePlaybackStateWhenPlaylistIsNotFound() {
        musicPlayer.play(1L, List.of(SongResponse.from(yellow)), null);

        PlaybackStateResponse original = playerService.getState();

        when(playlistRepository.findByIdWithTracks(99L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> playerService.play(new PlayRequest(99L, null)))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.PLAYLIST_NOT_FOUND);

        assertThat(playerService.getState()).isEqualTo(original);

        verifyNoInteractions(shuffleStrategyRegistry);
    }
}