package com.ssafy.backend.playlist.controller;

import com.ssafy.backend.common.ApiResponse;
import com.ssafy.backend.playlist.dto.GetPlaylistResponseDto;
import com.ssafy.backend.playlist.service.PlaylistService;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/playlist")
@RestController
@RequiredArgsConstructor
public class PlaylistController {

    private final PlaylistService playlistService;

    @GetMapping()
    public ApiResponse<?> getPlaylist(@RequestParam int playlistid) {
         return new ApiResponse.builder<Object>()
                 .payload(playlistService.getPlaylist(playlistid))
                 .build();
    }

}
