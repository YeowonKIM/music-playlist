package com.example.musicplaylist.shuffle.strategy;

import com.example.musicplaylist.song.domain.Song;
import org.springframework.stereotype.Component;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

@Component
public class SmartShuffleStrategy implements ShuffleStrategy {

    @Override
    public String getName() {
        return "smart";
    }

    @Override
    public List<Song> shuffle(List<Song> songs) {

        // Group songs by artist
        Map<String, List<Song>> songsByArtist = songs.stream()
                .collect(Collectors.groupingBy(Song::getArtist));

        // Prioritize artists with the most remaining songs
        Queue<Deque<Song>> artistQueue = new PriorityQueue<>(Comparator.<Deque<Song>>comparingInt(Deque::size).reversed());

        List<List<Song>> artistGroups = new ArrayList<>(songsByArtist.values());

        // Randomize artist group insertion order and song order within each group
        Collections.shuffle(artistGroups, ThreadLocalRandom.current());

        for (List<Song> artistSongs : artistGroups) {
            Collections.shuffle(artistSongs, ThreadLocalRandom.current());

            artistQueue.offer(new ArrayDeque<>(artistSongs));
        }

        List<Song> shuffledSongs = new ArrayList<>(songs.size());

        Deque<Song> previousArtist = null;

        // Greedy algorithm: select the artist with the most remaining songs
        while (!artistQueue.isEmpty()) {
            Deque<Song> currentArtist = artistQueue.poll();

            shuffledSongs.add(currentArtist.removeFirst());

            if (previousArtist != null && !previousArtist.isEmpty()) {
                artistQueue.offer(previousArtist);
            }

            previousArtist = currentArtist;
        }

        if (previousArtist != null) {
            shuffledSongs.addAll(previousArtist);
        }

        return shuffledSongs;
    }
}