package com.example.musicplaylist.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // Common request errors
    INVALID_REQUEST("Invalid request"),
    VALIDATION_FAILED("Request validation failed"),

    // Playlist errors
    PLAYLIST_NOT_FOUND("Playlist not found"),

    // Song errors
    INVALID_SONG("Song must not be null"),
    SONG_NOT_FOUND("Song not found"),

    // Playlist track errors
    SONG_NOT_IN_PLAYLIST("Song not found in the playlist"),
    DUPLICATE_SONG("Song already exists in the playlist"),
    INVALID_POSITION("Invalid track position"),

    // Shuffle errors
    INVALID_SHUFFLE_STRATEGY("Invalid shuffle strategy"),

    // Export errors
    INVALID_EXPORT_FORMAT("Invalid export format"),

    // Playback errors
    EMPTY_PLAYLIST("Cannot play an empty playlist"),
    INVALID_PLAYBACK_STATE("Invalid playback state"),
    INVALID_STOPPED_PLAYBACK_STATE("Invalid stopped playback state"),
    INVALID_ACTIVE_PLAYBACK_STATE("Invalid active playback state"),

    // Internal server errors
    INTERNAL_SERVER_ERROR("An unexpected error occurred");

    private final String message;
}