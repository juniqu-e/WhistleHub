package com.ssafy.backend.playlist.service;

import com.ssafy.backend.common.error.exception.NotFoundPlaylistException;
import com.ssafy.backend.mysql.entity.Playlist;
import com.ssafy.backend.mysql.repository.PlaylistRepository;
import com.ssafy.backend.playlist.dto.GetPlaylistResponseDto;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class PlaylistService {

    private final PlaylistRepository playlistRepository;

    public GetPlaylistResponseDto getPlaylist(int playlistid) {
        Playlist playlist = playlistRepository.findById(playlistid).orElseThrow(
            () -> {
                log.error("{} playlist not found", playlistid);
                return new NotFoundPlaylistException();
            }
        );

        return GetPlaylistResponseDto
                .builder()
                .memberId(playlist.getMember().getId())
                .name(playlist.getName())
                .description(playlist.getDescription())
                .imageUrl(playlist.getImageUrl())
                .build();
    }
}
