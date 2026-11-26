package com.example.musicplaylist.shuffle.service;

import com.example.musicplaylist.common.exception.BusinessException;
import com.example.musicplaylist.common.exception.ErrorCode;
import com.example.musicplaylist.playlist.domain.Playlist;
import com.example.musicplaylist.playlist.repository.PlaylistRepository;
import com.example.musicplaylist.shuffle.strategy.ShuffleStrategy;
import com.example.musicplaylist.song.domain.Song;
import com.example.musicplaylist.song.dto.SongResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ShuffleServiceTest {

    @Mock
    private PlaylistRepository playlistRepository;

    @Mock
    private ShuffleStrategyRegistry shuffleStrategyRegistry;

    @Mock
    private ShuffleStrategy shuffleStrategy;

    @InjectMocks
    private ShuffleService shuffleService;

    @Test
    void shouldShufflePlaylistUsingSelectedStrategy() {
        Song firstSong = createSong("Yellow", "Coldplay");
        Song secondSong = createSong("Hello", "Adele");

        Playlist playlist = Playlist.of("My Favorites", "Favorite songs");

        playlist.addSong(firstSong);
        playlist.addSong(secondSong);

        when(shuffleStrategyRegistry.getStrategy("smart"))
                .thenReturn(shuffleStrategy);

        when(playlistRepository.findByIdWithTracks(1L))
                .thenReturn(Optional.of(playlist));

        when(shuffleStrategy.shuffle(List.of(firstSong, secondSong)))
                .thenReturn(List.of(secondSong, firstSong));

        List<SongResponse> response = shuffleService.shufflePlaylist(1L, "smart");

        assertThat(response)
                .containsExactly(
                        SongResponse.from(secondSong),
                        SongResponse.from(firstSong)
                );

        assertThat(playlist.getTracks())
                .extracting(track -> track.getSong())
                .containsExactly(firstSong, secondSong);

        verify(shuffleStrategyRegistry).getStrategy("smart");
        verify(shuffleStrategy).shuffle(List.of(firstSong, secondSong));
    }

    @Test
    void shouldThrowExceptionWhenPlaylistDoesNotExist() {
        when(shuffleStrategyRegistry.getStrategy("random"))
                .thenReturn(shuffleStrategy);

        when(playlistRepository.findByIdWithTracks(1L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> shuffleService.shufflePlaylist(1L, "random"))
                .isInstanceOfSatisfying(
                        BusinessException.class,
                        exception -> assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.PLAYLIST_NOT_FOUND)
                );

        verifyNoInteractions(shuffleStrategy);
    }

    @Test
    void shouldRejectInvalidStrategyWithoutLoadingPlaylist() {
        when(shuffleStrategyRegistry.getStrategy("unknown"))
                .thenThrow(new BusinessException(ErrorCode.INVALID_SHUFFLE_STRATEGY));

        assertThatThrownBy(() -> shuffleService.shufflePlaylist(1L, "unknown"))
                .isInstanceOfSatisfying(
                        BusinessException.class,
                        exception -> assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.INVALID_SHUFFLE_STRATEGY)
                );

        verifyNoInteractions(playlistRepository, shuffleStrategy);
    }

    private Song createSong(String title, String artist) {
        return Song.builder()
                .title(title)
                .artist(artist)
                .genre("Rock")
                .durationSeconds(240)
                .uri("music/sample.mp3")
                .build();
    }
}