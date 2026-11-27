package com.example.musicplaylist.workflow.controller;

import com.example.musicplaylist.workflow.dto.CreateAndPlayRequest;
import com.example.musicplaylist.workflow.dto.CreateAndPlayResponse;
import com.example.musicplaylist.workflow.service.PlaylistWorkflowService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/workflows")
@RequiredArgsConstructor
public class PlaylistWorkflowController {

    private final PlaylistWorkflowService playlistWorkflowService;

    @PostMapping("/create-and-play")
    @ResponseStatus(HttpStatus.CREATED)
    public CreateAndPlayResponse createAndPlay(@Valid @RequestBody CreateAndPlayRequest request) {
        return playlistWorkflowService.createAndPlay(request);
    }
}