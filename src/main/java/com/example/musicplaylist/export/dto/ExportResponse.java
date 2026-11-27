package com.example.musicplaylist.export.dto;

public record ExportResponse(
        String fileName,
        String contentType,
        String content
) {
}