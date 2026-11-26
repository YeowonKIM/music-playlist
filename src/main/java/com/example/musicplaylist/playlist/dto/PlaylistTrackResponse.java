package com.example.musicplaylist.playlist.dto;

import com.example.musicplaylist.playlist.domain.PlaylistTrack;
import com.example.musicplaylist.song.domain.Song;

public record PlaylistTrackResponse(
        Long songId,
        String title,
        String artist,
        String genre,
        int durationSeconds,
        String uri,
        int position
) {
    public static PlaylistTrackResponse from(PlaylistTrack track) {
        Song song = track.getSong();

        return new PlaylistTrackResponse(
                song.getId(),
                song.getTitle(),
                song.getArtist(),
                song.getGenre(),
                song.getDurationSeconds(),
                song.getUri(),
                track.getPosition()
        );
    }
}