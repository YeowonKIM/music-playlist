package com.example.musicplaylist.shuffle.controller;

import com.example.musicplaylist.common.exception.BusinessException;
import com.example.musicplaylist.common.exception.ErrorCode;
import com.example.musicplaylist.shuffle.service.ShuffleService;
import com.example.musicplaylist.song.dto.SongResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ShuffleController.class)
class ShuffleControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ShuffleService shuffleService;

    @Test
    void shouldReturnAvailableStrategies() throws Exception {
        when(shuffleService.getAvailableStrategies())
                .thenReturn(List.of("genre-balanced", "random", "smart"));

        mockMvc.perform(get("/api/shuffle/strategies"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3))
                .andExpect(jsonPath("$[0]").value("genre-balanced"))
                .andExpect(jsonPath("$[1]").value("random"))
                .andExpect(jsonPath("$[2]").value("smart"));
    }

    @Test
    void shouldReturnShuffledSongs() throws Exception {
        SongResponse firstSong = new SongResponse(2L, "Hello", "Adele", "Pop", 295, "music/hello.mp3");
        SongResponse secondSong = new SongResponse(1L, "Yellow", "Coldplay", "Rock", 269, "music/yellow.mp3");

        when(shuffleService.shufflePlaylist(1L, "smart"))
                .thenReturn(List.of(firstSong, secondSong));

        mockMvc.perform(get("/api/playlists/1/shuffle").param("strategy", "smart"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(2))
                .andExpect(jsonPath("$[0].title").value("Hello"))
                .andExpect(jsonPath("$[1].id").value(1))
                .andExpect(jsonPath("$[1].title").value("Yellow"));

        verify(shuffleService).shufflePlaylist(1L, "smart");
    }

    @Test
    void shouldReturnBadRequestWhenStrategyIsInvalid() throws Exception {
        when(shuffleService.shufflePlaylist(1L, "unknown"))
                .thenThrow(new BusinessException(ErrorCode.INVALID_SHUFFLE_STRATEGY));

        mockMvc.perform(get("/api/playlists/1/shuffle").param("strategy", "unknown"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.code").value("INVALID_SHUFFLE_STRATEGY"))
                .andExpect(jsonPath("$.message").value("Invalid shuffle strategy"));
    }

    @Test
    void shouldReturnBadRequestWhenStrategyIsMissing() throws Exception {
        mockMvc.perform(get("/api/playlists/1/shuffle"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_REQUEST"));

        verifyNoInteractions(shuffleService);
    }
}