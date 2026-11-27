package com.example.musicplaylist.player.domain;

import com.example.musicplaylist.common.exception.ErrorCode;
import com.example.musicplaylist.song.dto.SongResponse;

import java.util.List;
import java.util.Objects;

public record PlaybackState(
        Long playlistId,
        PlaybackStatus status,
        List<SongResponse> playbackQueue,
        int currentIndex,
        String shuffleStrategy
) {

    public PlaybackState {
        Objects.requireNonNull(status, "Playback status must not be null");

        playbackQueue = List.copyOf(playbackQueue);

        if (status == PlaybackStatus.STOPPED) {
            if (playlistId != null || !playbackQueue.isEmpty() || currentIndex != -1 || shuffleStrategy != null) {
                throw new IllegalArgumentException(ErrorCode.INVALID_STOPPED_PLAYBACK_STATE.getMessage());
            }
        } else {
            if (playlistId == null || playlistId <= 0 || playbackQueue.isEmpty() || currentIndex < 0 || currentIndex >= playbackQueue.size()) {
                throw new IllegalArgumentException(ErrorCode.INVALID_ACTIVE_PLAYBACK_STATE.getMessage());
            }
        }
    }

    public static PlaybackState stopped() {
        return new PlaybackState(null, PlaybackStatus.STOPPED, List.of(), -1, null
        );
    }

    public static PlaybackState playing(Long playlistId, List<SongResponse> playbackQueue, String shuffleStrategy) {
        return new PlaybackState(playlistId, PlaybackStatus.PLAYING, playbackQueue, 0, shuffleStrategy);
    }

    public SongResponse currentSong() {
        return currentIndex == -1 ? null : playbackQueue.get(currentIndex);
    }

    PlaybackState withStatus(PlaybackStatus status) {
        return new PlaybackState(playlistId, status, playbackQueue, currentIndex, shuffleStrategy);
    }

    PlaybackState withCurrentIndex(int currentIndex) {
        return new PlaybackState(playlistId, status, playbackQueue, currentIndex, shuffleStrategy);
    }
}