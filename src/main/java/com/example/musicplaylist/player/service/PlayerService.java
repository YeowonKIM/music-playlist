package com.example.musicplaylist.player.service;

import com.example.musicplaylist.common.exception.BusinessException;
import com.example.musicplaylist.common.exception.ErrorCode;
import com.example.musicplaylist.player.domain.MusicPlayer;
import com.example.musicplaylist.player.dto.PlaybackStateResponse;
import com.example.musicplaylist.player.dto.PlayRequest;
import com.example.musicplaylist.playlist.domain.Playlist;
import com.example.musicplaylist.playlist.repository.PlaylistRepository;
import com.example.musicplaylist.shuffle.service.ShuffleStrategyRegistry;
import com.example.musicplaylist.song.domain.Song;
import com.example.musicplaylist.song.dto.SongResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PlayerService {

    private final PlaylistRepository playlistRepository;
    private final ShuffleStrategyRegistry shuffleStrategyRegistry;
    private final MusicPlayer musicPlayer;

    @Transactional(readOnly = true)
    public PlaybackStateResponse play(PlayRequest request) {
        Playlist playlist = playlistRepository.findByIdWithTracks(request.playlistId())
                .orElseThrow(() -> new BusinessException(ErrorCode.PLAYLIST_NOT_FOUND));

        List<Song> songs = playlist.getTracks().stream()
                .map(track -> track.getSong())
                .toList();

        if (request.shuffleStrategy() != null) {
            songs = shuffleStrategyRegistry.getStrategy(request.shuffleStrategy())
                    .shuffle(songs);
        }

        List<SongResponse> playbackQueue = songs.stream()
                .map(SongResponse::from)
                .toList();

        return PlaybackStateResponse.from(musicPlayer.play(playlist.getId(), playbackQueue, request.shuffleStrategy()));
    }

    public PlaybackStateResponse pause() {
        return PlaybackStateResponse.from(musicPlayer.pause());
    }

    public PlaybackStateResponse resume() {
        return PlaybackStateResponse.from(musicPlayer.resume());
    }

    public PlaybackStateResponse stop() {
        return PlaybackStateResponse.from(musicPlayer.stop());
    }

    public PlaybackStateResponse next() {
        return PlaybackStateResponse.from(musicPlayer.next());
    }

    public PlaybackStateResponse previous() {
        return PlaybackStateResponse.from(musicPlayer.previous());
    }

    public PlaybackStateResponse getState() {
        return PlaybackStateResponse.from(musicPlayer.getState());
    }
}