package com.example.musicplaylist.song.dto;

import com.example.musicplaylist.song.domain.Song;

public record SongResponse(
        Long id,
        String title,
        String artist,
        String genre,
        int durationSeconds,
        String uri
) {

    public static SongResponse from(Song song) {
        return new SongResponse(
                song.getId(),
                song.getTitle(),
                song.getArtist(),
                song.getGenre(),
                song.getDurationSeconds(),
                song.getUri()
        );
    }
}