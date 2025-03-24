package com.ssafy.backend.track.service;

import com.ssafy.backend.common.service.S3Service;
import com.ssafy.backend.common.util.S3FileKeyExtractor;
import com.ssafy.backend.graph.service.DataCollectingService;
import com.ssafy.backend.mysql.entity.*;
import com.ssafy.backend.mysql.repository.LayerFileRepository;
import com.ssafy.backend.mysql.repository.LayerRepository;
import com.ssafy.backend.mysql.repository.TrackRepository;
import com.ssafy.backend.mysql.repository.TrackTagRepository;
import com.ssafy.backend.track.dto.request.TrackUploadRequestDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    private final DataCollectingService dataCollectingService;
    private final S3Service s3Service;

    /**
     * 트랙 음원 데이터를 반환
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
                // 1-1. 트랙 이미지 업로드
                .imageUrl(s3Service.uploadFile(trackUploadRequestDto.getTrackImg(), S3Service.IMAGE))
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
}
