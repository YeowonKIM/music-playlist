package com.example.musicplaylist.export.strategy;

import com.example.musicplaylist.playlist.domain.Playlist;
import com.example.musicplaylist.song.domain.Song;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

class CsvExportStrategyTest {

    private final ExportStrategy exportStrategy = new CsvExportStrategy();

    @Test
    void shouldExportTracksInOrderAndEscapeSpecialCharacters() {
        Song yellow = createSong(2L, "Yellow", "Coldplay", "Rock", "music/yellow.mp3");
        Song hello = createSong(1L, "Hello, \"Live\"", "Adele\rLive", "Pop", "music/hello.mp3");

        Playlist playlist = Playlist.of("Favorites", null);
        playlist.addSong(yellow);
        playlist.addSong(hello);

        String content = exportStrategy.export(playlist);

        String expected = "position,songId,title,artist,genre,durationSeconds,uri\r\n"
                + "0,2,Yellow,Coldplay,Rock,240,music/yellow.mp3\r\n"
                + "1,1,\"Hello, \"\"Live\"\"\",\"Adele\rLive\",Pop,240,music/hello.mp3\r\n";

        assertThat(content).isEqualTo(expected);
    }

    @Test
    void shouldExportOnlyHeaderWhenPlaylistIsEmpty() {
        Playlist playlist = Playlist.of("Empty Playlist", null);

        String content = exportStrategy.export(playlist);

        assertThat(content).isEqualTo("position,songId,title,artist,genre,durationSeconds,uri\r\n");
    }

    private Song createSong(Long id, String title, String artist, String genre, String uri) {
        Song song = Song.builder()
                .title(title)
                .artist(artist)
                .genre(genre)
                .durationSeconds(240)
                .uri(uri)
                .build();

        ReflectionTestUtils.setField(song, "id", id);

        return song;
    }
}