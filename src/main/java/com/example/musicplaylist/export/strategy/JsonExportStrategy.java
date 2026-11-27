package com.example.musicplaylist.export.strategy;

import com.example.musicplaylist.playlist.domain.Playlist;
import com.example.musicplaylist.playlist.dto.PlaylistDetailResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

@Component
@RequiredArgsConstructor
public class JsonExportStrategy implements ExportStrategy {

    private final ObjectMapper objectMapper;

    @Override
    public String getFormat() {
        return "json";
    }

    @Override
    public String getContentType() {
        return "application/json";
    }

    @Override
    public String export(Playlist playlist) {
        return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(PlaylistDetailResponse.from(playlist));
    }
}