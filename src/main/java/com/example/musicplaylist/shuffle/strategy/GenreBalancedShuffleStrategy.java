package com.example.musicplaylist.shuffle.strategy;

import com.example.musicplaylist.song.domain.Song;
import org.springframework.stereotype.Component;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

@Component
public class GenreBalancedShuffleStrategy implements ShuffleStrategy {

    @Override
    public String getName() {
        return "genre-balanced";
    }

    @Override
    public List<Song> shuffle(List<Song> songs) {

        // Group songs by genre
        Map<String, List<Song>> songsByGenre = songs.stream()
                .collect(Collectors.groupingBy(Song::getGenre));

        List<List<Song>> genreGroups = new ArrayList<>(songsByGenre.values());

        // // Randomize the initial genre order
        Collections.shuffle(genreGroups, ThreadLocalRandom.current());

        Queue<Deque<Song>> genreQueue = new ArrayDeque<>();

        for (List<Song> genreSongs : genreGroups) {
            Collections.shuffle(genreSongs, ThreadLocalRandom.current());

            genreQueue.offer(new ArrayDeque<>(genreSongs));
        }

        List<Song> shuffledSongs = new ArrayList<>(songs.size());

        // Distribute songs across genres using round-robin scheduling
        while (!genreQueue.isEmpty()) {
            Deque<Song> currentGenre = genreQueue.poll();

            shuffledSongs.add(currentGenre.removeFirst());

            if (!currentGenre.isEmpty()) {
                genreQueue.offer(currentGenre);
            }
        }

        return shuffledSongs;
    }
}