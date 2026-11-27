package com.example.musicplaylist.workflow.service;

import com.example.musicplaylist.common.exception.BusinessException;
import com.example.musicplaylist.common.exception.ErrorCode;
import com.example.musicplaylist.player.domain.PlaybackStatus;
import com.example.musicplaylist.player.dto.PlaybackStateResponse;
import com.example.musicplaylist.player.service.PlayerService;
import com.example.musicplaylist.playlist.repository.PlaylistRepository;
import com.example.musicplaylist.song.domain.Song;
import com.example.musicplaylist.song.repository.SongRepository;
import com.example.musicplaylist.workflow.dto.CreateAndPlayRequest;
import com.example.musicplaylist.workflow.dto.CreateAndPlayResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest(properties = "app.sample-data.enabled=false")
class PlaylistWorkflowServiceTest {

    @Autowired
    private PlaylistWorkflowService playlistWorkflowService;

    @Autowired
    private PlaylistRepository playlistRepository;

    @Autowired
    private SongRepository songRepository;

    @Autowired
    private PlayerService playerService;

    private Song yellow;
    private Song clocks;

    @BeforeEach
    void setUp() {

        yellow = songRepository.save(
                Song.builder()
                        .title("Yellow")
                        .artist("Coldplay")
                        .genre("Rock")
                        .durationSeconds(240)
                        .uri("music/yellow.mp3")
                        .build()
        );

        clocks = songRepository.save(
                Song.builder()
                        .title("Clocks")
                        .artist("Coldplay")
                        .genre("Rock")
                        .durationSeconds(307)
                        .uri("music/clocks.mp3")
                        .build()
        );
    }

    @AfterEach
    void tearDown() {

        playerService.stop();
        playlistRepository.deleteAll();
        songRepository.deleteAll();
    }

    @Test
    void shouldCreateRecommendAndPlayPlaylist() {

        CreateAndPlayRequest request = new CreateAndPlayRequest("My Smart Playlist", "Automatically generated playlist", List.of(yellow.getId()), 1, null);
        CreateAndPlayResponse response = playlistWorkflowService.createAndPlay(request);

        assertThat(response.playlist().id()).isNotNull();
        assertThat(response.playlist().name()).isEqualTo("My Smart Playlist");
        assertThat(response.playlist().tracks()).hasSize(2);
        assertThat(response.recommendations()).hasSize(1);
        assertThat(response.recommendations().get(0).id()).isEqualTo(clocks.getId());
        assertThat(playlistRepository.existsById(response.playlist().id())).isTrue();
        assertThat(response.playbackState().playlistId()).isEqualTo(response.playlist().id());
        assertThat(response.playbackState().status()).isEqualTo(PlaybackStatus.PLAYING);
        assertThat(response.playbackState().currentIndex()).isZero();
        assertThat(response.playbackState().playbackQueue())
                .extracting(song -> song.id())
                .containsExactly(yellow.getId(), clocks.getId());
        assertThat(playerService.getState()).isEqualTo(response.playbackState());
    }

    @Test
    void shouldRollbackPlaylistAndPreservePlaybackStateWhenSongIsNotFound() {

        CreateAndPlayRequest initialRequest = new CreateAndPlayRequest("Existing Playlist", "Currently playing playlist", List.of(yellow.getId()), 1, null);

        playlistWorkflowService.createAndPlay(initialRequest);

        PlaybackStateResponse previousState = playerService.getState();
        long previousPlaylistCount = playlistRepository.count();

        CreateAndPlayRequest invalidRequest = new CreateAndPlayRequest("Rollback Playlist", "Invalid song test", List.of(yellow.getId(), Long.MAX_VALUE), 1, null);

        assertThatThrownBy(() -> playlistWorkflowService.createAndPlay(invalidRequest))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.SONG_NOT_FOUND);
        assertThat(playlistRepository.count()).isEqualTo(previousPlaylistCount);
        assertThat(playlistRepository.findAll()).noneMatch(playlist -> playlist.getName().equals("Rollback Playlist"));
        assertThat(playerService.getState()).isEqualTo(previousState);
    }
}