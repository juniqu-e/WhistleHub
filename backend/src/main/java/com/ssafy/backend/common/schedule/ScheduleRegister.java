package com.ssafy.backend.common.schedule;

import com.ssafy.backend.discovery.service.RankingService;
import com.ssafy.backend.graph.util.DataReconstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ScheduleRegister {

    private final DataReconstructor dataReconstructor;
    private final RankingService rankingService;

    @Scheduled(cron = "0 0 0 * * *")
    @Scheduled(cron = "0 0 12 * * *")
    public void relationshipReconstruction() {
        dataReconstructor.reconstruct();
    }

    @Scheduled(cron = "0 0 0 * * *")
    public void updateRanking() {
        rankingService.doUpdateAllRanking();
    }
}
