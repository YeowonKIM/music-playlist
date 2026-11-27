package com.example.musicplaylist.player.domain;

import com.example.musicplaylist.common.exception.BusinessException;
import com.example.musicplaylist.common.exception.ErrorCode;
import com.example.musicplaylist.song.dto.SongResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MusicPlayerTest {

    private MusicPlayer musicPlayer;

    private final SongResponse yellow = new SongResponse(1L, "Yellow", "Coldplay", "Rock", 240, "music/yellow.mp3");
    private final SongResponse hello = new SongResponse(2L, "Hello", "Adele", "Pop", 295, "music/hello.mp3");

    @BeforeEach
    void setUp() {
        musicPlayer = new MusicPlayer();
    }

    @Test
    void shouldInitializeWithStoppedState() {
        PlaybackState state = musicPlayer.getState();

        assertThat(state.status()).isEqualTo(PlaybackStatus.STOPPED);
        assertThat(state.playlistId()).isNull();
        assertThat(state.currentSong()).isNull();
        assertThat(state.currentIndex()).isEqualTo(-1);
        assertThat(state.playbackQueue()).isEmpty();
        assertThat(state.shuffleStrategy()).isNull();
    }

    @Test
    void shouldPlayPauseResumeAndStop() {
        PlaybackState playing = musicPlayer.play(1L, List.of(yellow, hello), "smart");

        assertThat(playing.status()).isEqualTo(PlaybackStatus.PLAYING);
        assertThat(playing.playlistId()).isEqualTo(1L);
        assertThat(playing.currentSong()).isEqualTo(yellow);
        assertThat(playing.currentIndex()).isZero();
        assertThat(playing.playbackQueue()).containsExactly(yellow, hello);
        assertThat(playing.shuffleStrategy()).isEqualTo("smart");

        PlaybackState paused = musicPlayer.pause();

        assertThat(paused.status()).isEqualTo(PlaybackStatus.PAUSED);
        assertThat(paused.currentSong()).isEqualTo(yellow);

        PlaybackState resumed = musicPlayer.resume();

        assertThat(resumed.status()).isEqualTo(PlaybackStatus.PLAYING);

        PlaybackState stopped = musicPlayer.stop();

        assertThat(stopped).isEqualTo(PlaybackState.stopped());
    }

    @Test
    void shouldNavigateTracksAndHandleBoundaries() {
        musicPlayer.play(1L, List.of(yellow, hello), null);

        assertThat(musicPlayer.previous().currentIndex()).isZero();
        assertThat(musicPlayer.next().currentSong()).isEqualTo(hello);
        assertThat(musicPlayer.previous().currentSong()).isEqualTo(yellow);
        assertThat(musicPlayer.next().currentIndex()).isEqualTo(1);
        assertThat(musicPlayer.next()).isEqualTo(PlaybackState.stopped());
    }

    @Test
    void shouldPreservePausedStatusWhenNavigatingTracks() {
        musicPlayer.play(1L, List.of(yellow, hello), null);
        musicPlayer.pause();

        PlaybackState next = musicPlayer.next();

        assertThat(next.status()).isEqualTo(PlaybackStatus.PAUSED);
        assertThat(next.currentSong()).isEqualTo(hello);

        PlaybackState previous = musicPlayer.previous();

        assertThat(previous.status()).isEqualTo(PlaybackStatus.PAUSED);
        assertThat(previous.currentSong()).isEqualTo(yellow);
    }

    @Test
    void shouldRejectInvalidStateTransitions() {
        assertThatThrownBy(musicPlayer::pause)
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_PLAYBACK_STATE);

        assertThatThrownBy(musicPlayer::resume)
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_PLAYBACK_STATE);

        assertThatThrownBy(musicPlayer::next)
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_PLAYBACK_STATE);

        assertThatThrownBy(musicPlayer::previous)
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_PLAYBACK_STATE);

        assertThat(musicPlayer.getState()).isEqualTo(PlaybackState.stopped());

        musicPlayer.play(1L, List.of(yellow), null);

        assertThatThrownBy(musicPlayer::resume)
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_PLAYBACK_STATE);

        musicPlayer.pause();

        assertThatThrownBy(musicPlayer::pause)
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_PLAYBACK_STATE);

        assertThat(musicPlayer.getState().status()).isEqualTo(PlaybackStatus.PAUSED);
    }

    @Test
    void shouldPreservePreviousStateWhenPlayFails() {
        PlaybackState original = musicPlayer.play(1L, List.of(yellow, hello), null);

        assertThatThrownBy(() -> musicPlayer.play(2L, List.of(), null))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.EMPTY_PLAYLIST);

        assertThat(musicPlayer.getState()).isEqualTo(original);
    }

    @Test
    void shouldProtectPlaybackQueueFromExternalModification() {
        List<SongResponse> songs = new ArrayList<>(List.of(yellow, hello));

        PlaybackState state = musicPlayer.play(1L, songs, null);

        songs.clear();

        assertThat(state.playbackQueue()).containsExactly(yellow, hello);

        assertThatThrownBy(() -> state.playbackQueue().clear())
                .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void shouldPreservePreviouslyReturnedSnapshot() {
        PlaybackState original = musicPlayer.play(1L, List.of(yellow, hello), null);

        musicPlayer.next();
        musicPlayer.pause();
        musicPlayer.play(2L, List.of(hello), "random");

        assertThat(original.playlistId()).isEqualTo(1L);
        assertThat(original.status()).isEqualTo(PlaybackStatus.PLAYING);
        assertThat(original.currentIndex()).isZero();
        assertThat(original.currentSong()).isEqualTo(yellow);
        assertThat(original.playbackQueue()).containsExactly(yellow, hello);

        assertThat(musicPlayer.getState().playlistId()).isEqualTo(2L);
        assertThat(musicPlayer.getState().currentSong()).isEqualTo(hello);
    }
}