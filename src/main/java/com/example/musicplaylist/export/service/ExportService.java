package com.example.musicplaylist.export.service;

import com.example.musicplaylist.common.exception.BusinessException;
import com.example.musicplaylist.common.exception.ErrorCode;
import com.example.musicplaylist.export.dto.ExportResponse;
import com.example.musicplaylist.export.strategy.ExportStrategy;
import com.example.musicplaylist.playlist.domain.Playlist;
import com.example.musicplaylist.playlist.repository.PlaylistRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ExportService {

    private final PlaylistRepository playlistRepository;
    private final ExportStrategyRegistry exportStrategyRegistry;

    public List<String> getAvailableFormats() {
        return exportStrategyRegistry.getAvailableFormats();
    }

    public ExportResponse exportPlaylist(Long playlistId, String format) {
        ExportStrategy strategy = exportStrategyRegistry.getStrategy(format);

        Playlist playlist = playlistRepository.findByIdWithTracks(playlistId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PLAYLIST_NOT_FOUND));

        String fileName = "playlist-" + playlist.getId() + "." + strategy.getFormat();
        String content = strategy.export(playlist);

        return new ExportResponse(fileName, strategy.getContentType(), content);
    }
}