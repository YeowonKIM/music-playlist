package com.example.musicplaylist.shuffle.controller;

import com.example.musicplaylist.shuffle.service.ShuffleService;
import com.example.musicplaylist.song.dto.SongResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ShuffleController {

    private final ShuffleService shuffleService;

    @GetMapping("/shuffle/strategies")
    public List<String> getAvailableStrategies() {
        return shuffleService.getAvailableStrategies();
    }

    @GetMapping("/playlists/{playlistId}/shuffle")
    public List<SongResponse> shufflePlaylist(@PathVariable Long playlistId, @RequestParam String strategy) {
        return shuffleService.shufflePlaylist(playlistId, strategy);
    }
}