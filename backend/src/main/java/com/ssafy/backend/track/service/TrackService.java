package com.ssafy.backend.track.service;

import com.ssafy.backend.auth.service.AuthService;
import com.ssafy.backend.common.service.S3Service;
import com.ssafy.backend.common.util.S3FileKeyExtractor;
import com.ssafy.backend.graph.model.entity.type.WeightType;
import com.ssafy.backend.graph.service.DataCollectingService;
import com.ssafy.backend.mysql.entity.*;
import com.ssafy.backend.mysql.repository.*;
import com.ssafy.backend.track.dto.request.TrackImageUploadRequestDto;
import com.ssafy.backend.track.dto.request.TrackUpdateRequestDto;
import com.ssafy.backend.track.dto.request.TrackUploadRequestDto;
import com.ssafy.backend.track.dto.response.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <pre>Track 서비스</pre>
 *
 * @author 박병주
 * @version 1.0
 * @since 2025-03-20
 */

@Slf4j
@Service
@RequiredArgsConstructor
public class TrackService {

    private final TrackRepository trackRepository;
    private final TrackTagRepository trackTagRepository;
    private final LayerRepository layerRepository;
    private final LayerFileRepository layerFileRepository;
    private final LikeRepository likeRepository;
    private final SamplingRepository samplingRepository;

    private final DataCollectingService dataCollectingService;
    private final S3Service s3Service;

    private final AuthService authService;

    /**
     * 트랙 음원 데이터를 반환
     *
     * @param trackId 반환할 음원의 트랙 id
     * @return byte[] 형식의 음원 데이터
     */
    public byte[] trackPlay(int trackId) {
        Track track = trackRepository.findById(trackId).orElseThrow(
                () -> {
                    log.warn("Track id {} not found", trackId);
                    return new RuntimeException("Track not found"); //TODO: 커스텀 예외 만들기
                }
        );
        return s3Service.downloadFile(S3FileKeyExtractor.extractS3FileKey(track.getSoundUrl()));
    }

    public byte[] layerPlay(int layerId) {
        Layer layer = layerRepository.findById(layerId).orElseThrow(
                () -> {
                    log.warn("Layer id {} not found", layerId);
                    return new RuntimeException("Layer not found");
                }
        );
        return s3Service.downloadFile(S3FileKeyExtractor.extractS3FileKey(layer.getLayerFile().getSoundUrl()));
    }

    /**
     * 트랙의 업로드
     *
     * @param trackUploadRequestDto 업로드 정보
     * @param memberId              업로드 멤버 id
     */
    @Transactional
    public int createTrack(TrackUploadRequestDto trackUploadRequestDto, int memberId) {
        // 트랜젝션
        Member member = new Member();
        member.setId(memberId);
        // 1. 트랙 생성
        Track track = Track.builder()
                .title(trackUploadRequestDto.getTitle())
                .description(trackUploadRequestDto.getDescription())
                .member(member)
                .blocked(false)
                .duration(trackUploadRequestDto.getDuration())
                .visibility(trackUploadRequestDto.isVisibility())
                .enabled(true)
                .importCount(0)
                .viewCount(0)
                // 1-1. 트랙 이미지 업로드(이미지 null 검사 처리)
                .imageUrl(trackUploadRequestDto.getTrackImg() != null ? s3Service.uploadFile(trackUploadRequestDto.getTrackImg(), S3Service.IMAGE) : null)
                // 1-2. 트랙 음성 업로드
                .soundUrl(s3Service.uploadFile(trackUploadRequestDto.getTrackSoundFile(), S3Service.MUSIC))
                .build();
        // 1-3. 트랙 insert
        Track t = trackRepository.save(track);
        // 1-3-1. 연결된 태그 insert
        List<TrackTag> trackTags = Arrays.stream(trackUploadRequestDto.getTags())
                .map(tagId -> {
                    Tag tag = new Tag();
                    tag.setId(tagId);
                    return TrackTag.builder()
                            .track(t)
                            .tag(tag)
                            .build();
                })
                .collect(Collectors.toList());
        trackTagRepository.saveAll(trackTags);
        // 1-3-2. 원천 트랙 insert
        List<Sampling> samplings = Arrays.stream(trackUploadRequestDto.getSourceTracks())
                .map(originTrackId -> Sampling.builder()
                        .originTrack(trackRepository.findById(originTrackId).get()) //TODO: 반복문 내에서 repo 조회 개선 필요.
                        .track(t).build()).toList();
        samplingRepository.saveAll(samplings);

        // 1-4. 그래프 추가
        // 1-4-1. Track 노드 생성 및 태그 연결
        dataCollectingService.createTrack(t.getId(), Arrays.stream(trackUploadRequestDto.getTags()).toList());
        // 1-4-2. TODO: FastAPI로 음원 보내기
        // 2. 레이어 목록 저장
        for (int i = 0; i < trackUploadRequestDto.getLayers().size(); i++) {
            // 2-1. 음성 업로드
            LayerFile layerFile = new LayerFile();
            layerFile.setSoundUrl(s3Service.uploadFile(trackUploadRequestDto.getLayerSoundFiles()[i], S3Service.MUSIC));
            LayerFile lf = layerFileRepository.save(layerFile);

            Layer layer = Layer.builder()
                    .name(trackUploadRequestDto.getLayers().get(i).getName())
                    .instrumentType(trackUploadRequestDto.getLayers().get(i).getInstrumentType())
                    .blocked(false)
                    .track(t)
                    .layerFile(lf).build();
            // 2-2. 레이어 정보 insert
            layerRepository.save(layer);

        }
        return t.getId();
    }

