package com.ssafy.backend.workstation.service;

import com.ssafy.backend.auth.service.AuthService;
import com.ssafy.backend.common.error.exception.TrackNotFoundException;
import com.ssafy.backend.common.service.S3Service;
import com.ssafy.backend.graph.service.DataCollectingService;
import com.ssafy.backend.mysql.entity.*;
import com.ssafy.backend.mysql.repository.*;
import com.ssafy.backend.track.dto.request.TrackUploadRequestDto;
import com.ssafy.backend.workstation.dto.response.LayerImportResponseDto;
import com.ssafy.backend.workstation.dto.response.TrackImportResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.util.*;
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

    private final AuthService authService;

    @Value("${BACKEND_HOST}")
    private String BACKEND_HOST;

    @Value("${FASTAPI_HOST}")
    private String FASTAPI_HOST;

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
        uploadAndFindSimilar(trackUploadRequestDto.getTrackSoundFile().getResource(), t.getId(), trackUploadRequestDto.getInstrumentType(), 10);

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

    /**
     * FastAPI에 음원 업로드 및 유사 트랙 찾기
     *
     * @param audioFile       업로드할 음원 파일
     * @param trackId         트랙 ID
     * @param instrumentTypes 악기 유형 배열
     * @param limit           유사 트랙 개수 제한
     */
    public void uploadAndFindSimilar(Resource audioFile, Integer trackId, int[]instrumentTypes, int limit) {
        try {
            // 1. 파일을 즉시 byte 배열로 읽어들입니다.
            byte[] audioBytes = audioFile.getInputStream().readAllBytes();

            MultipartBodyBuilder builder = new MultipartBodyBuilder();

            // 2. Resource 대신 byte 배열을 body part로 추가합니다.
            builder.part("audio", audioBytes) // byte[] 사용
                    .filename(audioFile.getFilename())
                    .contentType(MediaType.APPLICATION_OCTET_STREAM); // 필요시 MediaType 조정

            // 폼 필드 추가
            if(trackId != null) {
                builder.part("trackId", trackId);
            }

            // 배열의 각 요소를 별도의 파트로 추가
            if (instrumentTypes != null) {
                for (int type : instrumentTypes) {
                    builder.part("instrumentTypes", type); // 같은 이름("instrumentTypes")으로 각 int 값을 추가
                }
            }

            builder.part("limit", limit);
            builder.part("callbackUrl", BACKEND_HOST + "/api/openl3/similar/callback");

            log.info("콜백 URL: {}", BACKEND_HOST + "/api/openl3/similar/callback");

            WebClient fastAPIClient = WebClient.builder().baseUrl(FASTAPI_HOST).build();

            // 3. WebClient 호출 (이제 audioBytes를 사용하므로 임시 파일 문제 없음)
            fastAPIClient.post()
                    .uri("/api/FastAPI/track/upload/async")
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    .body(BodyInserters.fromMultipartData(builder.build()))
                    .retrieve()
                    .bodyToMono(Void.class)
                    .doOnSuccess(unused -> log.info("backend->fastapi 요청 성공"))
                    .doOnError(error -> log.error("backend->fastapi 요청 실패: {}", error.getMessage()))
                    .subscribe();

        } catch (IOException e) {
            log.error("파일을 byte 배열로 읽는 중 오류 발생: {}", e.getMessage(), e);
        }
    }

    /**
     * ai 트랙 추천 임포트
     * @param layerIds 레이어 ID 목록
     * @return 추천된 트랙 및 레이어 정보
     */
    public TrackImportResponseDto recommendImportTrack(List<Integer> layerIds) {

        List<Layer> layers = layerRepository.findAllById(layerIds);

        Set<Integer> instrumentTypeSet = new HashSet<>();
        Set<Integer> trackIdSet = new HashSet<>();

        for (Layer layer : layers) {
            instrumentTypeSet.add(layer.getInstrumentType());
            trackIdSet.add(layer.getTrack().getId());
        }

        //- 0 Record
        //- 1 Whistle
        //- 2 Acoustic Guitar
        //- 3 Voice
        //- 4 Drums
        //- 5 Bass
        //- 6 Electric Guitar
        //- 7 Piano
        //- 8 Synth

        // 드럼이 없으면 드럼, 그다음 bass, 그다음은 아무거나

        List<Integer> needInstrumentTypes = new ArrayList<>();
        if (!instrumentTypeSet.contains(4)) {
            needInstrumentTypes.add(4);
        } else if (instrumentTypeSet.contains(5)) {
            needInstrumentTypes.add(5);
        } else {
            // 4,5빼고 전부
            for (int i = 0; i < 9; i++) {
                if (i != 4 && i != 5) {
                    needInstrumentTypes.add(i);
                }
            }
        }
        log.info("추천할 트랙의 instrumentType: {}", instrumentTypeSet);
        log.info("추천 요청 instrumentType: {}", needInstrumentTypes);

        WebClient fastAPIClient = WebClient.builder().baseUrl(FASTAPI_HOST).build();
        Integer recommendedTrackId = fastAPIClient.post()
                .uri("/api/FastAPI/track/ai/recommend")
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(Map.of("needInstrumentTypes", needInstrumentTypes, "trackIds", trackIdSet)))
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, clientResponse -> {
                    log.error("FastAPI 4xx 에러: {}", clientResponse.statusCode());
                    return Mono.error(new RuntimeException("FastAPI 4xx 에러"));
                })
                .onStatus(HttpStatusCode::is5xxServerError, clientResponse -> {
                    log.error("FastAPI 5xx 에러: {}", clientResponse.statusCode());
                    return Mono.error(new RuntimeException("FastAPI 5xx 에러"));
                })
                .bodyToMono(Integer.class)
                .block();

        if (recommendedTrackId == null) {
            log.error("추천된 트랙 ID가 null입니다.");
            throw new RuntimeException("추천된 트랙 ID가 null입니다.");
        }

        log.info("추천된 트랙 ID: {}", recommendedTrackId);

        TrackImportResponseDto track = importTrack(recommendedTrackId);

        // 레이어 정보를 요구했던 instrumentType으로 필터링
        List<LayerImportResponseDto> filteredLayers = new ArrayList<>();
        for(LayerImportResponseDto layer : track.getLayers()) {
            if (needInstrumentTypes.contains(layer.getInstrumentType())) {
                filteredLayers.add(layer);
            }
        }

        track.setLayers(filteredLayers);

        return track;
    }
}
