package com.example.musicplaylist.player.controller;

import com.example.musicplaylist.player.dto.PlaybackStateResponse;
import com.example.musicplaylist.player.dto.PlayRequest;
import com.example.musicplaylist.player.service.PlayerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/player")
@RequiredArgsConstructor
public class PlayerController {

    private final PlayerService playerService;

    @PostMapping("/play")
    public PlaybackStateResponse play(@Valid @RequestBody PlayRequest request) {
        return playerService.play(request);
    }

    @PostMapping("/pause")
    public PlaybackStateResponse pause() {
        return playerService.pause();
    }

    @PostMapping("/resume")
    public PlaybackStateResponse resume() {
        return playerService.resume();
    }

    @PostMapping("/stop")
    public PlaybackStateResponse stop() {
        return playerService.stop();
    }

    @PostMapping("/next")
    public PlaybackStateResponse next() {
        return playerService.next();
    }

    @PostMapping("/previous")
    public PlaybackStateResponse previous() {
        return playerService.previous();
    }

    @GetMapping("/state")
    public PlaybackStateResponse getState() {
        return playerService.getState();
    }
}