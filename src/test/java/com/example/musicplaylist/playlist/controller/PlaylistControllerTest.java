package com.example.musicplaylist.playlist.controller;

import com.example.musicplaylist.common.exception.BusinessException;
import com.example.musicplaylist.common.exception.ErrorCode;
import com.example.musicplaylist.playlist.dto.*;
import com.example.musicplaylist.playlist.service.PlaylistService;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PlaylistController.class)
public class PlaylistControllerTest {

    private static final String BASE_URL = "/api/playlists";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PlaylistService playlistService;

    @Nested
    class CreatePlaylistTests {

        @Test
        void shouldCreatePlaylist() throws Exception {
            CreatePlaylistRequest request = new CreatePlaylistRequest("My Favorites", "My favorite songs");

            when(playlistService.createPlaylist(request))
                    .thenReturn(createPlaylistResponse());

            mockMvc.perform(post(BASE_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                        "name": "My Favorites",
                                        "description": "My favorite songs"
                                    }
                                    """))
                    .andExpect(status().isCreated())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.name").value("My Favorites"))
                    .andExpect(jsonPath("$.description").value("My favorite songs"));

            verify(playlistService).createPlaylist(request);
        }

        @Test
        void shouldRejectBlankName() throws Exception {
            mockMvc.perform(post(BASE_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                        "name": " ",
                                        "description": "My favorite songs"
                                    }
                                    """))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(400))
                    .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"))
                    .andExpect(jsonPath("$.message").value(containsString("name:")));

            verifyNoInteractions(playlistService);
        }
    }

    @Nested
    class GetPlaylistTests {

        @Test
        void shouldGetAllPlaylists() throws Exception {
            PlaylistResponse firstPlaylist = createPlaylistResponse();
            PlaylistResponse secondPlaylist = new PlaylistResponse(2L, "Workout", "Workout songs");

            when(playlistService.getAllPlaylists())
                    .thenReturn(List.of(firstPlaylist, secondPlaylist));

            mockMvc.perform(get(BASE_URL))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(2))
                    .andExpect(jsonPath("$[0].id").value(1))
                    .andExpect(jsonPath("$[0].name").value("My Favorites"))
                    .andExpect(jsonPath("$[1].id").value(2))
                    .andExpect(jsonPath("$[1].name").value("Workout"));
        }

        @Test
        void shouldGetPlaylist() throws Exception {
            when(playlistService.getPlaylist(1L))
                    .thenReturn(createPlaylistResponse());

            mockMvc.perform(get(BASE_URL + "/{playlistId}", 1L))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.name").value("My Favorites"));
        }

        @Test
        void shouldReturnNotFoundWhenPlaylistDoesNotExist() throws Exception {
            when(playlistService.getPlaylist(999L))
                    .thenThrow(new BusinessException(ErrorCode.PLAYLIST_NOT_FOUND));

            mockMvc.perform(get(BASE_URL + "/{playlistId}", 999L))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.status").value(404))
                    .andExpect(jsonPath("$.code").value("PLAYLIST_NOT_FOUND"))
                    .andExpect(jsonPath("$.message").value("Playlist not found"));
        }
    }

    @Nested
    class UpdatePlaylistTests {

        @Test
        void shouldUpdatePlaylist() throws Exception {
            UpdatePlaylistRequest request = new UpdatePlaylistRequest("Updated Favorites", "Updated description");

            PlaylistResponse response = new PlaylistResponse(1L, "Updated Favorites", "Updated description");

            when(playlistService.updatePlaylist(1L, request))
                    .thenReturn(response);

            mockMvc.perform(put(BASE_URL + "/{playlistId}", 1L)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                        "name": "Updated Favorites",
                                        "description": "Updated description"
                                    }
                                    """))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.name").value("Updated Favorites"))
                    .andExpect(jsonPath("$.description").value("Updated description"));

            verify(playlistService).updatePlaylist(1L, request);
        }
    }

    @Nested
    class DeletePlaylistTests {

        @Test
        void shouldDeletePlaylist() throws Exception {
            mockMvc.perform(delete(BASE_URL + "/{playlistId}", 1L))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("Playlist deleted successfully"));

            verify(playlistService).deletePlaylist(1L);
        }
    }

    @Nested
    class AddSongTests {

        @Test
        void shouldAddSongToPlaylist() throws Exception {
            mockMvc.perform(post(BASE_URL + "/1/tracks")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                        "songId": 2
                                    }
                                    """))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.message").value("Song added to playlist successfully"));

            verify(playlistService).addSongToPlaylist(1L, new AddSongRequest(2L));
        }

        @Test
        void shouldReturnConflictWhenSongAlreadyExists() throws Exception {
            doThrow(new BusinessException(ErrorCode.DUPLICATE_SONG))
                    .when(playlistService)
                    .addSongToPlaylist(1L, new AddSongRequest(2L));

            mockMvc.perform(post(BASE_URL + "/1/tracks")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                        "songId": 2
                                    }
                                    """))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.status").value(409))
                    .andExpect(jsonPath("$.code").value("DUPLICATE_SONG"))
                    .andExpect(jsonPath("$.message").value(ErrorCode.DUPLICATE_SONG.getMessage()));
        }
    }

    @Nested
    class RemoveSongTests {

        @Test
        void shouldRemoveSongFromPlaylist() throws Exception {
            mockMvc.perform(delete(BASE_URL + "/1/tracks/2"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message")
                            .value("Song removed from playlist successfully"));

            verify(playlistService).removeSongFromPlaylist(1L, 2L);
        }
    }

    @Nested
    class MoveSongTests {

        @Test
        void shouldMoveSongInPlaylist() throws Exception {
            mockMvc.perform(patch(BASE_URL + "/1/tracks/2/position")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                        "newPosition": 0
                                    }
                                    """))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("Song position updated successfully"));

            verify(playlistService).moveSongInPlaylist(
                    1L,
                    2L,
                    new MoveSongRequest(0)
            );
        }

        @Test
        void shouldRejectMissingNewPosition() throws Exception {
            mockMvc.perform(patch(BASE_URL + "/1/tracks/2/position")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(400))
                    .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"))
                    .andExpect(jsonPath("$.message").value(containsString("newPosition:")));

            verifyNoInteractions(playlistService);
        }
    }

    private PlaylistResponse createPlaylistResponse() {
        return new PlaylistResponse(1L, "My Favorites", "My favorite songs");
    }
}