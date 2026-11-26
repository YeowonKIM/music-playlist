package com.example.musicplaylist.shuffle.service;

import com.example.musicplaylist.common.exception.BusinessException;
import com.example.musicplaylist.common.exception.ErrorCode;
import com.example.musicplaylist.playlist.domain.Playlist;
import com.example.musicplaylist.playlist.domain.PlaylistTrack;
import com.example.musicplaylist.playlist.repository.PlaylistRepository;
import com.example.musicplaylist.shuffle.strategy.ShuffleStrategy;
import com.example.musicplaylist.song.domain.Song;
import com.example.musicplaylist.song.dto.SongResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ShuffleService {

    private final PlaylistRepository playlistRepository;
    private final ShuffleStrategyRegistry shuffleStrategyRegistry;

    public List<String> getAvailableStrategies() {
        return shuffleStrategyRegistry.getAvailableStrategies();
    }

    public List<SongResponse> shufflePlaylist(Long playlistId, String strategyName) {
        ShuffleStrategy strategy = shuffleStrategyRegistry.getStrategy(strategyName);

        Playlist playlist = playlistRepository.findByIdWithTracks(playlistId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PLAYLIST_NOT_FOUND));

        List<Song> songs = playlist.getTracks().stream()
                .map(PlaylistTrack::getSong)
                .toList();

        return strategy.shuffle(songs).stream()
                .map(SongResponse::from)
                .toList();
    }
}