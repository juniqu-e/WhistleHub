package com.ssafy.backend.track.service;

import com.ssafy.backend.auth.service.AuthService;
import com.ssafy.backend.common.error.exception.NotFoundLayerException;
import com.ssafy.backend.common.error.exception.NotFoundMemberException;
import com.ssafy.backend.common.error.exception.TrackNotFoundException;
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
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
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
    private final LikeRepository likeRepository;
    private final SamplingRepository samplingRepository;
    private final ReportRepository reportRepository;
    private final MemberRepository memberRepository;
    private final ListenRecoredRepository listenRecoredRepository;

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
                    return new TrackNotFoundException("Track not found");
                }
        );
        return s3Service.downloadFile(S3FileKeyExtractor.extractS3FileKey(track.getSoundUrl()));
    }

    public byte[] layerPlay(int layerId) {
        Layer layer = layerRepository.findById(layerId).orElseThrow(
                () -> {
                    log.warn("Layer id {} not found", layerId);
                    return new NotFoundLayerException();
                }
        );
        return s3Service.downloadFile(S3FileKeyExtractor.extractS3FileKey(layer.getLayerFile().getSoundUrl()));
    }


    /**
     * 트랙 조회
     * @param trackId 조회할 트랙 ID
     * @return 응답 트랙 DTO (TrackResponseDto)
     */
    @Transactional
    public TrackResponseDto viewTrack(int trackId) {
        int memberId = authService.getMember().getId();
        Track track = trackRepository.findById(trackId).orElseThrow(
                () -> {
                    log.warn("{}번 트랙은 존재하지 않음", trackId);
                    return new TrackNotFoundException("Track not found");
                });
        // visibility - public 여부 true -> 본인만 볼 수 있음
        if (!track.getVisibility()) {
            if (track.getMember().getId() != memberId) {
                log.info("{}번 트랙은 회원이 조회할 수 없는 데이터", memberId);
                throw new TrackNotFoundException("");
            }
        }
        Member member = memberRepository.findById(memberId).orElseThrow(
                () -> {
                    log.warn("{}번 회원은 존재하지 않음", memberId);
                    return new NotFoundMemberException();
                }
        );
        // 조회수 증가
        int viewCount = track.getViewCount();
        track.setViewCount(viewCount + 1);
        ListenRecord listenRecord = ListenRecord.builder()
                .member(member)
                .track(track)
                .build();

        listenRecoredRepository.save(listenRecord);

        // 가져와야 하는 데이터
        // isLike 좋아요 누른 여부
        boolean isLike = likeRepository.findByTrackIdAndMemberId(track.getId(), memberId).isPresent();

        // 출처 트랙들
        List<TrackInfo> sourceTrackList = new ArrayList<>();
        samplingRepository.findAllByTrack(track).forEach(sampling -> {
            sourceTrackList.add(TrackInfo.builder()
                    .trackId(sampling.getOriginTrack().getId())
                    .title(sampling.getOriginTrack().getTitle())
                    .duration(sampling.getOriginTrack().getDuration())
                    .imageUrl(sampling.getOriginTrack().getImageUrl())
                    .build());
        });
        // 임포트해간 트랙들
        List<TrackInfo> importedTrackList = new ArrayList<>();
        samplingRepository.findAllByOriginTrack(track).forEach(sampling -> {
            importedTrackList.add(TrackInfo.builder()
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
                .duration(track.getDuration())
                .description(track.getDescription())
                .imageUrl(track.getImageUrl())
                .artist(ArtistInfoDto.builder().memberId(track.getMember().getId()).nickname(track.getMember().getNickname()).profileImage(track.getMember().getProfileImage()).build())
                .isLiked(isLike)
                .createdAt(track.getCreatedAt())
                .importCount(track.getImportCount())
                .likeCount(track.getLikeCount())
                .viewCount(track.getViewCount())
                .sourceTracks(sourceTrackList)
                .importTracks(importedTrackList)
                .tags(tagInfoList)
                .build();
    }

    /**
     * 트랙 정보 업데이트
     * @param trackUpdateRequestDto 트랙 업데이트 요청 DTO
     */
    @Transactional
    public void updateTrack(TrackUpdateRequestDto trackUpdateRequestDto) {
        int memberId = authService.getMember().getId();
        Track track = trackRepository.findById(trackUpdateRequestDto.getTrackId()).orElseThrow(
                () -> {
                    log.warn("{} 트랙은 없는 트랙", trackUpdateRequestDto.getTrackId());
                    return new TrackNotFoundException("");
                }
        );
        if (track.getMember().getId() != memberId) {
            log.info("본인({})의 트랙({})이 아닐 때 조회 요청", memberId, track.getId());
            throw new TrackNotFoundException("");
        }
        track.setTitle(trackUpdateRequestDto.getTitle());
        track.setDescription(trackUpdateRequestDto.getDescription());
        track.setVisibility(trackUpdateRequestDto.isVisibility());
        trackRepository.save(track);
    }

    /**
     * S3 이미지 업데이트
     * @param trackImageUploadRequestDto 이미지 업데이트 요청 DTO
     * @return 업로드된 이미지 URL
     */
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

    /**
     * 트랙 삭제(소프트 삭제)
     *
     * @param trackId 삭제할 트랙 ID
     */
    @Transactional
    public void deleteTrack(int trackId) {
        Member member = authService.getMember();
        Track track = trackRepository.findById(trackId).orElseThrow(
                () -> {
                    log.warn("{} 트랙은 없는 트랙", trackId);
                    return new TrackNotFoundException("");
                }
        );
        if (track.getMember().getId() != member.getId()) {
            log.info("본인({})의 트랙({})이 아닐 때 삭제 요청", member.getId(), track.getId());
            throw new TrackNotFoundException("");
        } else if (!track.getEnabled()) {
            log.info("트랙({})은 이미 삭제 처리 됨", track.getId());
            return;
        }
        // soft delete
        track.setEnabled(false);
        trackRepository.save(track);
    }

    /**
     * 15초 이상 들었을 시 그래프 반영
     * (Member)-[VIEW]->(Track)
     * @param trackId 반영할 트랙
     */

    public void recordPlay(int trackId) {
        dataCollectingService.viewTrack(authService.getMember().getId(), trackId, WeightType.VIEW);
    }

    /**
     * 레이어 조회
     * @param trackId 조회할 레이어의 트랙 ID
     * @return 조회된 레이어 (LayerResponseDto 리스트) 반환
     */
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

    /**
     * 트랙 좋아요
     * @param trackId 좋아요 트랙 ID
     */
    @Transactional
    public void likeTrack(int trackId) {
        Member member = authService.getMember();
        Like l = likeRepository.findByTrackIdAndMemberId(trackId, member.getId()).orElse(null);
        if (l != null) return; // 중복된 요청 처리
        // 1. TrackLike, Track.LikeCount 반영
        Track track = trackRepository.findById(trackId).orElseThrow(
                () -> {
                    log.warn("트랙({})은 없는 트랙에 대한 요청", trackId);
                    return new TrackNotFoundException("");
                }
        );
        // 트랙에 Like 정보 반영
        int likeCount = track.getLikeCount();
        track.setLikeCount(++likeCount);
        trackRepository.save(track);

        // Like 관계 매핑
        Like like = new Like();
        like.setId(LikeId.builder().trackId(track.getId()).memberId(member.getId()).build());
        like.setTrack(track);
        like.setMember(member);
        likeRepository.save(like);


        // 2. graph 반영
        dataCollectingService.viewTrack(authService.getMember().getId(), trackId, WeightType.LIKE);
    }

    /**
     * 트랙 좋아요 취소
     * @param trackId 트랙 좋아요 취소할 트랙 ID
     */
    @Transactional
    public void unlikeTrack(int trackId) {
        // 1. TrackLike, Track.LikeCount 반영
        Member member = authService.getMember();
        Track track = trackRepository.findById(trackId).orElseThrow(
                () -> {
                    log.warn("트랙({})은 없는 트랙에 대한 요청", trackId);
                    return new TrackNotFoundException("");
                }
        );
        // 트랙에 Like 정보 반영
        int likeCount = track.getLikeCount();
        track.setLikeCount(--likeCount);
        trackRepository.save(track);

        // Like 관계 매핑 삭제
        Like like = likeRepository.findByTrackIdAndMemberId(trackId, member.getId()).orElseThrow(
                () -> {
                    log.warn("없는 관계 조회");
                    return new TrackNotFoundException("");
                }
        );
        likeRepository.delete(like);

        // 2. graph 반영
        dataCollectingService.viewTrack(authService.getMember().getId(), trackId, WeightType.DISLIKE);
    }

    /**
     * 트랙 신고
     * @param trackId 신고 대상 트랙 ID
     * @param type 신고 타입 번호(타입 미정)
     * @param detail 신고 상세 내용
     */
    public void reportTrack(int trackId, int type, String detail) {
        Member member = authService.getMember();
        Track track = trackRepository.findById(trackId).orElseThrow(
                () -> {
                    log.warn("트랙({})은 없는 트랙에 대한 요청", trackId);
                    return new TrackNotFoundException("");
                }
        );

        Report report = Report.builder()
                .reportType(type)
                .track(track)
                .member(member)
                .detail(detail)
                .build();
        reportRepository.save(report);
    }

    /**
     * 트랙 검색
     * @param keyword 검색 키워드 (null일 경우 적용 x)
     * @param page 페이지네이션 - 페이지 0 부터
     * @param size 페이지네이션 - 사이즈
     * @param orderBy 정렬 조건 - DESC, ASC 대소문자 구분 x
     * @return 조회된 트랙 정보(TrackSearchResponseDto 리스트) 반환
     */
    @Transactional
    public List<TrackSearchResponseDto> searchTrack(String keyword, int page, int size, String orderBy) {
        if (keyword == null) {
            keyword = "";
        }
        List<Track> tracks = null;
        if (orderBy.equalsIgnoreCase("ASC")) {
            tracks = trackRepository.findAllByTitleContains(keyword, PageRequest.of(page, size, Sort.by(Sort.Order.asc("createdAt"))));
        } else if (orderBy.equalsIgnoreCase("DESC")) {
            tracks = trackRepository.findAllByTitleContains(keyword, PageRequest.of(page, size, Sort.by(Sort.Order.desc("createdAt"))));
        }
        log.info(tracks.toString());
//        if(tracks == null) tracks = new ArrayList<>();
        List<TrackSearchResponseDto> result = new ArrayList<>();
        for (Track track : tracks) {
            result.add(TrackSearchResponseDto.builder()
                    .title(track.getTitle())
                    .trackId(track.getId())
                    .nickname(track.getMember().getNickname())
                    .imageUrl(track.getImageUrl())
                    .soundUrl(track.getSoundUrl())
                    .duration(track.getDuration())
                    .build());
        }
        return result;
    }
}
