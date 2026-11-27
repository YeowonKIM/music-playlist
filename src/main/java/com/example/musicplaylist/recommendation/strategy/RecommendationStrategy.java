package com.example.musicplaylist.recommendation.strategy;

import com.example.musicplaylist.song.domain.Song;

import java.util.List;

public interface RecommendationStrategy {

    List<Song> recommend(List<Song> playlistSongs, List<Song> candidates, int limit);
}