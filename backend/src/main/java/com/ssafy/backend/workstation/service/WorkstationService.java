package com.ssafy.backend.workstation.service;

import com.ssafy.backend.auth.service.AuthService;
import com.ssafy.backend.common.error.exception.TrackNotFoundException;
import com.ssafy.backend.common.service.S3Service;
import com.ssafy.backend.graph.service.DataCollectingService;
import com.ssafy.backend.mysql.entity.*;
import com.ssafy.backend.mysql.repository.*;
import com.ssafy.backend.openl3.service.Openl3Service;
import com.ssafy.backend.track.dto.request.TrackUploadRequestDto;
import com.ssafy.backend.workstation.dto.response.LayerImportResponseDto;
import com.ssafy.backend.workstation.dto.response.TrackImportResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class WorkstationService {
    private final TrackRepository trackRepository;
    private final TrackTagRepository trackTagRepository;
    private final LayerRepository layerRepository;
    private final SamplingRepository samplingRepository;
    private final LayerFileRepository layerFileRepository;

    private final DataCollectingService dataCollectingService;
    private final S3Service s3Service;
    private final Openl3Service openl3Service;

    private final AuthService authService;

    /**
     * 트랙의 업로드
     *
     * @param trackUploadRequestDto 업로드 정보
     */
    @Transactional
    public int createTrack(TrackUploadRequestDto trackUploadRequestDto) {
        Member m = authService.getMember();
        // 트랜젝션
        Member member = new Member();
        member.setId(m.getId());
        // 1. 트랙 생성
        Track track = Track.builder()
                .title(trackUploadRequestDto.getTitle()).description(trackUploadRequestDto.getDescription())
                .member(member).blocked(false).duration(trackUploadRequestDto.getDuration()).visibility(trackUploadRequestDto.isVisibility())
                .enabled(true).importCount(0).viewCount(0).likeCount(0)
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
                            .id(TrackTagId.builder()
                                    .tagId(tagId)
                                    .trackId(track.getId()).build())
                            .track(t)
                            .tag(tag)
                            .build();
                })
                .collect(Collectors.toList());
        trackTagRepository.saveAll(trackTags);
        // 1-3-2. 원천 트랙 insert
        List<Sampling> samplings = Arrays.stream(trackUploadRequestDto.getSourceTracks())
                .map(originTrackId -> Sampling.builder()
                        .originTrack(trackRepository.findById(originTrackId).orElse(null))
                        .track(t).build()).toList();
        samplingRepository.saveAll(samplings);

        // 1-4. 그래프 추가
        // 1-4-1. Track 노드 생성 및 태그 연결
        dataCollectingService.createTrack(t.getId(), Arrays.stream(trackUploadRequestDto.getTags()).toList());
        // 1-4-2. FastAPI로 음원 보내기
        openl3Service.uploadAndFindSimilar(trackUploadRequestDto.getTrackSoundFile().getResource(), t.getId(), 10);

        // 2. 레이어 목록 저장
        int layerSize = trackUploadRequestDto.getLayerName().length;
        for (int i = 0; i < layerSize; i++) {
            // 2-1. 음성 업로드
            LayerFile layerFile = new LayerFile();
            layerFile.setSoundUrl(s3Service.uploadFile(trackUploadRequestDto.getLayerSoundFiles()[i], S3Service.MUSIC));
            LayerFile lf = layerFileRepository.save(layerFile);

            Layer layer = Layer.builder()
                    .name(trackUploadRequestDto.getLayerName()[i])
                    .instrumentType(trackUploadRequestDto.getInstrumentType()[i])
                    .blocked(false)
                    .track(t)
                    .layerFile(lf).build();
            // 2-2. 레이어 정보 insert
            layerRepository.save(layer);

        }
        return t.getId();
    }

    /**
     * 트랙 임포트
     * @param trackId 임포트 대상 트랙 ID
     * @return 트랙 및 하위 레이어 정보
     */
    public TrackImportResponseDto importTrack(int trackId) {
        // 임포트 가능 여부 검사
        Track track = trackRepository.findById(trackId).orElseThrow(
                () -> new TrackNotFoundException("Track not found")
        );
        List<Layer> layers = layerRepository.findAllByTrackId(trackId);
        TrackImportResponseDto trackImportResponseDto = TrackImportResponseDto.builder()
                .trackId(track.getId())
                .title(track.getTitle())
                .layers(new ArrayList<>())
                .imageUrl(track.getImageUrl())
                .soundUrl(track.getSoundUrl())
                .build();

        for (Layer layer : layers) {
            trackImportResponseDto.getLayers().add(LayerImportResponseDto.builder()
                    .layerId(layer.getId())
                    .trackId(track.getId())
                    .name(layer.getName())
                    .instrumentType(layer.getInstrumentType())
                    .soundUrl(layer.getLayerFile().getSoundUrl())
                    .build());
        }
        return trackImportResponseDto;
    }
}
