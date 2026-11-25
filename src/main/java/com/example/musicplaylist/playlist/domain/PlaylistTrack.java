package com.example.musicplaylist.playlist.domain;

import com.example.musicplaylist.song.domain.Song;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "playlist_tracks")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PlaylistTrack {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "playlist_id", nullable = false)
    private Playlist playlist;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "song_id", nullable = false)
    private Song song;

    @PositiveOrZero
    @Column(nullable = false)
    private int position;

    PlaylistTrack(Playlist playlist, Song song, int position) {
        this.playlist = playlist;
        this.song = song;
        this.position = position;
    }
}
