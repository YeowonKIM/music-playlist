package com.example.musicplaylist.song.controller;

import com.example.musicplaylist.song.dto.CreateSongRequest;
import com.example.musicplaylist.song.dto.SongResponse;
import com.example.musicplaylist.song.service.SongService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/songs")
@RequiredArgsConstructor
public class SongController {

    private final SongService songService;

    @PostMapping
    public ResponseEntity<SongResponse> createSong(
            @Valid @RequestBody CreateSongRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(songService.createSong(request));
    }

    @GetMapping
    public ResponseEntity<List<SongResponse>> getAllSongs() {
        return ResponseEntity.ok(songService.getAllSongs());
    }

    @GetMapping("/{songId}")
    public ResponseEntity<SongResponse> getSong(@PathVariable Long songId) {
        return ResponseEntity.ok(songService.getSong(songId));
    }
}