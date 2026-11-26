package com.example.musicplaylist.song.service;

import com.example.musicplaylist.common.exception.BusinessException;
import com.example.musicplaylist.common.exception.ErrorCode;
import com.example.musicplaylist.song.domain.Song;
import com.example.musicplaylist.song.dto.CreateSongRequest;
import com.example.musicplaylist.song.dto.SongResponse;
import com.example.musicplaylist.song.repository.SongRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SongService {

    private final SongRepository songRepository;

    @Transactional
    public SongResponse createSong(CreateSongRequest request) {
        Song song = Song.builder()
                .title(request.title())
                .artist(request.artist())
                .genre(request.genre())
                .durationSeconds(request.durationSeconds())
                .uri(request.uri())
                .build();

        Song savedSong = songRepository.save(song);

        return SongResponse.from(savedSong);
    }

    public List<SongResponse> getAllSongs() {
        return songRepository.findAll()
                .stream()
                .map(SongResponse::from)
                .toList();
    }

    public SongResponse getSong(Long songId) {
        Song song = findSongById(songId);

        return SongResponse.from(song);
    }

    private Song findSongById(Long songId) {
        return songRepository.findById(songId)
                .orElseThrow(() ->
                        new BusinessException(ErrorCode.SONG_NOT_FOUND)
                );
    }
}