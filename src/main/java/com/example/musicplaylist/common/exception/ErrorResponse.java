package com.example.musicplaylist.common.exception;

public record ErrorResponse(
        int status,
        ErrorCode code,
        String message
) {
}