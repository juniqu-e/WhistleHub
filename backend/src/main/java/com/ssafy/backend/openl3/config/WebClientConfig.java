package com.ssafy.backend.openl3.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
@Slf4j
public class WebClientConfig {

    @Value("${FASTAPI_HOST}")
    private String FASTAPI_HOST;

    @Bean
    public WebClient fastAPIClient() {
        log.info("Fast API host: {}", FASTAPI_HOST);
        return WebClient.builder()
                .baseUrl(FASTAPI_HOST) // FastAPI 서버의 URL
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(10 * 1024 * 1024)) // 10MB 설정
                .build();
    }
}
