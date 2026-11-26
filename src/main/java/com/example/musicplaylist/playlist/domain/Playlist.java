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
import java.util.Collections;
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

    public void moveSong(Long songId, int newPosition) {
        if (songId == null) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST);
        }

        if (newPosition < 0 || newPosition >= tracks.size()) {
            throw new BusinessException(ErrorCode.INVALID_POSITION);
        }

        int currentPosition = findTrackIndex(songId);

        if (currentPosition == newPosition) {
            return;
        }

        int start = Math.min(currentPosition, newPosition);
        int end = Math.max(currentPosition, newPosition);
        int distance = currentPosition < newPosition ? -1 : 1;

        Collections.rotate(tracks.subList(start, end + 1), distance);

        updateTrackPositions(start, end);
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

    private int findTrackIndex(Long songId) {
        for (int i = 0; i < tracks.size(); i++) {
            if (songId.equals(tracks.get(i).getSong().getId())) {
                return i;
            }
        }

        throw new BusinessException(ErrorCode.SONG_NOT_IN_PLAYLIST);
    }

    private void updateTrackPositions() {
        updateTrackPositions(0, tracks.size() - 1);
    }

    private void updateTrackPositions(int start, int end) {
        for (int i = start; i <= end; i++) {
            tracks.get(i).updatePosition(i);
        }
    }

    public void rename(String name) {
        this.name = name;
    }

    public void updateDescription(String description) {
        this.description = description;
    }
}
