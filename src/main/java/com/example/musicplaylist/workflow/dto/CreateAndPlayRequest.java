package com.example.musicplaylist.workflow.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.util.List;

public record CreateAndPlayRequest(

        @NotBlank
        @Size(max = 255)
        String name,

        @Size(max = 1000)
        String description,

        @NotEmpty
        List<@NotNull @Positive Long> songIds,

        @NotNull
        @Positive
        Integer recommendationLimit,

        String shuffleStrategy
) {
}