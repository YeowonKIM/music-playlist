package com.example.musicplaylist.export.strategy;

import com.example.musicplaylist.playlist.domain.Playlist;
import com.example.musicplaylist.song.domain.Song;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

class M3uExportStrategyTest {

    private final ExportStrategy exportStrategy = new M3uExportStrategy();

    @Test
    void shouldExportTracksInOrderAndSanitizeLineBreaks() {
        Song yellow = createSong(2L, "Yellow\nLive", "Coldplay", 269, "music/yellow.mp3");
        Song hello = createSong(1L, "Hello", "Adele\rLive", 295, "music/hello.mp3");

        Playlist playlist = Playlist.of("Favorites", null);
        playlist.addSong(yellow);
        playlist.addSong(hello);

        String content = exportStrategy.export(playlist);

        assertThat(content).isEqualTo("""
                #EXTM3U
                #EXTINF:269,Coldplay - Yellow Live
                music/yellow.mp3
                #EXTINF:295,Adele Live - Hello
                music/hello.mp3
                """);
    }

    @Test
    void shouldExportOnlyHeaderWhenPlaylistIsEmpty() {
        Playlist playlist = Playlist.of("Empty Playlist", null);

        String content = exportStrategy.export(playlist);

        assertThat(content).isEqualTo("#EXTM3U\n");
    }

    private Song createSong(Long id, String title, String artist, int durationSeconds, String uri) {
        Song song = Song.builder()
                .title(title)
                .artist(artist)
                .genre("Rock")
                .durationSeconds(durationSeconds)
                .uri(uri)
                .build();

        ReflectionTestUtils.setField(song, "id", id);

        return song;
    }
}