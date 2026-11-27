package com.example.musicplaylist.export.strategy;

import com.example.musicplaylist.playlist.domain.Playlist;
import com.example.musicplaylist.song.domain.Song;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import static org.assertj.core.api.Assertions.assertThat;

class JsonExportStrategyTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    private final ExportStrategy exportStrategy = new JsonExportStrategy(objectMapper);

    @Test
    void shouldExportPlaylistMetadataAndTracksInOrder() {
        Song yellow = createSong(2L, "Yellow", "Coldplay", "Rock");
        Song hello = createSong(1L, "Hello", "Adele", "Pop");

        Playlist playlist = Playlist.of("Favorites", "Favorite songs");
        ReflectionTestUtils.setField(playlist, "id", 1L);

        playlist.addSong(yellow);
        playlist.addSong(hello);

        String content = exportStrategy.export(playlist);

        JsonNode json = objectMapper.readTree(content);
        JsonNode tracks = json.get("tracks");

        assertThat(json.get("id").asLong()).isEqualTo(1L);
        assertThat(json.get("name").asString()).isEqualTo("Favorites");
        assertThat(json.get("description").asString()).isEqualTo("Favorite songs");

        assertThat(tracks.size()).isEqualTo(2);

        assertThat(tracks.get(0).get("songId").asLong()).isEqualTo(2L);
        assertThat(tracks.get(0).get("title").asString()).isEqualTo("Yellow");
        assertThat(tracks.get(0).get("artist").asString()).isEqualTo("Coldplay");
        assertThat(tracks.get(0).get("genre").asString()).isEqualTo("Rock");
        assertThat(tracks.get(0).get("durationSeconds").asInt()).isEqualTo(240);
        assertThat(tracks.get(0).get("uri").asString()).isEqualTo("music/Yellow.mp3");
        assertThat(tracks.get(0).get("position").asInt()).isZero();

        assertThat(tracks.get(1).get("songId").asLong()).isEqualTo(1L);
        assertThat(tracks.get(1).get("position").asInt()).isEqualTo(1);
    }

    @Test
    void shouldExportEmptyPlaylist() {
        Playlist playlist = Playlist.of("Empty Playlist", null);

        String content = exportStrategy.export(playlist);

        JsonNode json = objectMapper.readTree(content);

        assertThat(json.get("name").asString()).isEqualTo("Empty Playlist");
        assertThat(json.get("tracks").isArray()).isTrue();
        assertThat(json.get("tracks").size()).isZero();
    }

    private Song createSong(Long id, String title, String artist, String genre) {
        Song song = Song.builder()
                .title(title)
                .artist(artist)
                .genre(genre)
                .durationSeconds(240)
                .uri("music/" + title + ".mp3")
                .build();

        ReflectionTestUtils.setField(song, "id", id);

        return song;
    }
}