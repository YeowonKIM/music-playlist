package com.example.musicplaylist.song.service;

import com.example.musicplaylist.common.exception.BusinessException;
import com.example.musicplaylist.common.exception.ErrorCode;
import com.example.musicplaylist.song.domain.Song;
import com.example.musicplaylist.song.dto.CreateSongRequest;
import com.example.musicplaylist.song.dto.SongResponse;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class SongServiceTest {

    @Mock
    private SongRepository songRepository;

    @InjectMocks
    private SongService songService;

    @Nested
    class CreateSongTests {

        @Test
        void shouldCreateSong() {
            CreateSongRequest request = new CreateSongRequest("Yellow", "Coldplay", "Rock", 240, "music/yellow.mp3");

            when(songRepository.save(any(Song.class)))
                    .thenAnswer(invocation -> {
                        Song song = invocation.getArgument(0);
                        ReflectionTestUtils.setField(song, "id", 1L);
                        return song;
                    });

            SongResponse response = songService.createSong(request);

            assertThat(response.id()).isEqualTo(1L);
            assertThat(response.title()).isEqualTo("Yellow");
            assertThat(response.artist()).isEqualTo("Coldplay");
            assertThat(response.genre()).isEqualTo("Rock");
            assertThat(response.durationSeconds()).isEqualTo(240);
            assertThat(response.uri()).isEqualTo("music/yellow.mp3");

            verify(songRepository).save(any(Song.class));
        }
    }

    @Nested
    class GetSongTests {

        @Test
        void shouldGetAllSongs() {
            Song firstSong = createSong(1L);

            Song secondSong = createSong(2L, "Hello", "Adele", "Pop", 295, "music/hello.mp3");

            when(songRepository.findAll())
                    .thenReturn(List.of(firstSong, secondSong));

            List<SongResponse> responses = songService.getAllSongs();

            assertThat(responses).containsExactly(
                    new SongResponse(1L, "Yellow", "Coldplay", "Rock", 240, "music/yellow.mp3"),
                    new SongResponse(2L, "Hello", "Adele", "Pop", 295, "music/hello.mp3")
            );

            verify(songRepository).findAll();
        }

        @Test
        void shouldReturnEmptyListWhenNoSongsExist() {
            when(songRepository.findAll())
                    .thenReturn(List.of());

            List<SongResponse> responses = songService.getAllSongs();

            assertThat(responses).isEmpty();

            verify(songRepository).findAll();
        }

        @Test
        void shouldGetSongById() {
            Song song = createSong(1L);

            when(songRepository.findById(1L))
                    .thenReturn(Optional.of(song));

            SongResponse response = songService.getSong(1L);

            assertThat(response).isEqualTo(
                    new SongResponse(1L, "Yellow", "Coldplay", "Rock", 240, "music/yellow.mp3"));

            verify(songRepository).findById(1L);
        }

        @Test
        void shouldThrowExceptionWhenSongNotFound() {
            when(songRepository.findById(999L))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> songService.getSong(999L))
                    .isInstanceOfSatisfying(
                            BusinessException.class,
                            exception -> {
                                assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.SONG_NOT_FOUND);
                                assertThat(exception.getMessage()).isEqualTo("Song not found");
                            }
                    );

            verify(songRepository).findById(999L);
        }
    }

    private Song createSong(Long id) {
        return createSong(
                id,
                "Yellow",
                "Coldplay",
                "Rock",
                240,
                "music/yellow.mp3"
        );
    }

    private Song createSong(Long id, String title, String artist, String genre, int durationSeconds, String uri) {
        Song song = Song.builder()
                .title(title)
                .artist(artist)
                .genre(genre)
                .durationSeconds(durationSeconds)
                .uri(uri)
                .build();

        ReflectionTestUtils.setField(song, "id", id);

        return song;
    }
}