package com.example.musicplaylist.workflow.dto;

import com.example.musicplaylist.player.dto.PlaybackStateResponse;
import com.example.musicplaylist.playlist.dto.PlaylistDetailResponse;
import com.example.musicplaylist.song.dto.SongResponse;

import java.util.List;

public record CreateAndPlayResponse(
        PlaylistDetailResponse playlist,
        List<SongResponse> recommendations,
        PlaybackStateResponse playbackState
) {
}