    @Transactional
    public TrackResponseDto viewTrack(int trackId, int memberId) {
        Track track = trackRepository.findById(trackId).orElseThrow(
                () -> {
                    log.warn("{}번 트랙은 존재하지 않음", trackId);
                    return new RuntimeException("Track not found"); // TODO: 커스텀 예외로 교체
                });
        // visibility - public 여부 true -> 본인만 볼 수 있음
        // block - 정지 여부 true -> 본인만 볼 수 있음
        // enabled - 소프트 삭제 여부 true -> 조회 불가
        if (track.getBlocked() || track.getVisibility()) {
            if (track.getMember().getId() != memberId) {
                log.info("{}번 트랙은 회원이 조회할 수 없는 데이터", memberId);
                throw new RuntimeException(); // TODO: 커스텀으로 교체
            }
        }

        if (track.getEnabled()) {
            log.info("{}번 회원이 삭제된 트랙({})을 조회", memberId, track.getId());
            throw new RuntimeException(); // TODO: 커스텀으로 교체
        }

        // 조회수 증가
        int viewCount = track.getViewCount();
        track.setViewCount(viewCount + 1);

        // 가져와야 하는 데이터
        // isLike 좋아요 누른 여부
        boolean isLike = likeRepository.findByTrackIdAndMemberId(track.getId(), memberId).isPresent();

        // 출처 트랙들
        List<TrackInfoDto> sourceTrackList = new ArrayList<>();
        samplingRepository.findAllByTrack(track).forEach(sampling -> {
            sourceTrackList.add(TrackInfoDto.builder()
                    .trackId(sampling.getOriginTrack().getId())
                    .title(sampling.getOriginTrack().getTitle())
                    .duration(sampling.getOriginTrack().getDuration())
                    .imageUrl(sampling.getOriginTrack().getImageUrl())
                    .build());
        });
        // 임포트해간 트랙들
        List<TrackInfoDto> importedTrackList = new ArrayList<>();
        samplingRepository.findAllByOriginTrack(track).forEach(sampling -> {
            importedTrackList.add(TrackInfoDto.builder()
                    .trackId(sampling.getTrack().getId())
                    .title(sampling.getTrack().getTitle())
                    .duration(sampling.getTrack().getDuration())
                    .imageUrl(sampling.getTrack().getImageUrl())
                    .build());
        });

        // 태그 정보
        List<TrackTag> tags = trackTagRepository.findAllByTrack(track);
        List<TagInfo> tagInfoList = new ArrayList<>();
        if (!tags.isEmpty()) {
            tags.forEach(tag -> {
                tagInfoList.add(TagInfo.builder()
                        .tagId(tag.getTag().getId())
                        .name(tag.getTag().getName()).build());
            });
        }
        return TrackResponseDto.builder()
                .trackId(track.getId())
                .title(track.getTitle())
                .description(track.getDescription())
                .imageUrl(track.getImageUrl())
                .artist(ArtistInfoDto.builder().memberId(track.getMember().getId()).nickname(track.getMember().getNickname()).profileImage(track.getMember().getProfileImage()).build())
                .isLiked(isLike)
                .importCount(track.getImportCount())
                .likeCount(track.getLikeCount())
                .viewCount(track.getViewCount())
                .sourceTracks(sourceTrackList)
                .importTracks(importedTrackList)
                .tags(tagInfoList)
                .build();
    }

