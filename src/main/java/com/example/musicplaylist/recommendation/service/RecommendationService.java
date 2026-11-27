package com.example.musicplaylist.recommendation.service;

import com.example.musicplaylist.common.exception.BusinessException;
import com.example.musicplaylist.common.exception.ErrorCode;
import com.example.musicplaylist.playlist.domain.Playlist;
import com.example.musicplaylist.playlist.domain.PlaylistTrack;
import com.example.musicplaylist.playlist.repository.PlaylistRepository;
import com.example.musicplaylist.recommendation.strategy.RecommendationStrategy;
import com.example.musicplaylist.song.domain.Song;
import com.example.musicplaylist.song.dto.SongResponse;
import com.example.musicplaylist.song.repository.SongRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RecommendationService {

    private final PlaylistRepository playlistRepository;
    private final SongRepository songRepository;
    private final RecommendationStrategy recommendationStrategy;

    public List<SongResponse> recommendSongs(Long playlistId, int limit) {
        if (limit <= 0) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST);
        }

        Playlist playlist = playlistRepository.findByIdWithTracks(playlistId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PLAYLIST_NOT_FOUND));

        List<Song> playlistSongs = playlist.getTracks().stream()
                .map(PlaylistTrack::getSong)
                .toList();

        if (playlistSongs.isEmpty()) {
            return List.of();
        }

        List<Song> candidates = songRepository.findAll();

        return recommendationStrategy.recommend(playlistSongs, candidates, limit)
                .stream()
                .map(SongResponse::from)
                .toList();
    }
}