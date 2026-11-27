package com.example.musicplaylist.export.strategy;

import com.example.musicplaylist.playlist.domain.Playlist;
import com.example.musicplaylist.playlist.domain.PlaylistTrack;
import com.example.musicplaylist.song.domain.Song;
import org.springframework.stereotype.Component;

@Component
public class M3uExportStrategy implements ExportStrategy {

    @Override
    public String getFormat() {
        return "m3u";
    }

    @Override
    public String getContentType() {
        return "audio/x-mpegurl";
    }

    @Override
    public String export(Playlist playlist) {
        StringBuilder content = new StringBuilder("#EXTM3U\n");

        for (PlaylistTrack track : playlist.getTracks()) {
            Song song = track.getSong();

            content.append("#EXTINF:")
                    .append(song.getDurationSeconds())
                    .append(",")
                    .append(sanitize(song.getArtist()))
                    .append(" - ")
                    .append(sanitize(song.getTitle()))
                    .append("\n")
                    .append(sanitize(song.getUri()))
                    .append("\n");
        }

        return content.toString();
    }

    private String sanitize(String value) {
        return value.replace("\r", " ").replace("\n", " ");}
}