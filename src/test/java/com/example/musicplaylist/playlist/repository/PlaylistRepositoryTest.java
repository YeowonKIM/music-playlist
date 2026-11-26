package com.example.musicplaylist.playlist.repository;

import com.example.musicplaylist.common.exception.BusinessException;
import com.example.musicplaylist.common.exception.ErrorCode;
import com.example.musicplaylist.playlist.domain.Playlist;
import com.example.musicplaylist.playlist.domain.PlaylistTrack;
import com.example.musicplaylist.song.domain.Song;
import com.example.musicplaylist.song.repository.SongRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.hibernate.exception.ConstraintViolationException;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
public class PlaylistRepositoryTest {
    @Autowired
    private PlaylistRepository playlistRepository;

    @Autowired
    private SongRepository songRepository;

    @PersistenceContext
    private EntityManager entityManager;

    @Test
    void shouldRejectDuplicateSongWithSameId() {
        Song song = songRepository.saveAndFlush(createSong());

        Playlist playlist = Playlist.of("My Favorites", null);
        playlist.addSong(song);

        entityManager.detach(song);

        Song retrievedSong = songRepository.findById(song.getId())
                .orElseThrow();

        assertThat(retrievedSong).isNotSameAs(song);
        assertThat(retrievedSong.getId()).isEqualTo(song.getId());

        assertThatThrownBy(() -> playlist.addSong(retrievedSong))
                .isInstanceOfSatisfying(
                        BusinessException.class,
                        exception -> assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.DUPLICATE_SONG)
                );

        assertThat(playlist.getTracks()).hasSize(1);
    }

    @Test
    void shouldEnforceUniquePlaylistSongConstraint() {
        Song song = songRepository.saveAndFlush(createSong());

        Playlist playlist = Playlist.of("My Favorites", null);
        playlist.addSong(song);
        Playlist savedPlaylist = playlistRepository.saveAndFlush(playlist);

        assertThatThrownBy(() -> entityManager.createNativeQuery("""
            INSERT INTO playlist_tracks (playlist_id, song_id, position)
            VALUES (:playlistId, :songId, 1)
            """)
                .setParameter("playlistId", savedPlaylist.getId())
                .setParameter("songId", song.getId())
                .executeUpdate())
                .isInstanceOf(
                        ConstraintViolationException.class
                );
    }

    @Test
    void shouldDeleteTracksWithoutDeletingSongs() {
        Song song = songRepository.saveAndFlush(createSong());

        Playlist playlist = Playlist.of("My Favorites", null);
        playlist.addSong(song);

        Playlist savedPlaylist = playlistRepository.saveAndFlush(playlist);

        Long playlistId = savedPlaylist.getId();
        Long songId = song.getId();
        Long trackId = savedPlaylist.getTracks().get(0).getId();

        playlistRepository.delete(savedPlaylist);
        playlistRepository.flush();

        entityManager.clear();

        assertThat(playlistRepository.findById(playlistId)).isEmpty();
        assertThat(entityManager.find(PlaylistTrack.class, trackId)).isNull();
        assertThat(songRepository.findById(songId)).isPresent();
    }

    @Test
    void shouldPersistReorderedTracksWithoutReplacingExistingTracks() {
        Song firstSong = songRepository.save(createSong());
        Song secondSong = songRepository.save(createSong());
        Song thirdSong = songRepository.save(createSong());

        Playlist playlist = Playlist.of("My Playlist", "Description");
        playlist.addSong(firstSong);
        playlist.addSong(secondSong);
        playlist.addSong(thirdSong);

        playlistRepository.saveAndFlush(playlist);

        Long playlistId = playlist.getId();

        List<Long> originalTrackIds = playlist.getTracks().stream()
                .map(PlaylistTrack::getId)
                .toList();

        entityManager.clear();

        Playlist managedPlaylist = playlistRepository.findById(playlistId)
                .orElseThrow();

        managedPlaylist.moveSong(firstSong.getId(), 2);

        entityManager.flush();
        entityManager.clear();

        Playlist reloadedPlaylist = playlistRepository.findById(playlistId)
                .orElseThrow();

        List<PlaylistTrack> tracks = reloadedPlaylist.getTracks();

        assertThat(tracks)
                .extracting(track -> track.getSong().getId())
                .containsExactly(
                        secondSong.getId(),
                        thirdSong.getId(),
                        firstSong.getId()
                );

        assertThat(tracks)
                .extracting(PlaylistTrack::getPosition)
                .containsExactly(0, 1, 2);

        assertThat(tracks)
                .extracting(PlaylistTrack::getId)
                .containsExactly(
                        originalTrackIds.get(1),
                        originalTrackIds.get(2),
                        originalTrackIds.get(0)
                );
    }

    private Song createSong() {
        return Song.builder()
                .title("Yellow")
                .artist("Coldplay")
                .genre("Rock")
                .durationSeconds(240)
                .uri("music/yellow.mp3")
                .build();
    }
}