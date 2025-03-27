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

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class Openl3Service {

    private final WebClient fastAPIClient;

    @Value("${BACKEND_HOST}")
    private String BACKEND_HOST;

    public void uploadAndFindSimilar(Resource audioFile, Integer trackId, int limit) {
        MultipartBodyBuilder builder = new MultipartBodyBuilder();

        // 오디오 파일 파트 추가
        builder.part("audio", audioFile)
                .filename(audioFile.getFilename())
                .contentType(MediaType.APPLICATION_OCTET_STREAM);

        // 폼 필드 추가
        if(trackId != null) {
            builder.part("trackId", trackId);
        }
        builder.part("limit", limit);
        builder.part("callbackUrl", BACKEND_HOST + "/api/openl3/similar/callback");

        fastAPIClient.post()
                .uri("/upload/async")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(BodyInserters.fromMultipartData(builder.build()))
                .exchangeToMono(response -> {
                    // 응답 상태 코드 로깅
                    log.info("응답 상태 코드: {}", response.statusCode());

                    if (response.statusCode().isError()) {
                        return response.bodyToMono(String.class)
                                .flatMap(error -> Mono.error(new RuntimeException(error)));
                    } else {
                        return response.bodyToMono(new ParameterizedTypeReference<ApiResponse<Map<String, Object>>>() {});
                    }
                })
                .subscribe(
                        result -> log.info("요청 성공"),
                        error -> log.error("요청 실패: {}", error.getMessage())
                );
    }
}
