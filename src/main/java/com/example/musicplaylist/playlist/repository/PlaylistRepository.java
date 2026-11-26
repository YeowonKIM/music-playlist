package com.example.musicplaylist.playlist.repository;

import com.example.musicplaylist.playlist.domain.Playlist;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlaylistRepository extends JpaRepository<Playlist, Long> {
}
