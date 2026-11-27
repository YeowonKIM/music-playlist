package com.example.musicplaylist.player.dto;

import com.example.musicplaylist.player.domain.PlaybackState;
import com.example.musicplaylist.player.domain.PlaybackStatus;
import com.example.musicplaylist.song.dto.SongResponse;

import java.util.List;

public record PlaybackStateResponse(
        Long playlistId,
        PlaybackStatus status,
        SongResponse currentSong,
        int currentIndex,
        List<SongResponse> playbackQueue,
        String shuffleStrategy
) {

    public PlaybackStateResponse {
        playbackQueue = List.copyOf(playbackQueue);
    }

    public static PlaybackStateResponse from(PlaybackState state) {
        return new PlaybackStateResponse(state.playlistId(), state.status(), state.currentSong(), state.currentIndex(), state.playbackQueue(), state.shuffleStrategy());
    }
}