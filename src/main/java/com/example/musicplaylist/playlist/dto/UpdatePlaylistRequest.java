package com.example.musicplaylist.playlist.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdatePlaylistRequest(

        @NotBlank
        @Size(max = 255)
        String name,

        @Size(max = 1000)
        String description

) {
}