package com.example.musicplaylist.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    INVALID_REQUEST("Invalid request"),
    VALIDATION_FAILED("Request validation failed"),
    PLAYLIST_NOT_FOUND("Playlist not found"),
    INVALID_SONG("Song must not be null"),
    SONG_NOT_FOUND("Song not found"),
    SONG_NOT_IN_PLAYLIST("Song not found in the playlist"),
    DUPLICATE_SONG("Song already exists in the playlist"),
    INVALID_POSITION("Invalid track position"),
    INVALID_SHUFFLE_STRATEGY("Invalid shuffle strategy"),
    INTERNAL_SERVER_ERROR("An unexpected error occurred");

    private final String message;
}