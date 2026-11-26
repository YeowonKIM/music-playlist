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
    DUPLICATE_SONG("Song already exists in the playlist"),
    INTERNAL_SERVER_ERROR("An unexpected error occurred");

    private final String message;
}