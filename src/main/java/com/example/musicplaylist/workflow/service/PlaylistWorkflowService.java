package com.example.musicplaylist.workflow.service;

import com.example.musicplaylist.player.dto.PlayRequest;
import com.example.musicplaylist.player.dto.PlaybackStateResponse;
import com.example.musicplaylist.player.service.PlayerService;
import com.example.musicplaylist.playlist.dto.AddSongRequest;
import com.example.musicplaylist.playlist.dto.CreatePlaylistRequest;
import com.example.musicplaylist.playlist.dto.PlaylistDetailResponse;
import com.example.musicplaylist.playlist.dto.PlaylistResponse;
import com.example.musicplaylist.playlist.service.PlaylistService;
import com.example.musicplaylist.recommendation.service.RecommendationService;
import com.example.musicplaylist.shuffle.service.ShuffleStrategyRegistry;
import com.example.musicplaylist.song.dto.SongResponse;
import com.example.musicplaylist.workflow.dto.CreateAndPlayRequest;
import com.example.musicplaylist.workflow.dto.CreateAndPlayResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class PlaylistWorkflowService {

    private final PlaylistService playlistService;
    private final RecommendationService recommendationService;
    private final PlayerService playerService;
    private final ShuffleStrategyRegistry shuffleStrategyRegistry;
    private final TransactionTemplate transactionTemplate;

    public CreateAndPlayResponse createAndPlay(CreateAndPlayRequest request) {

        if (request.shuffleStrategy() != null) {
            shuffleStrategyRegistry.getStrategy(request.shuffleStrategy());
        }

        PreparedPlaylist preparedPlaylist = Objects.requireNonNull(transactionTemplate.execute(status -> preparePlaylist(request)));

        PlaybackStateResponse playbackState = playerService.play(new PlayRequest(preparedPlaylist.playlist().id(), request.shuffleStrategy()));

        return new CreateAndPlayResponse(preparedPlaylist.playlist(), preparedPlaylist.recommendations(), playbackState);
    }

    private PreparedPlaylist preparePlaylist(CreateAndPlayRequest request) {

        PlaylistResponse playlist = playlistService.createPlaylist(new CreatePlaylistRequest(request.name(), request.description()));

        Long playlistId = playlist.id();

        for (Long songId : request.songIds()) {
            playlistService.addSongToPlaylist(playlistId, new AddSongRequest(songId));
        }

        List<SongResponse> recommendations = recommendationService.recommendSongs(playlistId, request.recommendationLimit());

        for (SongResponse song : recommendations) {
            playlistService.addSongToPlaylist(playlistId, new AddSongRequest(song.id()));
        }

        PlaylistDetailResponse playlistDetails = playlistService.getPlaylistDetails(playlistId);

        return new PreparedPlaylist(playlistDetails, recommendations);
    }

    private record PreparedPlaylist(
            PlaylistDetailResponse playlist,
            List<SongResponse> recommendations
    ) {
    }
}