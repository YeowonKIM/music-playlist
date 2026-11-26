package com.example.musicplaylist.song.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "songs")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Song {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(max = 255)
    @Column(nullable = false)
    private String title;

    @NotBlank
    @Size(max = 255)
    @Column(nullable = false)
    private String artist;

    @NotBlank
    @Size(max = 255)
    @Column(nullable = false)
    private String genre;

    @Positive
    @Column(nullable = false)
    private int durationSeconds;

    @NotBlank
    @Size(max = 2048)
    @Column(nullable = false, length = 2048)
    private String uri;

    @Builder
    private Song(String title, String artist, String genre, int durationSeconds, String uri) {
        this.title = title;
        this.artist = artist;
        this.genre = genre;
        this.durationSeconds = durationSeconds;
        this.uri = uri;
    }
}
