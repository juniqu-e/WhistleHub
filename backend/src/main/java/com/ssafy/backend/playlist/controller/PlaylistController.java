package com.ssafy.backend.playlist.controller;

import com.ssafy.backend.common.ApiResponse;
import com.ssafy.backend.playlist.dto.CreatePlaylistRequestDto;
import com.ssafy.backend.playlist.dto.GetPlaylistResponseDto;
import com.ssafy.backend.playlist.service.PlaylistService;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

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

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<?> createPlaylist(
            @RequestParam("name") String name,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam(value = "image", required = false) MultipartFile image,
            @RequestParam(value = "trackIds", required = false) List<Integer> trackIds) {

        // 서비스 호출
        int payload = playlistService.createPlaylist(name, description, image, trackIds);

        // 응답
        return new ApiResponse.builder<Object>()
                .payload(payload)
                .build();
    }

}
