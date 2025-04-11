package com.ssafy.backend.common.config;

import com.ssafy.backend.discovery.service.RankingService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.scheduling.annotation.Scheduled;

@Configuration
@RequiredArgsConstructor
public class RankingConfig {

    private final RankingService rankingService;

    /**
     * <pre>RankingService 초기화</pre>
     * RankingService의 doUpdateAllRanking 메서드를 호출하여 초기화 작업을 수행한다.
     * order는 2로 설정하여 redis 초기화 후에 실행되도록 한다.
     * @see com.ssafy.backend.common.config.RedisConfig
     * @param rankingService RankingService
     * @return ApplicationRunner
     */
    @Bean
    @Order(2)
    public ApplicationRunner initializeRankings(RankingService rankingService) {
        return args -> {
            rankingService.doUpdateAllRanking();
        };
    }
}