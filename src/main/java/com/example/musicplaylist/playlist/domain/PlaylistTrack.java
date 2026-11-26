package com.example.musicplaylist.playlist.domain;

import com.example.musicplaylist.common.exception.BusinessException;
import com.example.musicplaylist.common.exception.ErrorCode;
import com.example.musicplaylist.song.domain.Song;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "playlist_tracks",
        uniqueConstraints = {@UniqueConstraint(name = "uk_playlist_tracks_playlist_song", columnNames = {"playlist_id", "song_id"})})
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

    private PlaylistTrack(Playlist playlist, Song song, int position) {
        this.playlist = playlist;
        this.song = song;
        this.position = position;
    }

    static PlaylistTrack of(Playlist playlist, Song song, int position) {
        return new PlaylistTrack(playlist, song, position);
    }

    void updatePosition(int position) {
        if (position < 0) {
            throw new BusinessException(ErrorCode.INVALID_POSITION);
        }

        this.position = position;
    }
}
