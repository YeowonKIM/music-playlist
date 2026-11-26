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
    void shouldPersistAndRetrievePlaylist() {
        Playlist playlist = new Playlist("My Favorites", "My favorite songs");
        Playlist savedPlaylist = playlistRepository.saveAndFlush(playlist);

        assertThat(savedPlaylist.getId()).isNotNull();

        entityManager.clear();

        Playlist retrievedPlaylist = playlistRepository.findById(savedPlaylist.getId()).orElseThrow();

        assertThat(retrievedPlaylist.getId()).isEqualTo(savedPlaylist.getId());
        assertThat(retrievedPlaylist.getName()).isEqualTo("My Favorites");
        assertThat(retrievedPlaylist.getDescription()).isEqualTo("My favorite songs");
        assertThat(retrievedPlaylist.getTracks()).isEmpty();
    }

    @Test
    void shouldPersistPlaylistWithTracks() {
        Song firstSong = songRepository.save(createSong());
        Song secondSong = songRepository.save(new Song("Hello", "Adele", "Pop", 295, "music/hello.mp3"));

        Playlist playlist = new Playlist("My Favorites", null);
        playlist.addSong(firstSong);
        playlist.addSong(secondSong);

        Playlist savedPlaylist = playlistRepository.saveAndFlush(playlist);

        entityManager.clear();

        Playlist retrievedPlaylist = playlistRepository.findById(savedPlaylist.getId()).orElseThrow();

        assertThat(retrievedPlaylist.getTracks()).hasSize(2);

        assertThat(retrievedPlaylist.getTracks())
                .allSatisfy(track -> {
                    assertThat(track.getId()).isNotNull();
                    assertThat(track.getPlaylist().getId())
                            .isEqualTo(retrievedPlaylist.getId());
                });

        assertThat(retrievedPlaylist.getTracks())
                .extracting(track -> track.getSong().getId())
                .containsExactly(firstSong.getId(), secondSong.getId());

        assertThat(retrievedPlaylist.getTracks())
                .extracting(PlaylistTrack::getPosition)
                .containsExactly(0, 1);
    }

    @Test
    void shouldRetrieveTracksInPositionOrder() {
        Song firstSong = songRepository.save(createSong());
        Song secondSong = songRepository.save(new Song("Hello", "Adele", "Pop", 295, "music/hello.mp3"));

        Playlist playlist = new Playlist("My Favorites", null);
        playlist.addSong(firstSong);
        playlist.addSong(secondSong);

        Playlist savedPlaylist = playlistRepository.saveAndFlush(playlist);

        entityManager.createQuery("""
            UPDATE PlaylistTrack t
            SET t.position = 1 - t.position
            WHERE t.playlist.id = :playlistId
            """)
                .setParameter("playlistId", savedPlaylist.getId())
                .executeUpdate();

        entityManager.clear();

        Playlist retrievedPlaylist = playlistRepository
                .findById(savedPlaylist.getId())
                .orElseThrow();

        assertThat(retrievedPlaylist.getTracks())
                .extracting(PlaylistTrack::getPosition)
                .containsExactly(0, 1);

        assertThat(retrievedPlaylist.getTracks())
                .extracting(track -> track.getSong().getId())
                .containsExactly(secondSong.getId(), firstSong.getId());
    }

    @Test
    void shouldRejectDuplicateSongWithSameId() {
        Song song = songRepository.saveAndFlush(createSong());

        Playlist playlist = new Playlist("My Favorites", null);
        playlist.addSong(song);

        entityManager.detach(song);

        Song retrievedSong = songRepository.findById(song.getId())
                .orElseThrow();

        assertThat(retrievedSong).isNotSameAs(song);
        assertThat(retrievedSong.getId()).isEqualTo(song.getId());

        assertThatThrownBy(() -> playlist.addSong(retrievedSong))
                .isInstanceOfSatisfying(
                        BusinessException.class,
                        exception -> assertThat(exception.getErrorCode())
                                .isEqualTo(ErrorCode.DUPLICATE_SONG)
                );

        assertThat(playlist.getTracks()).hasSize(1);
    }

    @Test
    void shouldEnforceUniquePlaylistSongConstraint() {
        Song song = songRepository.saveAndFlush(createSong());

        Playlist playlist = new Playlist("My Favorites", null);
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

        Playlist playlist = new Playlist("My Favorites", null);
        playlist.addSong(song);

        Playlist savedPlaylist = playlistRepository.saveAndFlush(playlist);

        Long playlistId = savedPlaylist.getId();
        Long songId = song.getId();
        Long trackId = savedPlaylist.getTracks().get(0).getId();

        playlistRepository.delete(savedPlaylist);
        playlistRepository.flush();

        entityManager.clear();

        assertThat(playlistRepository.findById(playlistId)).isEmpty();

        assertThat(entityManager.find(PlaylistTrack.class, trackId))
                .isNull();

        assertThat(songRepository.findById(songId)).isPresent();
    }

    private Song createSong() {
        return new Song(
                "Yellow", "Coldplay", "Rock", 240, "music/yellow.mp3"
        );
    }
}
