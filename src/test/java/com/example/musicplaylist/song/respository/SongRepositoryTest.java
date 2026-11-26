package com.example.musicplaylist.song.respository;

import com.example.musicplaylist.song.domain.Song;
import com.example.musicplaylist.song.repository.SongRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
public class SongRepositoryTest {
    @Autowired
    private SongRepository songRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    void shouldPersistAndRetrieveSong() {
        Song song = new Song(
                "Yellow",
                "Coldplay",
                "Rock",
                240,
                "music/yellow.mp3"
        );

        Song savedSong = songRepository.saveAndFlush(song);

        assertThat(savedSong.getId()).isNotNull();

        entityManager.clear();
        Song retrievedSong = songRepository.findById(savedSong.getId())
                .orElseThrow();

        assertThat(retrievedSong.getId()).isEqualTo(savedSong.getId());
        assertThat(retrievedSong.getTitle()).isEqualTo("Yellow");
        assertThat(retrievedSong.getArtist()).isEqualTo("Coldplay");
        assertThat(retrievedSong.getGenre()).isEqualTo("Rock");
        assertThat(retrievedSong.getDurationSeconds()).isEqualTo(240);
        assertThat(retrievedSong.getUri()).isEqualTo("music/yellow.mp3");
    }
}