    @Transactional
    public void updateTrack(TrackUpdateRequestDto trackUpdateRequestDto, int memberId) {
        Track track = trackRepository.findById(trackUpdateRequestDto.getTrackId()).orElseThrow(
                () -> {
                    log.warn("{} 트랙은 없는 트랙", trackUpdateRequestDto.getTrackId());
                    return new RuntimeException();
                }
        );
        if (track.getMember().getId() != memberId) {
            log.info("본인({})의 트랙({})이 아닐 때 조회 요청", memberId, track.getId());
            throw new RuntimeException("");
        }
        track.setTitle(trackUpdateRequestDto.getTitle());
        track.setDescription(trackUpdateRequestDto.getDescription());
        track.setVisibility(trackUpdateRequestDto.isVisibility());
        trackRepository.save(track);
    }

    @Transactional
    public String updateImage(TrackImageUploadRequestDto trackImageUploadRequestDto) {
        Track track = trackRepository.findById(trackImageUploadRequestDto.getTrackId()).orElseThrow();
        String existingFileUrl = track.getImageUrl();
        String updatedFileUrl;
        if (existingFileUrl == null) {
            updatedFileUrl = s3Service.uploadFile(trackImageUploadRequestDto.getTrackImg(), S3Service.MUSIC);
        } else {
            updatedFileUrl = s3Service.updateFile(existingFileUrl, trackImageUploadRequestDto.getTrackImg(), S3Service.MUSIC);
        }
        // 변경된 이미지 반영
        track.setImageUrl(updatedFileUrl);
        trackRepository.save(track);

        return updatedFileUrl;
    }

    @Transactional
    public void deleteTrack(int trackId) {
        Member member = authService.getMember();
        Track track = trackRepository.findById(trackId).orElseThrow(
                () -> {
                    log.warn("{} 트랙은 없는 트랙", trackId);
                    return new RuntimeException();
                }
        );
        if (track.getMember().getId() != member.getId()) {
            log.info("본인({})의 트랙({})이 아닐 때 삭제 요청", member.getId(), track.getId());
            throw new RuntimeException("");
        } else if (!track.getEnabled()) {
            log.info("트랙({})은 이미 삭제 처리 됨", track.getId());
            return;
        }
        // soft delete
        track.setEnabled(false);
        trackRepository.save(track);
    }

    public void recordPlay(int trackId) {
        dataCollectingService.viewTrack(authService.getMember().getId(), trackId, WeightType.VIEW);
    }

    public List<LayerResponseDto> getLayers(int trackId) {
        List<Layer> layers = layerRepository.findAllByTrackId(trackId);
        List<LayerResponseDto> layerResponseDtoList = new ArrayList<>();
        layers.forEach(layer -> {
            layerResponseDtoList.add(LayerResponseDto.builder()
                    .layerId(layer.getId())
                    .instrumentType(layer.getInstrumentType())
                    .name(layer.getName()).build());
        });
        return layerResponseDtoList;
    }

    @Transactional
    public void likeTrack(int trackId) {
        Member member = authService.getMember();
        Like l = likeRepository.findByTrackIdAndMemberId(trackId, member.getId()).orElse(null);
        if (l != null) return; // 중복된 요청 처리
        // 1. TrackLike, Track.LikeCount 반영
        Track track = trackRepository.findById(trackId).orElseThrow(
                () -> {
                    log.warn("트랙({})은 없는 트랙에 대한 요청", trackId);
                    return new RuntimeException();
                }
        );
        // 트랙에 Like 정보 반영
        int likeCount = track.getLikeCount();
        track.setLikeCount(++likeCount);
        trackRepository.save(track);

        // Like 관계 매핑
        Like like = new Like();
        like.setTrack(track);
        like.setMember(member);
        likeRepository.save(like);


        // 2. graph 반영
        dataCollectingService.viewTrack(authService.getMember().getId(), trackId, WeightType.LIKE);
    }

    @Transactional
    public void unlikeTrack(int trackId) {
        // 1. TrackLike, Track.LikeCount 반영
        Member member = authService.getMember();
        Track track = trackRepository.findById(trackId).orElseThrow(
                () -> {
                    log.warn("트랙({})은 없는 트랙에 대한 요청", trackId);
                    return new RuntimeException();
                }
        );
        // 트랙에 Like 정보 반영
        int likeCount = track.getLikeCount();
        track.setLikeCount(--likeCount);
        trackRepository.save(track);

        // Like 관계 매핑 삭제
        Like like = new Like();
        like.setTrack(track);
        like.setMember(member);
        likeRepository.delete(like);

        // 2. graph 반영
        dataCollectingService.viewTrack(authService.getMember().getId(), trackId, WeightType.DISLIKE);
    }
}
