package com.example.musicplaylist.playlist.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record AddSongRequest(
        @NotNull
        @Positive
        Long songId
) {
}