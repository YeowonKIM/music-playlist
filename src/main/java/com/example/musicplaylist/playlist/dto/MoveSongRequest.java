package com.example.musicplaylist.playlist.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public record MoveSongRequest(
        @NotNull
        @PositiveOrZero
        Integer newPosition
) {
}