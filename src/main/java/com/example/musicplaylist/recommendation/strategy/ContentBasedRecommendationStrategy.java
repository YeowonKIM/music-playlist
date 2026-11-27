package com.example.musicplaylist.recommendation.strategy;

import com.example.musicplaylist.song.domain.Song;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class ContentBasedRecommendationStrategy implements RecommendationStrategy {

    private static final double GENRE_WEIGHT = 0.7;
    private static final double ARTIST_WEIGHT = 0.3;

    private final GenreCompatibility genreCompatibility;

    @Override
    public List<Song> recommend(List<Song> playlistSongs, List<Song> candidates, int limit) {
        if (limit <= 0) {
            throw new IllegalArgumentException("Limit must be positive");
        }

        if (playlistSongs.isEmpty() || candidates.isEmpty()) {
            return List.of();
        }

        Map<String, Long> genreCounts = new HashMap<>();
        Map<String, Long> artistCounts = new HashMap<>();
        Set<Long> existingSongIds = new HashSet<>();

        for (Song song : playlistSongs) {
            genreCounts.merge(song.getGenre(), 1L, Long::sum);
            artistCounts.merge(song.getArtist(), 1L, Long::sum);
            existingSongIds.add(song.getId());
        }

        return candidates.stream()
                .filter(song -> !existingSongIds.contains(song.getId()))
                .map(song -> new ScoredSong(song, calculateScore(song, genreCounts, artistCounts, playlistSongs.size())))
                .filter(scoredSong -> scoredSong.score() > 0.0)
                .sorted(
                        Comparator.comparingDouble(ScoredSong::score)
                                .reversed()
                                .thenComparing(scoredSong -> scoredSong.song().getId())
                )
                .limit(limit)
                .map(ScoredSong::song)
                .toList();
    }

    private double calculateScore(Song candidate, Map<String, Long> genreCounts, Map<String, Long> artistCounts, int totalSongs) {
        double genreScore = genreCounts.entrySet().stream()
                .mapToDouble(entry -> entry.getValue() * genreCompatibility.getCompatibility(entry.getKey(), candidate.getGenre())) // number of songs × compatibility
                .sum() / totalSongs;

        double artistScore = (double) artistCounts.getOrDefault(candidate.getArtist(), 0L) / totalSongs;

        return GENRE_WEIGHT * genreScore + ARTIST_WEIGHT * artistScore;
    }

    private record ScoredSong(Song song, double score) {
    }
}