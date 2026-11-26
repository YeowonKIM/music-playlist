package com.example.musicplaylist.playlist.controller;

import com.example.musicplaylist.playlist.dto.AddSongRequest;
import com.example.musicplaylist.playlist.dto.CreatePlaylistRequest;
import com.example.musicplaylist.playlist.dto.PlaylistResponse;
import com.example.musicplaylist.playlist.dto.UpdatePlaylistRequest;
import com.example.musicplaylist.playlist.service.PlaylistService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/playlists")
@RequiredArgsConstructor
public class PlaylistController {

    private final PlaylistService playlistService;

    @PostMapping
    public ResponseEntity<PlaylistResponse> createPlaylist(
            @Valid @RequestBody CreatePlaylistRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(playlistService.createPlaylist(request));
    }

    @GetMapping
    public ResponseEntity<List<PlaylistResponse>> getAllPlaylists() {
        return ResponseEntity.ok(playlistService.getAllPlaylists());
    }

    @GetMapping("/{playlistId}")
    public ResponseEntity<PlaylistResponse> getPlaylist(@PathVariable Long playlistId) {
        return ResponseEntity.ok(playlistService.getPlaylist(playlistId));
    }

    @PutMapping("/{playlistId}")
    public ResponseEntity<PlaylistResponse> updatePlaylist(@PathVariable Long playlistId, @Valid @RequestBody UpdatePlaylistRequest request) {
        return ResponseEntity.ok(playlistService.updatePlaylist(playlistId, request));
    }

    @DeleteMapping("/{playlistId}")
    public ResponseEntity<Void> deletePlaylist(@PathVariable Long playlistId) {
        playlistService.deletePlaylist(playlistId);

        return ResponseEntity.noContent().build();
    }

    // Dynamic song addition & removal
    @PostMapping("/{playlistId}/tracks")
    public ResponseEntity<Void> addSongToPlaylist(@PathVariable Long playlistId, @Valid @RequestBody AddSongRequest request) {
        playlistService.addSongToPlaylist(playlistId, request);

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @DeleteMapping("/{playlistId}/tracks/{songId}")
    public ResponseEntity<Void> removeSongFromPlaylist(@PathVariable Long playlistId, @PathVariable Long songId) {
        playlistService.removeSongFromPlaylist(playlistId, songId);

        return ResponseEntity.noContent().build();
    }
}