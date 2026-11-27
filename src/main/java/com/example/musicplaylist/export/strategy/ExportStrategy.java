package com.example.musicplaylist.export.strategy;

import com.example.musicplaylist.playlist.domain.Playlist;

public interface ExportStrategy {

    String getFormat();

    String getContentType();

    String export(Playlist playlist);
}