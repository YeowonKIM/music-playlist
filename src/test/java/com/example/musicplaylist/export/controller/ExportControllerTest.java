package com.example.musicplaylist.export.controller;

import com.example.musicplaylist.export.dto.ExportResponse;
import com.example.musicplaylist.export.service.ExportService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.charset.StandardCharsets;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ExportController.class)
class ExportControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ExportService exportService;

    @Test
    void shouldReturnDownloadablePlaylistFile() throws Exception {
        String fileContent = """
                #EXTM3U
                #EXTINF:240,Beyoncé - Hello
                music/hello.mp3
                """;

        byte[] expectedContent = fileContent.getBytes(StandardCharsets.UTF_8);

        ExportResponse response = new ExportResponse(
                "playlist-1.m3u",
                "audio/x-mpegurl",
                fileContent
        );

        given(exportService.exportPlaylist(1L, "m3u")).willReturn(response);

        mockMvc.perform(get("/api/playlists/1/export").param("format", "m3u"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("audio/x-mpegurl"))
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"playlist-1.m3u\""))
                .andExpect(header().string(HttpHeaders.CONTENT_LENGTH, String.valueOf(expectedContent.length)))
                .andExpect(content().bytes(expectedContent));
    }
}