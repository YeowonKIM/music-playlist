package com.example.musicplaylist.song.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record CreateSongRequest(

        @NotBlank
        @Size(max = 255)
        String title,

        @NotBlank
        @Size(max = 255)
        String artist,

        @NotBlank
        @Size(max = 255)
        String genre,

        @NotNull
        @Positive
        Integer durationSeconds,

        @NotBlank
        @Size(max = 2048)
        String uri
) {
}