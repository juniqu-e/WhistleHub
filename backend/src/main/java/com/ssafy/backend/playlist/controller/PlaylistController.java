package com.ssafy.backend.playlist.controller;

import com.ssafy.backend.common.ApiResponse;
import com.ssafy.backend.playlist.dto.*;
import com.ssafy.backend.playlist.service.PlaylistService;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Slf4j
@RequestMapping("/playlist")
@RestController
@RequiredArgsConstructor
public class PlaylistController {

    private final PlaylistService playlistService;

    // 플레이리스트 설명 조회
    @GetMapping()
    public ApiResponse<?> getPlaylist(@RequestParam int playlistid) {
         return new ApiResponse.builder<Object>()
                 .payload(playlistService.getPlaylist(playlistid))
                 .build();
    }

    // 플레이리스트 만들기
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<?> createPlaylist(
            @RequestParam("name") String name,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam(value = "image", required = false) MultipartFile image,
            @RequestParam(value = "track_ids", required = false) List<Integer> trackIds) {

        log.warn("name: {}, description: {}, image: {}, trackIds: {}", name, description, image, trackIds);

        int payload = playlistService.createPlaylist(name, description, image, trackIds);

        return new ApiResponse.builder<Object>()
                .payload(payload)
                .build();
    }

    // 플레이리스트 수정
    @PutMapping()
    public ApiResponse<?> updatePlaylist(@RequestBody ModifyPlaylistRequestDto requestDto) {
        int payload = playlistService.updatePlaylist(requestDto);
        return new ApiResponse.builder<Object>()
                .payload(payload)
                .build();
    }

    // 플레이리스트 삭제
    @DeleteMapping()
    public ApiResponse<?> deletePlaylist(@RequestParam int playlistId) {
        playlistService.deletePlaylist(playlistId);
        return new ApiResponse.builder<Object>()
                .payload(null)
                .build();
    }

    // 플레이리스트 상세 조회
    @GetMapping("/track")
    public ApiResponse<?> getPlaylistTrack(@RequestParam int playlistId) {
        return new ApiResponse.builder<Object>()
                .payload(playlistService.getPlaylistTrack(playlistId))
                .build();
    }

    // 플레이리스트 트랙 수정
    @PutMapping("/track")
    public ApiResponse<?> updatePlaylistTrack(@RequestBody ModifyPlaylistTrackRequestDto requestDto) {
        playlistService.updatePlaylistTrack(requestDto);
        return new ApiResponse.builder<Object>()
                .payload(null)
                .build();
    }

    // 플레이리스트 사진 업로드
    @PostMapping("/image")
    public ApiResponse<?> uploadImage(@RequestBody UploadPlaylistImageRequestDto requestDto) {
        playlistService.uploadImage(requestDto);
        return new ApiResponse.builder<Object>()
                .payload(null)
                .build();
    }
}
