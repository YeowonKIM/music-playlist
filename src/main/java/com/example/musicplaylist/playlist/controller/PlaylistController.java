package com.example.musicplaylist.playlist.controller;

import com.example.musicplaylist.common.dto.MessageResponse;
import com.example.musicplaylist.playlist.dto.*;
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

    @GetMapping("/details")
    public ResponseEntity<List<PlaylistDetailResponse>> getAllPlaylistDetails() {
        return ResponseEntity.ok(
                playlistService.getAllPlaylistDetails()
        );
    }

    @GetMapping("/{playlistId}")
    public ResponseEntity<PlaylistResponse> getPlaylist(@PathVariable Long playlistId) {
        return ResponseEntity.ok(playlistService.getPlaylist(playlistId));
    }

    @GetMapping("/{playlistId}/details")
    public ResponseEntity<PlaylistDetailResponse> getPlaylistDetails(
            @PathVariable Long playlistId
    ) {
        return ResponseEntity.ok(
                playlistService.getPlaylistDetails(playlistId)
        );
    }

    @PutMapping("/{playlistId}")
    public ResponseEntity<PlaylistResponse> updatePlaylist(@PathVariable Long playlistId, @Valid @RequestBody UpdatePlaylistRequest request) {
        return ResponseEntity.ok(playlistService.updatePlaylist(playlistId, request));
    }

    @DeleteMapping("/{playlistId}")
    public ResponseEntity<MessageResponse> deletePlaylist(@PathVariable Long playlistId) {
        playlistService.deletePlaylist(playlistId);

        return ResponseEntity.ok(new MessageResponse("Playlist deleted successfully"));
    }

    @PostMapping("/{playlistId}/tracks")
    public ResponseEntity<MessageResponse> addSongToPlaylist(@PathVariable Long playlistId, @Valid @RequestBody AddSongRequest request) {
        playlistService.addSongToPlaylist(playlistId, request);

        return ResponseEntity.status(HttpStatus.CREATED).body(new MessageResponse("Song added to playlist successfully"));}

    @DeleteMapping("/{playlistId}/tracks/{songId}")
    public ResponseEntity<MessageResponse> removeSongFromPlaylist(@PathVariable Long playlistId, @PathVariable Long songId) {
        playlistService.removeSongFromPlaylist(playlistId, songId);

        return ResponseEntity.ok(new MessageResponse("Song removed from playlist successfully"));
    }

    @PatchMapping("/{playlistId}/tracks/{songId}/position")
    public ResponseEntity<MessageResponse> moveSongInPlaylist(@PathVariable Long playlistId, @PathVariable Long songId, @Valid @RequestBody MoveSongRequest request) {
        playlistService.moveSongInPlaylist(playlistId, songId, request);

        return ResponseEntity.ok(new MessageResponse("Song position updated successfully"));
    }
}