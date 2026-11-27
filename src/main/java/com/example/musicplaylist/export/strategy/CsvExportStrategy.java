package com.example.musicplaylist.export.strategy;

import com.example.musicplaylist.playlist.domain.Playlist;
import com.example.musicplaylist.playlist.domain.PlaylistTrack;
import com.example.musicplaylist.song.domain.Song;
import org.springframework.stereotype.Component;

@Component
public class CsvExportStrategy implements ExportStrategy {

    @Override
    public String getFormat() {
        return "csv";
    }

    @Override
    public String getContentType() {
        return "text/csv";
    }

    @Override
    public String export(Playlist playlist) {
        StringBuilder content = new StringBuilder("position,songId,title,artist,genre,durationSeconds,uri\r\n");

        for (PlaylistTrack track : playlist.getTracks()) {
            Song song = track.getSong();

            content.append(track.getPosition())
                    .append(",")
                    .append(song.getId())
                    .append(",")
                    .append(escape(song.getTitle()))
                    .append(",")
                    .append(escape(song.getArtist()))
                    .append(",")
                    .append(escape(song.getGenre()))
                    .append(",")
                    .append(song.getDurationSeconds())
                    .append(",")
                    .append(escape(song.getUri()))
                    .append("\r\n");
        }

        return content.toString();
    }

    private String escape(String value) {
        if (value.contains(",") || value.contains("\"") || value.contains("\r") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }

        return value;
    }
}