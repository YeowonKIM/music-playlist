package com.example.musicplaylist.song.repository;

import com.example.musicplaylist.song.domain.Song;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SongRepository extends JpaRepository<Song, Long> {
}
