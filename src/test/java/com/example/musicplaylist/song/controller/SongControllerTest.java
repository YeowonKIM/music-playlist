package com.example.musicplaylist.song.controller;

import com.example.musicplaylist.common.exception.BusinessException;
import com.example.musicplaylist.common.exception.ErrorCode;
import com.example.musicplaylist.song.dto.CreateSongRequest;
import com.example.musicplaylist.song.dto.SongResponse;
import com.example.musicplaylist.song.service.SongService;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SongController.class)
public class SongControllerTest {

    private static final String BASE_URL = "/api/songs";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SongService songService;

    @Nested
    class CreateSongTests {

        @Test
        void shouldCreateSong() throws Exception {
            CreateSongRequest request = new CreateSongRequest("Yellow", "Coldplay", "Rock", 240, "music/yellow.mp3");

            SongResponse response = createSongResponse(1L);

            when(songService.createSong(request))
                    .thenReturn(response);

            mockMvc.perform(post(BASE_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                        "title": "Yellow",
                                        "artist": "Coldplay",
                                        "genre": "Rock",
                                        "durationSeconds": 240,
                                        "uri": "music/yellow.mp3"
                                    }
                                    """))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.title").value("Yellow"))
                    .andExpect(jsonPath("$.artist").value("Coldplay"))
                    .andExpect(jsonPath("$.genre").value("Rock"))
                    .andExpect(jsonPath("$.durationSeconds").value(240))
                    .andExpect(jsonPath("$.uri").value("music/yellow.mp3"));

            verify(songService).createSong(request);
        }

        @Test
        void shouldRejectInvalidFields() throws Exception {
            mockMvc.perform(post(BASE_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                        "title": "",
                                        "artist": " ",
                                        "genre": "",
                                        "durationSeconds": 0,
                                        "uri": ""
                                    }
                                    """))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(400))
                    .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"))
                    .andExpect(jsonPath("$.message").value(containsString("title:")))
                    .andExpect(jsonPath("$.message").value(containsString("artist:")))
                    .andExpect(jsonPath("$.message").value(containsString("genre:")))
                    .andExpect(jsonPath("$.message").value(containsString("durationSeconds:")))
                    .andExpect(jsonPath("$.message").value(containsString("uri:")));

            verifyNoInteractions(songService);
        }

        @Test
        void shouldRejectMissingRequiredFields() throws Exception {
            mockMvc.perform(post(BASE_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {}
                                    """))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(400))
                    .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"))
                    .andExpect(jsonPath("$.message").value(containsString("title:")))
                    .andExpect(jsonPath("$.message").value(containsString("artist:")))
                    .andExpect(jsonPath("$.message").value(containsString("genre:")))
                    .andExpect(jsonPath("$.message").value(containsString("durationSeconds:")))
                    .andExpect(jsonPath("$.message").value(containsString("uri:")));

            verifyNoInteractions(songService);
        }
    }

    @Nested
    class GetSongTests {

        @Test
        void shouldGetAllSongs() throws Exception {
            SongResponse firstSong = createSongResponse(1L);
            SongResponse secondSong = new SongResponse(2L, "Hello", "Adele", "Pop", 295, "music/hello.mp3");

            when(songService.getAllSongs())
                    .thenReturn(List.of(firstSong, secondSong));

            mockMvc.perform(get(BASE_URL))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(2))
                    .andExpect(jsonPath("$[0].id").value(1))
                    .andExpect(jsonPath("$[0].title").value("Yellow"))
                    .andExpect(jsonPath("$[1].id").value(2))
                    .andExpect(jsonPath("$[1].title").value("Hello"));
        }

        @Test
        void shouldGetSongById() throws Exception {
            when(songService.getSong(1L))
                    .thenReturn(createSongResponse(1L));

            mockMvc.perform(get(BASE_URL + "/{songId}", 1L))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.title").value("Yellow"));
        }

        @Test
        void shouldReturnNotFoundWhenSongDoesNotExist() throws Exception {
            when(songService.getSong(999L))
                    .thenThrow(new BusinessException(ErrorCode.SONG_NOT_FOUND));

            mockMvc.perform(get(BASE_URL + "/{songId}", 999L))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.status").value(404))
                    .andExpect(jsonPath("$.code").value("SONG_NOT_FOUND"))
                    .andExpect(jsonPath("$.message").value("Song not found"));
        }
    }

    private SongResponse createSongResponse(Long id) {
        return new SongResponse(id, "Yellow", "Coldplay", "Rock", 240, "music/yellow.mp3");
    }
}