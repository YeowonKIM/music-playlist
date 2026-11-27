package com.example.musicplaylist.recommendation.controller;

import com.example.musicplaylist.recommendation.service.RecommendationService;
import com.example.musicplaylist.song.dto.SongResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/playlists")
@RequiredArgsConstructor
public class RecommendationController {

    private final RecommendationService recommendationService;

    @GetMapping("/{playlistId}/recommendations")
    public List<SongResponse> recommendSongs(@PathVariable Long playlistId, @RequestParam(defaultValue = "10") int limit) {
        return recommendationService.recommendSongs(playlistId, limit);
    }
}