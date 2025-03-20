package com.ssafy.backend.playlist.service;

import com.ssafy.backend.common.error.exception.NotFoundPlaylistException;
import com.ssafy.backend.common.service.S3Service;
import com.ssafy.backend.mysql.entity.Playlist;
import com.ssafy.backend.mysql.entity.PlaylistTrack;
import com.ssafy.backend.mysql.repository.PlaylistRepository;
import com.ssafy.backend.mysql.repository.PlaylistTrackRepository;
import com.ssafy.backend.mysql.repository.TrackRepository;
import com.ssafy.backend.playlist.dto.GetPlaylistResponseDto;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class PlaylistService {

    private final PlaylistRepository playlistRepository;
    private final PlaylistTrackRepository playlistTrackRepository;
    private final TrackRepository trackRepository;
    private final S3Service s3Service;

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

    @Transactional
    public int createPlaylist(String name, String description, MultipartFile image, List<Integer> trackIds) {
        Playlist playlist = new Playlist();
        playlist.setName(name);
        playlist.setDescription(description);

        // 이미지 저장 -> S3
        String imageUrl = s3Service.uploadFile(image, S3Service.IMAGE);
        playlist.setImageUrl(image.getOriginalFilename());

        // 트랙 저장 -> TrackRepository
        int order = 1;
        for(int trackId : trackIds) {
            PlaylistTrack playlistTrack = new PlaylistTrack();
            playlistTrack.setPlaylist(playlist);
            playlistTrack.setTrack(trackRepository.findById(trackId).orElseThrow());
            playlistTrack.setPlayOrder(order++);
            playlistTrackRepository.save(playlistTrack);
        }

        return playlistRepository.save(playlist).getId();
    }
}
