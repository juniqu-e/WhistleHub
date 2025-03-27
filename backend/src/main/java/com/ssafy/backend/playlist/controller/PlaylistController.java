package com.ssafy.backend.playlist.controller;

import com.ssafy.backend.common.ApiResponse;
import com.ssafy.backend.common.error.exception.MissingParameterException;
import com.ssafy.backend.mysql.entity.Playlist;
import com.ssafy.backend.playlist.dto.*;
import com.ssafy.backend.playlist.service.PlaylistService;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.awt.print.Book;
import java.awt.print.Pageable;
import java.util.List;

@Slf4j
@RequestMapping("/api/playlist")
@RestController
@RequiredArgsConstructor
public class PlaylistController {

    private final PlaylistService playlistService;

    // 플레이리스트 설명 조회
    @GetMapping()
    public ApiResponse<?> getPlaylist(@RequestParam int playlistId) {
         return new ApiResponse.builder<Object>()
                 .payload(playlistService.getPlaylist(playlistId))
                 .build();
    }

    // 플레이리스트 만들기
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<?> createPlaylist(
            @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam(value = "image", required = false) MultipartFile image,
            @RequestParam(value = "trackIds", required = false) int[] trackIds) {

        log.debug("name: {}, description: {}, image: {}, trackIds: {}", name, description, image, trackIds);

        if(name == null || name.isEmpty()) {
            throw new MissingParameterException();
        }

        int payload = playlistService.createPlaylist(name, description, image, trackIds);

        return new ApiResponse.builder<Object>()
                .payload(payload)
                .build();
    }

    // 플레이리스트 수정
    @PutMapping()
    public ApiResponse<?> updatePlaylist(@RequestBody ModifyPlaylistRequestDto requestDto) {
        if(requestDto.getName() == null || requestDto.getName().isEmpty()
                || requestDto.getDescription() == null || requestDto.getDescription().isEmpty()) {
            throw new MissingParameterException();
        }

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
@PostMapping(value = "/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
public ApiResponse<?> uploadImage(UploadPlaylistImageRequestDto requestDto) {
    playlistService.uploadImage(requestDto);
    return new ApiResponse.builder<Object>()
            .payload(null)
            .build();
}

    // 플레이리스트에 트랙 추가
    @PostMapping("/track")
    public ApiResponse<?> addTrack(@RequestBody AddTrackRequestDto requestDto) {
        playlistService.addTrack(requestDto);
        return new ApiResponse.builder<Object>()
                .payload(null)
                .build();
    }

    @GetMapping("/member")
    public ApiResponse<?> getMemberPlaylist(
            @RequestParam int memberId,
            @RequestParam int page,
            @RequestParam int size,
            @RequestParam String orderby) {

        List<GetMemberPlaylistResponseDto> playlists;

        if(orderby.equals("ASC")) {
            playlists = playlistService.getMemberPlaylist(memberId, PageRequest.of(page, size, Sort.by(Sort.Order.asc("createdAt"))));
        } else if(orderby.equals("DESC")) {
            playlists = playlistService.getMemberPlaylist(memberId, PageRequest.of(page, size, Sort.by(Sort.Order.desc("createdAt"))));
        } else {
            throw new MissingParameterException();
        }

        return new ApiResponse.builder<Object>()
                .payload(playlists)
                .build();
    }
}
