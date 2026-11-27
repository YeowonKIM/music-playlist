package com.example.musicplaylist.player.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record PlayRequest(
        @NotNull @Positive
        Long playlistId,
        String shuffleStrategy
) {
}