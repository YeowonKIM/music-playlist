package com.example.musicplaylist.player.domain;

import com.example.musicplaylist.common.exception.BusinessException;
import com.example.musicplaylist.common.exception.ErrorCode;
import com.example.musicplaylist.song.dto.SongResponse;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class MusicPlayer {

    private PlaybackState state = PlaybackState.stopped();

    public synchronized PlaybackState play(Long playlistId, List<SongResponse> songs, String shuffleStrategy) {
        if (songs.isEmpty()) {
            throw new BusinessException(ErrorCode.EMPTY_PLAYLIST);
        }

        state = PlaybackState.playing(playlistId, songs, shuffleStrategy);

        return state;
    }

    public synchronized PlaybackState pause() {
        if (state.status() != PlaybackStatus.PLAYING) {
            throw new BusinessException(ErrorCode.INVALID_PLAYBACK_STATE);
        }

        state = state.withStatus(PlaybackStatus.PAUSED);

        return state;
    }

    public synchronized PlaybackState resume() {
        if (state.status() != PlaybackStatus.PAUSED) {
            throw new BusinessException(ErrorCode.INVALID_PLAYBACK_STATE);
        }

        state = state.withStatus(PlaybackStatus.PLAYING);

        return state;
    }

    public synchronized PlaybackState stop() {
        state = PlaybackState.stopped();

        return state;
    }

    public synchronized PlaybackState next() {
        validateActivePlayback();

        int nextIndex = state.currentIndex() + 1;

        if (nextIndex >= state.playbackQueue().size()) {
            state = PlaybackState.stopped();
        } else {
            state = state.withCurrentIndex(nextIndex);
        }

        return state;
    }

    public synchronized PlaybackState previous() {
        validateActivePlayback();

        if (state.currentIndex() > 0) {
            state = state.withCurrentIndex(state.currentIndex() - 1);
        }

        return state;
    }

    public synchronized PlaybackState getState() {
        return state;
    }

    private void validateActivePlayback() {
        if (state.status() == PlaybackStatus.STOPPED) {
            throw new BusinessException(ErrorCode.INVALID_PLAYBACK_STATE);
        }
    }
}