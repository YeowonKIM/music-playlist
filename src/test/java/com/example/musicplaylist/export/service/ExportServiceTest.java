package com.example.musicplaylist.export.service;

import com.example.musicplaylist.common.exception.BusinessException;
import com.example.musicplaylist.common.exception.ErrorCode;
import com.example.musicplaylist.export.dto.ExportResponse;
import com.example.musicplaylist.export.strategy.ExportStrategy;
import com.example.musicplaylist.playlist.domain.Playlist;
import com.example.musicplaylist.playlist.repository.PlaylistRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class ExportServiceTest {

    @Mock
    private PlaylistRepository playlistRepository;

    @Mock
    private ExportStrategyRegistry exportStrategyRegistry;

    @Mock
    private ExportStrategy exportStrategy;

    @InjectMocks
    private ExportService exportService;

    @Test
    void shouldExportPlaylist() {
        Playlist playlist = Playlist.of("Favorites", null);
        ReflectionTestUtils.setField(playlist, "id", 1L);

        String content = "#EXTM3U\n";

        given(exportStrategyRegistry.getStrategy("m3u")).willReturn(exportStrategy);
        given(playlistRepository.findByIdWithTracks(1L)).willReturn(Optional.of(playlist));
        given(exportStrategy.getFormat()).willReturn("m3u");
        given(exportStrategy.getContentType()).willReturn("audio/x-mpegurl");
        given(exportStrategy.export(playlist)).willReturn(content);

        ExportResponse response = exportService.exportPlaylist(1L, "m3u");

        assertThat(response.fileName()).isEqualTo("playlist-1.m3u");
        assertThat(response.contentType()).isEqualTo("audio/x-mpegurl");
        assertThat(response.content()).isEqualTo(content);
    }

    @Test
    void shouldThrowExceptionWhenPlaylistDoesNotExist() {
        given(exportStrategyRegistry.getStrategy("m3u")).willReturn(exportStrategy);
        given(playlistRepository.findByIdWithTracks(999L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> exportService.exportPlaylist(999L, "m3u"))
                .isInstanceOfSatisfying(
                        BusinessException.class,
                        exception -> assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.PLAYLIST_NOT_FOUND)
                );

        verifyNoInteractions(exportStrategy);
    }
}