package com.ssafy.backend.playlist.service;

import com.ssafy.backend.auth.service.AuthService;
import com.ssafy.backend.common.error.exception.DuplicateTrackException;
import com.ssafy.backend.common.error.exception.NotFoundException;
import com.ssafy.backend.common.error.exception.NotFoundPlaylistException;
import com.ssafy.backend.common.service.S3Service;
import com.ssafy.backend.mysql.entity.Playlist;
import com.ssafy.backend.mysql.entity.PlaylistTrack;
import com.ssafy.backend.mysql.entity.Track;
import com.ssafy.backend.mysql.repository.PlaylistRepository;
import com.ssafy.backend.mysql.repository.PlaylistTrackRepository;
import com.ssafy.backend.mysql.repository.TrackRepository;
import com.ssafy.backend.playlist.dto.*;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class PlaylistService {

    private final PlaylistRepository playlistRepository;
    private final PlaylistTrackRepository playlistTrackRepository;
    private final AuthService authService;
    private final TrackRepository trackRepository;
    private final S3Service s3Service;

    public GetPlaylistResponseDto getPlaylist(int playlistid) {
        Playlist playlist = playlistRepository.findById(playlistid).orElseThrow(
                () -> {
                    log.warn("{} 플레이리스트가 없습니다.", playlistid);
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
    public int createPlaylist(String name, String description, MultipartFile image, int[] trackIds) {
        Playlist playlist = new Playlist();
        playlist.setName(name);
        playlist.setDescription(description);

        //멤버 정보 추가
        playlist.setMember(authService.getMember());

        // 이미지 저장 -> S3
        String imageUrl = s3Service.uploadFile(image, S3Service.IMAGE);
        playlist.setImageUrl(imageUrl);

        int playlistId = playlistRepository.save(playlist).getId();

        // 트랙 저장 -> TrackRepository
        try {
            int order = 1;
            for (int trackId : trackIds) {
                PlaylistTrack playlistTrack = new PlaylistTrack();
                playlistTrack.setPlaylist(playlist);
                playlistTrack.setTrack(trackRepository.findById(trackId).orElseThrow());
                playlistTrack.setPlayOrder(order++);
                playlistTrackRepository.save(playlistTrack);
            }
        } catch (Exception e) {
            log.warn("플레이리스트 생성 중 오류 발생: {}", e.getMessage());
            //만들었던 트랙 삭제
            playlistRepository.deleteById(playlistId);
            throw new NotFoundPlaylistException();
        }
        return playlistId;
    }

    @Transactional
    public int updatePlaylist(ModifyPlaylistRequestDto requestDto) {
        Playlist playlist = playlistRepository.findById(requestDto.getPlaylistId()).orElseThrow(
                () -> {
                    log.warn("{} 플레이리스트가 없습니다.", requestDto.getPlaylistId());
                    return new NotFoundPlaylistException();
                }
        );
        playlist.setName(requestDto.getName());
        playlist.setDescription(requestDto.getDescription());
        return playlistRepository.save(playlist).getId();
    }


    @Transactional
    public void deletePlaylist(int playlistId) {
        Playlist playlist = playlistRepository.findById(playlistId).orElseThrow(
                () -> {
                    log.warn("{} 플레이리스트가 없습니다.", playlistId);
                    return new NotFoundPlaylistException();
                }
        );
        playlistTrackRepository.deleteAllByPlaylist(playlist);
        playlistRepository.delete(playlist);
    }

    public List<GetPlaylistTrackResponseDto> getPlaylistTrack(int playlistId) {
        Playlist playlist = playlistRepository.findById(playlistId).orElseThrow(
                () -> {
                    log.warn("{} 플레이리스트가 없습니다.", playlistId);
                    return new NotFoundPlaylistException();
                }
        );
        List<PlaylistTrack> playlistTracks = playlistTrackRepository.findAllByPlaylist(playlist);
        List<GetPlaylistTrackResponseDto> responseDtos = new ArrayList<>();

        for (PlaylistTrack playlistTrack : playlistTracks) {
            responseDtos.add(
                    GetPlaylistTrackResponseDto
                            .builder()
                            .playlistTrackId(playlistTrack.getId())
                            .playOrder(playlistTrack.getPlayOrder())
                            .trackInfo(TrackInfo
                                    .builder()
                                    .trackId(playlistTrack.getTrack().getId())
                                    .title(playlistTrack.getTrack().getTitle())
                                    .nickname(playlistTrack.getTrack().getMember().getNickname())
                                    .duration(playlistTrack.getTrack().getDuration())
                                    .imageUrl(playlistTrack.getTrack().getImageUrl())
                                    .build()
                            )
                            .build()
            );
        }
        return responseDtos;
    }

    @Transactional
    public void updatePlaylistTrack(ModifyPlaylistTrackRequestDto requestDto) {

        Playlist playlist = playlistRepository.findById(requestDto.getPlaylistId()).orElseThrow(
                () -> {
                    log.warn("{} 플레이리스트가 없습니다.", requestDto.getPlaylistId());
                    return new NotFoundPlaylistException();
                }
        );
        playlistTrackRepository.deleteAllByPlaylist(playlist);

        int order = 1;
        for (int trackId : requestDto.getTracks()) {
            PlaylistTrack playlistTrack = new PlaylistTrack();
            playlistTrack.setPlaylist(playlist);
            playlistTrack.setTrack(trackRepository.findById(trackId).orElseThrow());
            playlistTrack.setPlayOrder(order++);
            playlistTrackRepository.save(playlistTrack);
        }
    }

    public void uploadImage(UploadPlaylistImageRequestDto requestDto) {
        Playlist playlist = playlistRepository.findById(requestDto.getPlaylistId()).orElseThrow(
                () -> {
                    log.warn("{} 플레이리스트가 없습니다.", requestDto.getPlaylistId());
                    return new NotFoundPlaylistException();
                }
        );
        playlist.setImageUrl(requestDto.getImage());
        playlistRepository.save(playlist);
    }

    @Transactional
    public void addTrack(AddTrackRequestDto requestDto) {
        Playlist playlist = playlistRepository.findById(requestDto.getPlaylistId()).orElseThrow(
                () -> {
                    log.warn("{} 해당 플레이리스트가 없습니다.", requestDto.getPlaylistId());
                    return new NotFoundException();
                }
        );
        Track track = trackRepository.findById(requestDto.getTrackId()).orElseThrow(
                () -> {
                    log.warn("{} 해당 트랙이 없습니다.", requestDto.getTrackId());
                    return new NotFoundException();
                }
        );

        List<PlaylistTrack> playlistTracks = playlistTrackRepository.findAllByPlaylist(playlist);

        for(PlaylistTrack playlistTrack : playlistTracks) {
            if(playlistTrack.getTrack().getId().equals(track.getId())) {
                throw new DuplicateTrackException();
            }
        }
        
        playlistTrackRepository.save(PlaylistTrack.builder()
                .playlist(playlist)
                .track(track)
                .playOrder(playlistTracks.size() + 1)
                .build());
    }
}