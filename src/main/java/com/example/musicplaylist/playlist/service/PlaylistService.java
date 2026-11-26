package com.example.musicplaylist.playlist.service;

import com.example.musicplaylist.common.exception.BusinessException;
import com.example.musicplaylist.common.exception.ErrorCode;
import com.example.musicplaylist.playlist.domain.Playlist;
import com.example.musicplaylist.playlist.dto.AddSongRequest;
import com.example.musicplaylist.playlist.dto.CreatePlaylistRequest;
import com.example.musicplaylist.playlist.dto.PlaylistResponse;
import com.example.musicplaylist.playlist.dto.UpdatePlaylistRequest;
import com.example.musicplaylist.playlist.repository.PlaylistRepository;
import com.example.musicplaylist.song.domain.Song;
import com.example.musicplaylist.song.repository.SongRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PlaylistService {

    private final PlaylistRepository playlistRepository;
    private final SongRepository songRepository;

    @Transactional
    public PlaylistResponse createPlaylist(CreatePlaylistRequest request) {
        Playlist playlist = Playlist.of(request.name(), request.description());
        Playlist savedPlaylist = playlistRepository.save(playlist);

        return PlaylistResponse.from(savedPlaylist);
    }

    public List<PlaylistResponse> getAllPlaylists() {
        return playlistRepository.findAll().stream()
                .map(PlaylistResponse::from)
                .toList();
    }

    public PlaylistResponse getPlaylist(Long playlistId) {
        Playlist playlist = findPlaylistById(playlistId);

        return PlaylistResponse.from(playlist);
    }

    @Transactional
    public PlaylistResponse updatePlaylist(Long playlistId, UpdatePlaylistRequest request) {
        Playlist playlist = findPlaylistById(playlistId);

        playlist.rename(request.name());
        playlist.updateDescription(request.description());

        return PlaylistResponse.from(playlist);
    }

    @Transactional
    public void deletePlaylist(Long playlistId) {
        Playlist playlist = findPlaylistById(playlistId);

        playlistRepository.delete(playlist);
    }

    @Transactional
    public void addSongToPlaylist(Long playlistId, AddSongRequest request) {
        Playlist playlist = findPlaylistById(playlistId);
        Song song = findSongById(request.songId());

        playlist.addSong(song);
    }

    @Transactional
    public void removeSongFromPlaylist(Long playlistId, Long songId) {
        Playlist playlist = findPlaylistById(playlistId);

        playlist.removeSong(songId);
    }

    private Playlist findPlaylistById(Long playlistId) {
        return playlistRepository.findById(playlistId)
                .orElseThrow(() ->
                        new BusinessException(ErrorCode.PLAYLIST_NOT_FOUND));
    }

    private Song findSongById(Long songId) {
        return songRepository.findById(songId)
                .orElseThrow(() ->
                        new BusinessException(ErrorCode.SONG_NOT_FOUND));
    }
}