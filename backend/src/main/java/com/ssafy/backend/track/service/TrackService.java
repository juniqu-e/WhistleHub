package com.ssafy.backend.track.service;

import com.ssafy.backend.common.service.S3Service;
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

    public byte[] trackPlay(int trackId) {
        String tempUrl = "https://whistlehub.s3.ap-northeast-2.amazonaws.com/%EA%B5%AD%EC%95%85+%ED%9A%A8%EA%B3%BC%EC%9D%8C+%23542.mp3";



        return s3Service.downloadFile("sound/battle-of-the-dragons-8037.mp3");
    }

    /**
     * 트랙의 업로드
     * @param trackUploadRequestDto 업로드 정보
     * @param memberId 업로드 멤버 id
     */
    @Transactional
    public void createTrack(TrackUploadRequestDto trackUploadRequestDto, int memberId) {
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
                .soundUrl(s3Service.uploadFile(trackUploadRequestDto.getFiles()[0], S3Service.MUSIC))
                .build();
        // 1-3. 트랙 insert
        Track t = trackRepository.save(track);
        // 1-3-1. 연결된 태그 insert
        for(int tagId:trackUploadRequestDto.getTags()){
            TrackTag trackTag = new TrackTag();
            Tag tag = new Tag();
            tag.setId(tagId);
            trackTag.setTrack(t);
            trackTag.setTag(tag);
            trackTagRepository.save(trackTag);
        }
        // 1-4. 그래프 추가
        // 1-4-1. Track 노드 생성 및 태그 연결
        dataCollectingService.createTrack(t.getId(), Arrays.stream(trackUploadRequestDto.getTags()).toList());
        // 1-4-2. TODO: FastAPI로 음원 보내기
        // 2. 레이어 목록 저장
        for(int i = 0; i < trackUploadRequestDto.getLayers().size(); i++){
        // 2-1. 음성 업로드
            LayerFile layerFile = new LayerFile();
            layerFile.setSoundUrl(s3Service.uploadFile(trackUploadRequestDto.getFiles()[i+1], S3Service.MUSIC));
            LayerFile lf = layerFileRepository.save(layerFile);

            Layer layer = new Layer();
            layer.setName(trackUploadRequestDto.getLayers().get(i).getName());
            layer.setInstrumentType(trackUploadRequestDto.getLayers().get(i).getInstrumentType());
            layer.setBlocked(false);
            layer.setTrack(t);
            layer.setLayerFile(lf);
        // 2-2. 레이어 정보 insert
            layerRepository.save(layer);

        }
    }
}
