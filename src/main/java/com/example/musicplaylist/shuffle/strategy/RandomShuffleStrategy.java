package com.example.musicplaylist.shuffle.strategy;

import com.example.musicplaylist.song.domain.Song;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Component
public class RandomShuffleStrategy implements ShuffleStrategy {

    @Override
    public String getName() {
        return "random";
    }

    @Override
    public List<Song> shuffle(List<Song> songs) {
        List<Song> shuffledSongs = new ArrayList<>(songs);

        Collections.shuffle(shuffledSongs, ThreadLocalRandom.current());

        return shuffledSongs;
    }
}