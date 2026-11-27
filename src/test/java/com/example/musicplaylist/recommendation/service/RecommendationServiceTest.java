package com.example.musicplaylist.recommendation.service;

import com.example.musicplaylist.common.exception.BusinessException;
import com.example.musicplaylist.common.exception.ErrorCode;
import com.example.musicplaylist.playlist.domain.Playlist;
import com.example.musicplaylist.playlist.repository.PlaylistRepository;
import com.example.musicplaylist.recommendation.strategy.RecommendationStrategy;
import com.example.musicplaylist.song.domain.Song;
import com.example.musicplaylist.song.dto.SongResponse;
import com.example.musicplaylist.song.repository.SongRepository;
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
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class RecommendationServiceTest {

    @Mock
    private PlaylistRepository playlistRepository;

    @Mock
    private SongRepository songRepository;

    @Mock
    private RecommendationStrategy recommendationStrategy;

    @InjectMocks
    private RecommendationService recommendationService;

    @Test
    void shouldReturnRecommendedSongs() {
        Song yellow = createSong(1L, "Yellow", "Coldplay", "Rock");
        Song hello = createSong(2L, "Hello", "Adele", "Pop");
        Song fixYou = createSong(3L, "Fix You", "Coldplay", "Rock");
        Song easyOnMe = createSong(4L, "Easy On Me", "Adele", "Pop");
        Song takeFive = createSong(5L, "Take Five", "Dave Brubeck", "Jazz");

        Playlist playlist = Playlist.of("Favorites", "Favorite songs");
        playlist.addSong(yellow);
        playlist.addSong(hello);

        List<Song> candidates = List.of(yellow, hello, fixYou, easyOnMe, takeFive);

        given(playlistRepository.findByIdWithTracks(1L)).willReturn(Optional.of(playlist));
        given(songRepository.findAll()).willReturn(candidates);
        given(recommendationStrategy.recommend(List.of(yellow, hello), candidates, 2)).willReturn(List.of(fixYou, easyOnMe));

        List<SongResponse> recommendations = recommendationService.recommendSongs(1L, 2);

        assertThat(recommendations).containsExactly(SongResponse.from(fixYou), SongResponse.from(easyOnMe));
    }

    @Test
    void shouldReturnEmptyRecommendationsWhenPlaylistIsEmpty() {
        Playlist playlist = Playlist.of("Empty Playlist", null);

        given(playlistRepository.findByIdWithTracks(1L)).willReturn(Optional.of(playlist));

        List<SongResponse> recommendations = recommendationService.recommendSongs(1L, 10);

        assertThat(recommendations).isEmpty();

        verifyNoInteractions(songRepository, recommendationStrategy);
    }

    @Test
    void shouldThrowExceptionWhenPlaylistDoesNotExist() {
        given(playlistRepository.findByIdWithTracks(999L))
                .willReturn(Optional.empty());

        assertThatThrownBy(() ->
                recommendationService.recommendSongs(999L, 10))
                .isInstanceOfSatisfying(
                        BusinessException.class,
                        exception -> assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.PLAYLIST_NOT_FOUND)
                );

        verifyNoInteractions(songRepository, recommendationStrategy);
    }

    @Test
    void shouldThrowExceptionWhenLimitIsInvalid() {
        assertThatThrownBy(() ->
                recommendationService.recommendSongs(1L, 0))
                .isInstanceOfSatisfying(
                        BusinessException.class,
                        exception -> assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.INVALID_REQUEST)
                );

        verifyNoInteractions(playlistRepository, songRepository, recommendationStrategy);
    }

    private Song createSong(Long id, String title, String artist, String genre) {
        Song song = Song.builder()
                .title(title)
                .artist(artist)
                .genre(genre)
                .durationSeconds(240)
                .uri("music/" + title + ".mp3")
                .build();

        ReflectionTestUtils.setField(song, "id", id);

        return song;
    }
}