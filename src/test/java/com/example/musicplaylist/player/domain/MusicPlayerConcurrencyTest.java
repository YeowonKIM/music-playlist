package com.example.musicplaylist.player.domain;

import com.example.musicplaylist.song.dto.SongResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

@Timeout(30)
class MusicPlayerConcurrencyTest {

    private static final int THREAD_COUNT = 8;
    private static final int OPERATION_COUNT = 1000;

    @Test
    void shouldHandleConcurrentNextOperationsWithoutLostUpdates() throws Exception {
        MusicPlayer musicPlayer = new MusicPlayer();

        List<SongResponse> songs = IntStream.rangeClosed(1, OPERATION_COUNT + 1)
                .mapToObj(i -> new SongResponse((long) i, "Song " + i, "Artist", "Rock", 240, "music/song-" + i + ".mp3"))
                .toList();

        musicPlayer.play(1L, songs, null);

        ExecutorService executor = Executors.newFixedThreadPool(THREAD_COUNT);
        CountDownLatch startLatch = new CountDownLatch(1);

        try {
            List<Future<PlaybackState>> futures = new ArrayList<>();

            for (int i = 0; i < OPERATION_COUNT; i++) {
                futures.add(executor.submit(() -> {
                    startLatch.await();

                    return musicPlayer.next();
                }));
            }

            startLatch.countDown();

            Set<Integer> returnedIndices = new HashSet<>();

            for (Future<PlaybackState> future : futures) {
                PlaybackState state = future.get();

                assertThat(state.status()).isEqualTo(PlaybackStatus.PLAYING);
                assertThat(state.currentSong()).isEqualTo(songs.get(state.currentIndex()));

                returnedIndices.add(state.currentIndex());
            }

            PlaybackState finalState = musicPlayer.getState();

            assertThat(finalState.currentIndex()).isEqualTo(OPERATION_COUNT);
            assertThat(finalState.currentSong()).isEqualTo(songs.get(OPERATION_COUNT));
            assertThat(returnedIndices).hasSize(OPERATION_COUNT);
        } finally {
            startLatch.countDown();
            executor.shutdownNow();
        }
    }

    @Test
    void shouldMaintainConsistentStateUnderConcurrentOperations() throws Exception {
        MusicPlayer musicPlayer = new MusicPlayer();

        List<SongResponse> songs = List.of(
                new SongResponse(1L, "Yellow", "Coldplay", "Rock", 240, "music/yellow.mp3"),
                new SongResponse(2L, "Hello", "Adele", "Pop", 295, "music/hello.mp3")
        );

        ExecutorService executor = Executors.newFixedThreadPool(THREAD_COUNT);
        CountDownLatch startLatch = new CountDownLatch(1);

        try {
            List<Future<PlaybackState>> futures = new ArrayList<>();

            for (int i = 0; i < OPERATION_COUNT; i++) {
                int operation = i % 3;

                futures.add(executor.submit(() -> {
                    startLatch.await();

                    return switch (operation) {
                        case 0 -> musicPlayer.play(1L, songs, null);
                        case 1 -> musicPlayer.stop();
                        default -> musicPlayer.getState();
                    };
                }));
            }

            startLatch.countDown();

            for (Future<PlaybackState> future : futures) {
                assertValidState(future.get(), songs);
            }

            assertValidState(musicPlayer.getState(), songs);
        } finally {
            startLatch.countDown();
            executor.shutdownNow();
        }
    }

    private void assertValidState(PlaybackState state, List<SongResponse> songs) {
        if (state.status() == PlaybackStatus.STOPPED) {
            assertThat(state.playlistId()).isNull();
            assertThat(state.playbackQueue()).isEmpty();
            assertThat(state.currentIndex()).isEqualTo(-1);
            assertThat(state.currentSong()).isNull();
            assertThat(state.shuffleStrategy()).isNull();
        } else {
            assertThat(state.status()).isEqualTo(PlaybackStatus.PLAYING);
            assertThat(state.playlistId()).isEqualTo(1L);
            assertThat(state.playbackQueue()).containsExactlyElementsOf(songs);
            assertThat(state.currentIndex()).isZero();
            assertThat(state.currentSong()).isEqualTo(songs.get(0));
            assertThat(state.shuffleStrategy()).isNull();
        }
    }
}