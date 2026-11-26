package com.example.musicplaylist.shuffle.strategy;

import com.example.musicplaylist.song.domain.Song;

import java.util.List;

public interface ShuffleStrategy {

    String getName();

    List<Song> shuffle(List<Song> songs);
}