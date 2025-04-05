package com.ssafy.backend.openl3.service;

import com.ssafy.backend.mysql.entity.Layer;
import com.ssafy.backend.mysql.repository.LayerRepository;
import com.ssafy.backend.workstation.dto.response.LayerImportResponseDto;
import com.ssafy.backend.workstation.dto.response.TrackImportResponseDto;
import com.ssafy.backend.workstation.service.WorkstationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class Openl3Service {

    @Value("${BACKEND_HOST}")
    private String BACKEND_HOST;

    @Value("${FASTAPI_HOST}")
    private String FASTAPI_HOST;

    WorkstationService workstationService;
    LayerRepository layerRepository;

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

    public TrackImportResponseDto recommendImportTrack(Integer[] layerIds) {

        List<Layer> layers = layerRepository.findAllById(List.of(layerIds));

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

        WebClient fastAPIClient = WebClient.builder().baseUrl(FASTAPI_HOST).build();
        Integer recommendedTrackId = fastAPIClient.post()
                .uri("/api/FastAPI/track/recommend")
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

        TrackImportResponseDto track = workstationService.importTrack(recommendedTrackId);

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
