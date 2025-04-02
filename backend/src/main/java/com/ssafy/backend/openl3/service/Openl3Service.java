package com.ssafy.backend.openl3.service;

import com.ssafy.backend.common.ApiResponse;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class Openl3Service {

    @Value("${BACKEND_HOST}")
    private String BACKEND_HOST;

    @Value("${FASTAPI_HOST}")
    private String FASTAPI_HOST;

    public void uploadAndFindSimilar(Resource audioFile, Integer trackId, int limit) {
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
}
