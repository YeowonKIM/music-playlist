package com.example.musicplaylist.export.controller;

import com.example.musicplaylist.export.dto.ExportResponse;
import com.example.musicplaylist.export.service.ExportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;
import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ExportController {

    private final ExportService exportService;

    @GetMapping("/export/formats")
    public List<String> getAvailableFormats() {
        return exportService.getAvailableFormats();
    }

    @GetMapping("/playlists/{playlistId}/export")
    public ResponseEntity<byte[]> exportPlaylist(@PathVariable Long playlistId, @RequestParam String format) {
        ExportResponse response = exportService.exportPlaylist(playlistId, format);

        byte[] content = response.content().getBytes(StandardCharsets.UTF_8);

        ContentDisposition contentDisposition = ContentDisposition.attachment()
                .filename(response.fileName())
                .build();

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(response.contentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, contentDisposition.toString())
                .contentLength(content.length)
                .body(content);
    }
}