package com.example.musicplaylist.playlist.repository;

import com.example.musicplaylist.playlist.domain.Playlist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PlaylistRepository extends JpaRepository<Playlist, Long> {

    @Query("""
            SELECT DISTINCT p
            FROM Playlist p
            LEFT JOIN FETCH p.tracks t
            LEFT JOIN FETCH t.song
            """)
    List<Playlist> findAllWithTracks();

    @Query("""
            SELECT DISTINCT p
            FROM Playlist p
            LEFT JOIN FETCH p.tracks t
            LEFT JOIN FETCH t.song
            WHERE p.id = :playlistId
            """)
    Optional<Playlist> findByIdWithTracks(
            @Param("playlistId") Long playlistId
    );
}