package com.example.musicplaylist.playlist.dto;

import com.example.musicplaylist.playlist.domain.Playlist;

import java.util.List;

public record PlaylistDetailResponse(
        Long id,
        String name,
        String description,
        List<PlaylistTrackResponse> tracks
) {
    public static PlaylistDetailResponse from(Playlist playlist) {
        return new PlaylistDetailResponse(
                playlist.getId(),
                playlist.getName(),
                playlist.getDescription(),
                playlist.getTracks().stream()
                        .map(PlaylistTrackResponse::from)
                        .toList()
        );
    }
}