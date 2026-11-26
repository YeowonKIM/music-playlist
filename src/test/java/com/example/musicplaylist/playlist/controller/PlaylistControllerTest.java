package com.example.musicplaylist.playlist.controller;

import com.example.musicplaylist.common.exception.BusinessException;
import com.example.musicplaylist.common.exception.ErrorCode;
import com.example.musicplaylist.playlist.dto.*;
import com.example.musicplaylist.playlist.service.PlaylistService;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
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
        void shouldCreatePlaylistWithoutDescription() throws Exception {
            CreatePlaylistRequest request = new CreatePlaylistRequest("My Favorites", null);

            PlaylistResponse response = new PlaylistResponse(1L, "My Favorites", null);

            when(playlistService.createPlaylist(request))
                    .thenReturn(response);

            mockMvc.perform(post(BASE_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                        "name": "My Favorites"
                                    }
                                    """))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.name").value("My Favorites"))
                    .andExpect(jsonPath("$.description").isEmpty());

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

        @Test
        void shouldRejectMissingName() throws Exception {
            mockMvc.perform(post(BASE_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                        "description": "My favorite songs"
                                    }
                                    """))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(400))
                    .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"))
                    .andExpect(jsonPath("$.message").value(containsString("name:")));

            verifyNoInteractions(playlistService);
        }

        @Test
        void shouldRejectOversizedName() throws Exception {
            String requestBody = """
                    {
                        "name": "%s"
                    }
                    """.formatted("a".repeat(256));

            mockMvc.perform(post(BASE_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(400))
                    .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"))
                    .andExpect(jsonPath("$.message").value(containsString("name:")));

            verifyNoInteractions(playlistService);
        }

        @Test
        void shouldRejectOversizedDescription() throws Exception {
            String requestBody = """
                    {
                        "name": "My Favorites",
                        "description": "%s"
                    }
                    """.formatted("a".repeat(1001));

            mockMvc.perform(post(BASE_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(400))
                    .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"))
                    .andExpect(jsonPath("$.message").value(containsString("description:")));

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
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.length()").value(2))
                    .andExpect(jsonPath("$[0].id").value(1))
                    .andExpect(jsonPath("$[0].name").value("My Favorites"))
                    .andExpect(jsonPath("$[0].description").value("My favorite songs"))
                    .andExpect(jsonPath("$[1].id").value(2))
                    .andExpect(jsonPath("$[1].name").value("Workout"))
                    .andExpect(jsonPath("$[1].description").value("Workout songs"));

            verify(playlistService).getAllPlaylists();
        }

        @Test
        void shouldReturnEmptyListWhenNoPlaylistsExist() throws Exception {
            when(playlistService.getAllPlaylists())
                    .thenReturn(List.of());

            mockMvc.perform(get(BASE_URL))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$.length()").value(0));

            verify(playlistService).getAllPlaylists();
        }

        @Test
        void shouldGetPlaylist() throws Exception {
            when(playlistService.getPlaylist(1L))
                    .thenReturn(createPlaylistResponse());

            mockMvc.perform(get(BASE_URL + "/{playlistId}", 1L))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.name").value("My Favorites"))
                    .andExpect(jsonPath("$.description").value("My favorite songs"));

            verify(playlistService).getPlaylist(1L);
        }

        @Test
        void shouldReturnNotFoundWhenPlaylistDoesNotExist() throws Exception {
            when(playlistService.getPlaylist(999L))
                    .thenThrow(new BusinessException(
                            ErrorCode.PLAYLIST_NOT_FOUND));

            mockMvc.perform(get(BASE_URL + "/{playlistId}", 999L))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.status").value(404))
                    .andExpect(jsonPath("$.code").value("PLAYLIST_NOT_FOUND"))
                    .andExpect(jsonPath("$.message").value("Playlist not found"));

            verify(playlistService).getPlaylist(999L);
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

        @Test
        void shouldAllowRemovingPlaylistDescription() throws Exception {
            UpdatePlaylistRequest request = new UpdatePlaylistRequest("My Favorites", null);

            PlaylistResponse response = new PlaylistResponse(1L, "My Favorites", null);

            when(playlistService.updatePlaylist(1L, request))
                    .thenReturn(response);

            mockMvc.perform(put(BASE_URL + "/{playlistId}", 1L)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                        "name": "My Favorites",
                                        "description": null
                                    }
                                    """))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.name").value("My Favorites"))
                    .andExpect(jsonPath("$.description").isEmpty());

            verify(playlistService).updatePlaylist(1L, request);
        }

        @Test
        void shouldRejectBlankNameWhenUpdatingPlaylist() throws Exception {
            mockMvc.perform(put(BASE_URL + "/{playlistId}", 1L)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                        "name": " "
                                    }
                                    """))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(400))
                    .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"))
                    .andExpect(jsonPath("$.message").value(containsString("name:")));

            verifyNoInteractions(playlistService);
        }

        @Test
        void shouldReturnNotFoundWhenUpdatingNonexistentPlaylist() throws Exception {
            UpdatePlaylistRequest request = new UpdatePlaylistRequest("Updated Favorites", "Updated description");

            when(playlistService.updatePlaylist(999L, request))
                    .thenThrow(new BusinessException(
                            ErrorCode.PLAYLIST_NOT_FOUND));

            mockMvc.perform(put(BASE_URL + "/{playlistId}", 999L)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                        "name": "Updated Favorites",
                                        "description": "Updated description"
                                    }
                                    """))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.status").value(404))
                    .andExpect(jsonPath("$.code").value("PLAYLIST_NOT_FOUND"))
                    .andExpect(jsonPath("$.message").value("Playlist not found"));

            verify(playlistService).updatePlaylist(999L, request);
        }
    }

    @Nested
    class DeletePlaylistTests {

        @Test
        void shouldDeletePlaylist() throws Exception {
            mockMvc.perform(delete(BASE_URL + "/{playlistId}", 1L))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message")
                            .value("Playlist deleted successfully"));

            verify(playlistService).deletePlaylist(1L);
        }

        @Test
        void shouldReturnNotFoundWhenDeletingNonexistentPlaylist() throws Exception {
            doThrow(new BusinessException(ErrorCode.PLAYLIST_NOT_FOUND))
                    .when(playlistService).deletePlaylist(999L);

            mockMvc.perform(delete(BASE_URL + "/{playlistId}", 999L))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.status").value(404))
                    .andExpect(jsonPath("$.code").value("PLAYLIST_NOT_FOUND"))
                    .andExpect(jsonPath("$.message").value("Playlist not found"));

            verify(playlistService).deletePlaylist(999L);
        }
    }

    @Nested
    class ExceptionHandlingTests {

        @Test
        void shouldRejectMalformedJson() throws Exception {
            mockMvc.perform(post(BASE_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"name\":"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(400))
                    .andExpect(jsonPath("$.code").value("INVALID_REQUEST"))
                    .andExpect(jsonPath("$.message").value("Invalid request"));

            verifyNoInteractions(playlistService);
        }

        @Test
        void shouldRejectInvalidPlaylistId() throws Exception {
            mockMvc.perform(get(BASE_URL + "/{playlistId}", "invalid"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(400))
                    .andExpect(jsonPath("$.code").value("INVALID_REQUEST"))
                    .andExpect(jsonPath("$.message").value("Invalid request"));

            verifyNoInteractions(playlistService);
        }

        @Test
        void shouldRejectUnsupportedHttpMethod() throws Exception {
            mockMvc.perform(patch(BASE_URL + "/{playlistId}", 1L))
                    .andExpect(status().isMethodNotAllowed())
                    .andExpect(header().exists("Allow"))
                    .andExpect(jsonPath("$.status").value(405))
                    .andExpect(jsonPath("$.code").value("INVALID_REQUEST"))
                    .andExpect(jsonPath("$.message").value("Invalid request"));

            verifyNoInteractions(playlistService);
        }

        @Test
        void shouldRejectUnsupportedMediaType() throws Exception {
            mockMvc.perform(post(BASE_URL)
                            .contentType(MediaType.TEXT_PLAIN)
                            .content("My Favorites"))
                    .andExpect(status().isUnsupportedMediaType())
                    .andExpect(jsonPath("$.status").value(415))
                    .andExpect(jsonPath("$.code").value("INVALID_REQUEST"))
                    .andExpect(jsonPath("$.message").value("Invalid request"));

            verifyNoInteractions(playlistService);
        }

        @Test
        void shouldHandleUnexpectedException() throws Exception {
            when(playlistService.getAllPlaylists())
                    .thenThrow(new RuntimeException("Database connection failed"));

            mockMvc.perform(get(BASE_URL))
                    .andExpect(status().isInternalServerError())
                    .andExpect(jsonPath("$.status").value(500))
                    .andExpect(jsonPath("$.code").value("INTERNAL_SERVER_ERROR"))
                    .andExpect(jsonPath("$.message").value("An unexpected error occurred"));

            verify(playlistService).getAllPlaylists();
        }
    }

    // Song addition & removal tests
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
        void shouldReturnNotFoundWhenPlaylistDoesNotExist() throws Exception {
            doThrow(new BusinessException(ErrorCode.PLAYLIST_NOT_FOUND))
                    .when(playlistService)
                    .addSongToPlaylist(999L, new AddSongRequest(2L));

            mockMvc.perform(post(BASE_URL + "/999/tracks")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                {
                                    "songId": 2
                                }
                                """))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.status").value(404))
                    .andExpect(jsonPath("$.code").value("PLAYLIST_NOT_FOUND"))
                    .andExpect(jsonPath("$.message")
                            .value(ErrorCode.PLAYLIST_NOT_FOUND.getMessage()));
        }

        @Test
        void shouldReturnNotFoundWhenSongDoesNotExist() throws Exception {
            doThrow(new BusinessException(ErrorCode.SONG_NOT_FOUND))
                    .when(playlistService)
                    .addSongToPlaylist(1L, new AddSongRequest(999L));

            mockMvc.perform(post(BASE_URL + "/1/tracks")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                {
                                    "songId": 999
                                }
                                """))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.status").value(404))
                    .andExpect(jsonPath("$.code").value("SONG_NOT_FOUND"))
                    .andExpect(jsonPath("$.message")
                            .value(ErrorCode.SONG_NOT_FOUND.getMessage()));
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
                    .andExpect(jsonPath("$.message")
                            .value(ErrorCode.DUPLICATE_SONG.getMessage()));
        }

        @Test
        void shouldRejectMissingSongId() throws Exception {
            mockMvc.perform(post(BASE_URL + "/1/tracks")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(400))
                    .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"))
                    .andExpect(jsonPath("$.message")
                            .value(containsString("songId:")));

            verifyNoInteractions(playlistService);
        }

        @ParameterizedTest
        @ValueSource(longs = {0L, -1L})
        void shouldRejectNonPositiveSongId(long songId) throws Exception {
            mockMvc.perform(post(BASE_URL + "/1/tracks")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                {
                                    "songId": %d
                                }
                                """.formatted(songId)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(400))
                    .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"))
                    .andExpect(jsonPath("$.message")
                            .value(containsString("songId:")));

            verifyNoInteractions(playlistService);
        }
    }

    @Nested
    class RemoveSongTests {

        @Test
        void shouldRemoveSongFromPlaylist() throws Exception {
            mockMvc.perform(delete(BASE_URL + "/1/tracks/2"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("Song removed from playlist successfully"));

            verify(playlistService).removeSongFromPlaylist(1L, 2L);
        }

        @Test
        void shouldReturnNotFoundWhenPlaylistDoesNotExist() throws Exception {
            doThrow(new BusinessException(ErrorCode.PLAYLIST_NOT_FOUND))
                    .when(playlistService)
                    .removeSongFromPlaylist(999L, 2L);

            mockMvc.perform(delete(BASE_URL + "/999/tracks/2"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.status").value(404))
                    .andExpect(jsonPath("$.code").value("PLAYLIST_NOT_FOUND"))
                    .andExpect(jsonPath("$.message").value(ErrorCode.PLAYLIST_NOT_FOUND.getMessage()));
        }

        @Test
        void shouldReturnNotFoundWhenSongIsNotInPlaylist() throws Exception {
            doThrow(new BusinessException(ErrorCode.SONG_NOT_IN_PLAYLIST))
                    .when(playlistService)
                    .removeSongFromPlaylist(1L, 999L);

            mockMvc.perform(delete(BASE_URL + "/1/tracks/999"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.status").value(404))
                    .andExpect(jsonPath("$.code").value("SONG_NOT_IN_PLAYLIST"))
                    .andExpect(jsonPath("$.message")
                            .value(ErrorCode.SONG_NOT_IN_PLAYLIST.getMessage()));
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
                    .andExpect(jsonPath("$.message")
                            .value("Song position updated successfully"));

            verify(playlistService).moveSongInPlaylist(
                    1L,
                    2L,
                    new MoveSongRequest(0)
            );
        }

        @ParameterizedTest
        @ValueSource(strings = {
                "{}",
                "{\"newPosition\": null}",
                "{\"newPosition\": -1}"
        })
        void shouldRejectInvalidMoveSongRequest(String requestBody)
                throws Exception {
            mockMvc.perform(patch(BASE_URL + "/1/tracks/2/position")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code")
                            .value("VALIDATION_FAILED"));

            verifyNoInteractions(playlistService);
        }

        @Test
        void shouldReturnBadRequestWhenPositionIsOutOfBounds()
                throws Exception {
            doThrow(new BusinessException(ErrorCode.INVALID_POSITION))
                    .when(playlistService)
                    .moveSongInPlaylist(1L, 2L, new MoveSongRequest(10));

            mockMvc.perform(patch(BASE_URL + "/1/tracks/2/position")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                {
                                    "newPosition": 10
                                }
                                """))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code")
                            .value("INVALID_POSITION"));
        }
    }

    private PlaylistResponse createPlaylistResponse() {
        return new PlaylistResponse(1L, "My Favorites", "My favorite songs");
    }
}