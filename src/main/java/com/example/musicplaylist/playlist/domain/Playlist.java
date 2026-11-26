package com.example.musicplaylist.playlist.domain;

import com.example.musicplaylist.common.exception.BusinessException;
import com.example.musicplaylist.common.exception.ErrorCode;
import com.example.musicplaylist.song.domain.Song;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Getter
@Entity
@Table(name = "playlists")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Playlist {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(max = 255)
    @Column(nullable = false)
    private String name;

    @Size(max = 1000)
    @Column(length = 1000)
    private String description;

    @OneToMany(mappedBy = "playlist", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("position ASC")
    private List<PlaylistTrack> tracks = new ArrayList<>();

    private Playlist(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public static Playlist of(String name, String description) {
        return new Playlist(name, description);
    }

    public List<PlaylistTrack> getTracks() {
        return List.copyOf(tracks);
    }

    public void addSong(Song song) {
        if (song == null) {
            throw new BusinessException(ErrorCode.INVALID_SONG);
        }

        boolean alreadyExists = tracks.stream()
                .anyMatch(track -> track.getSong() == song ||
                        (song.getId() != null &&
                                song.getId().equals(track.getSong().getId())));

        if (alreadyExists) {
            throw new BusinessException(ErrorCode.DUPLICATE_SONG);
        }

        tracks.add(PlaylistTrack.of(this, song, tracks.size()));
    }

    public void rename(String name) {
        this.name = name;
    }

    public void updateDescription(String description) {
        this.description = description;
    }

    public void removeSong(Long songId) {
        if (songId == null) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST);
        }

        boolean removed = tracks.removeIf(
                track -> songId.equals(track.getSong().getId())
        );

        if (!removed) {
            throw new BusinessException(ErrorCode.SONG_NOT_IN_PLAYLIST);
        }

        updateTrackPositions();
    }

    private void updateTrackPositions() {
        for (int i = 0; i < tracks.size(); i++) {
            tracks.get(i).updatePosition(i);
        }
    }
}
