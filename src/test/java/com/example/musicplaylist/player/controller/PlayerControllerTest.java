package com.example.musicplaylist.player.controller;

import com.example.musicplaylist.common.exception.BusinessException;
import com.example.musicplaylist.common.exception.ErrorCode;
import com.example.musicplaylist.player.domain.PlaybackStatus;
import com.example.musicplaylist.player.dto.PlaybackStateResponse;
import com.example.musicplaylist.player.dto.PlayRequest;
import com.example.musicplaylist.player.service.PlayerService;
import com.example.musicplaylist.song.dto.SongResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PlayerController.class)
class PlayerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PlayerService playerService;

    @Test
    void shouldPlayPlaylist() throws Exception {
        SongResponse song = new SongResponse(1L, "Yellow", "Coldplay", "Rock", 240, "music/yellow.mp3");

        PlayRequest request = new PlayRequest(1L, "smart");
        PlaybackStateResponse response = new PlaybackStateResponse(1L, PlaybackStatus.PLAYING, song, 0, List.of(song), "smart");

        when(playerService.play(request)).thenReturn(response);

        mockMvc.perform(post("/api/player/play")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "playlistId": 1,
                                    "shuffleStrategy": "smart"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.playlistId").value(1))
                .andExpect(jsonPath("$.status").value("PLAYING"))
                .andExpect(jsonPath("$.currentIndex").value(0))
                .andExpect(jsonPath("$.currentSong.title").value("Yellow"))
                .andExpect(jsonPath("$.playbackQueue.length()").value(1))
                .andExpect(jsonPath("$.shuffleStrategy").value("smart"));

        verify(playerService).play(request);
    }

    @Test
    void shouldRejectInvalidPlayRequest() throws Exception {
        mockMvc.perform(post("/api/player/play")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "playlistId": 0
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"));

        verifyNoInteractions(playerService);
    }

    @Test
    void shouldReturnConflictForInvalidPlaybackState() throws Exception {
        when(playerService.pause())
                .thenThrow(new BusinessException(ErrorCode.INVALID_PLAYBACK_STATE));

        mockMvc.perform(post("/api/player/pause"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.code").value("INVALID_PLAYBACK_STATE"))
                .andExpect(jsonPath("$.message").value("Invalid playback state"));
    }
}