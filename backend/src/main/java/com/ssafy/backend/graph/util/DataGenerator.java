package com.ssafy.backend.graph.util;

import com.ssafy.backend.graph.model.entity.type.WeightType;
import com.ssafy.backend.graph.repository.MemberNodeRepository;
import com.ssafy.backend.graph.repository.TrackNodeRepository;
import com.ssafy.backend.graph.service.DataCollectingService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Component
@RequiredArgsConstructor
public class DataGenerator {

    private final DataCollectingService dataCollectingService;

    private static final int MEMBER_COUNT = 500; // 사용자 1만명
    private static final int TRACK_COUNT = 3000; // 아이템 2만개
    private static final int TAG_COUNT = 200; // 태그 500개

    public void gerating(){
        // 사용자 생성
        for(int i = 1; i <= MEMBER_COUNT; i++){
            dataCollectingService.createMember(i);
        }
        // 트랙 생성
        for(int i = 1; i <= TRACK_COUNT; i++){
            Set<Integer> tagSet = new HashSet<>();
            // 1~5 랜덤 값
            int havTag = new Random().nextInt(5);
            for(int j = 1; j <= havTag; j++){
                tagSet.add(new Random().nextInt(TAG_COUNT));
            }
            dataCollectingService.createTrack(i, new ArrayList<>(tagSet));
        }
        
        // 랜덤 방문
        for(int i = 1; i <= MEMBER_COUNT; i++){
            // 몇개의 방문을 할 지 3~30
            int visitCount = new Random().nextInt(3, 30);
            for(int j = 0; j < visitCount; j++){
                dataCollectingService.viewTrack(i, new Random().nextInt(TRACK_COUNT), WeightType.VIEW);
            }
        }
        System.out.println("🎉 데이터 삽입 완료");
    }

}

