package com.example.musicplaylist.recommendation.controller;

import com.example.musicplaylist.common.exception.BusinessException;
import com.example.musicplaylist.common.exception.ErrorCode;
import com.example.musicplaylist.recommendation.service.RecommendationService;
import com.example.musicplaylist.song.dto.SongResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(RecommendationController.class)
class RecommendationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RecommendationService recommendationService;

    @Test
    void shouldReturnRecommendationsWithDefaultLimit() throws Exception {
        SongResponse song = getSong();

        given(recommendationService.recommendSongs(1L, 10)).willReturn(List.of(song));

        mockMvc.perform(get("/api/playlists/1/recommendations"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(2))
                .andExpect(jsonPath("$[0].title").value("Fix You"));
    }
    @Test
    void shouldReturnRecommendationsWithCustomLimit() throws Exception {
        SongResponse song = getSong();

        given(recommendationService.recommendSongs(1L, 1)).willReturn(List.of(song));

        mockMvc.perform(get("/api/playlists/1/recommendations").param("limit", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(2));
    }

    @Test
    void shouldReturnBadRequestWhenLimitIsInvalid() throws Exception {
        given(recommendationService.recommendSongs(1L, 0)).willThrow(new BusinessException(ErrorCode.INVALID_REQUEST, "Limit must be positive"));

        mockMvc.perform(get("/api/playlists/1/recommendations").param("limit", "0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_REQUEST"));
    }

    private static SongResponse getSong() {
        return new SongResponse(2L, "Fix You", "Coldplay", "Rock", 295, "music/fix-you.mp3");
    }

}