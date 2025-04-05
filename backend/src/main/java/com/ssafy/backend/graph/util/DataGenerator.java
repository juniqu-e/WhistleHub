package com.ssafy.backend.graph.util;

import com.ssafy.backend.graph.model.entity.type.WeightType;
import com.ssafy.backend.graph.repository.MemberNodeRepository;
import com.ssafy.backend.graph.repository.TrackNodeRepository;
import com.ssafy.backend.graph.service.DataCollectingService;
import com.ssafy.backend.mysql.entity.*;
import com.ssafy.backend.mysql.repository.*;
import lombok.RequiredArgsConstructor;
import org.neo4j.driver.Driver;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class DataGenerator {

    private final DataCollectingService dataCollectingService;

    private final MemberNodeRepository memberNodeRepository;
    private final TrackNodeRepository trackNodeRepository;

    private final Driver driver;
    private static final int MEMBER_COUNT = 500; // 사용자 1만명
    private static final int TRACK_COUNT = 3000; // 아이템 2만개
    private static final int TAG_COUNT = 200; // 태그 500개
    private final TagRepository tagRepository;

    public void gerating() {
        // 사용자 생성
        for (int i = 1; i <= MEMBER_COUNT; i++) {
            dataCollectingService.createMember(i);
        }
        // 트랙 생성
        for (int i = 1; i <= TRACK_COUNT; i++) {
            Set<Integer> tagSet = new HashSet<>();
            // 1~5 랜덤 값
            int havTag = new Random().nextInt(5) + 1; // 0~4에 1을 더해서 1~5로 만듦
            for (int j = 1; j <= havTag; j++) {
                tagSet.add(new Random().nextInt(TAG_COUNT) + 1); // 0~TAG_COUNT-1에 1을 더해서 1~TAG_COUNT로 만듦
            }
            dataCollectingService.createTrack(i, new ArrayList<>(tagSet));
        }

        // 랜덤 방문
        for (int i = 1; i <= MEMBER_COUNT; i++) {
            // 몇개의 방문을 할 지 3~30
            int visitCount = new Random().nextInt(28) + 3; // 0~27에 3을 더해 3~30 범위로
            for (int j = 0; j < visitCount; j++) {
                dataCollectingService.viewTrack(i, new Random().nextInt(TRACK_COUNT) + 1, WeightType.VIEW);
            }
        }
        System.out.println("🎉 데이터 삽입 완료");
    }

    public void generate(int memberCount, int trackCount) {
        int wholeMemberCount = memberNodeRepository.findAll().size();
        int wholeTrackCount = trackNodeRepository.findAll().size();
        int wholeTagCount = 46;

        Random random = new Random();
        // 사용자 생성
        // batch  처리
        System.out.println("멤버 생성 시작 ⭐⭐⭐⭐️ (1/7)");
        try (var session = driver.session()) {
            List<Map<String, Object>> listOfObjects = new LinkedList<>();
            System.out.println("모든 멤버에 대해서 태그를 생성합니다.");
            for (int i = wholeMemberCount + 1; i <= wholeMemberCount + memberCount; i++) {
                Map<String, Object> map = new HashMap<>();
                map.put("id", i);
                listOfObjects.add(map);
            }
            Map<String, Object> params = new HashMap<>();
            params.put("props", listOfObjects);
            String query = "UNWIND $props AS map " +
                    "CREATE (m:Member) SET m = map ";
            System.out.println("멤버 쿼리 실행");
            session.writeTransaction(tx -> tx.run(
                    query, params
            ).consume());
        }
        System.out.println("멤버 생성 종료 ⭐⭐⭐⭐️");
        // 랜덤 태그 좋아요
        // batch 처리
        System.out.println("태그 좋아요 시작 ⭐⭐⭐⭐️ (2/7)");
         try(var session = driver.session()){
                List<Map<String,Object>> tagAddList = new LinkedList<>();
             System.out.println("모든 멤버에 대해서 태그를 생성합니다.");
                for(int i= wholeMemberCount + 1; i <= wholeMemberCount + memberCount; i++){
                    // 태그 개수는 최소 1개, 최대 9개 (1~9)
                    int tagCount = Math.max(1, random.nextInt(10));
                    List<Integer> tagList = getRandomNumbers(1, wholeTagCount, tagCount);
                    Map<String,Object> map = new HashMap<>();
                    map.put("id", i);
                    map.put("tagList", tagList);
                    tagAddList.add(map);
                }
                System.out.println("태그 쿼리 실행");
                Map<String,Object> tagAddParam = new HashMap<>();
                tagAddParam.put("props", tagAddList);
                String tagAddQuery = "UNWIND $props AS map " +
                        "MATCH (m:Member {id: map.id}) " +
                        "WITH m, map.tagList AS tagList " +
                        "UNWIND tagList AS tagId " +
                        "MATCH (tag:Tag {id: tagId}) " +
                        "CREATE (m)-[:PREFER{weight: 10.0}]->(tag)";
                session.writeTransaction(tx -> tx.run(
                        tagAddQuery, tagAddParam
                ).consume());
         }
        System.out.println("태그 좋아요 종료 ⭐⭐⭐⭐️");
        System.out.println("팔로우 시작 ⭐⭐⭐⭐️(3/7)");
        // 랜덤 팔로우
        try (var session = driver.session()) {
            List<Map<String, Object>> followAddList = new LinkedList<>();
            int maxPossibleFollows = wholeMemberCount + memberCount - 1; // 자기 자신을 제외

            // 팔로우 개수는 1~10
            System.out.println("모든 멤버에 대해서 팔로우를 생성합니다.");
            for (int i = wholeMemberCount + 1; i <= wholeMemberCount + memberCount; i++) {
                int followCount = Math.min(maxPossibleFollows, Math.max(1, random.nextInt(10)));
                List<Integer> followList = getRandomNumbers(1, wholeMemberCount + memberCount, followCount);

                // 자기 자신은 팔로우 목록에서 제거
                for(int j = 0; j < followList.size(); j++) {
                    if (followList.get(j) == i) {
                        followList.remove(j);
                    }
                }

                if (!followList.isEmpty()) {
                    Map<String, Object> map = new HashMap<>();
                    map.put("id", i);
                    map.put("followList", followList);
                    followAddList.add(map);
                }
            }

            System.out.println("팔로우 쿼리 실행");
            if (!followAddList.isEmpty()) {
                Map<String, Object> followAddParam = new HashMap<>();
                followAddParam.put("props", followAddList);
                String followAddQuery = "UNWIND $props AS map " +
                        "MATCH (m:Member {id: map.id}) " +
                        "WITH m, map.followList AS followList " +
                        "UNWIND followList AS followId " +
                        "MATCH (follow:Member {id: followId}) " +
                        "CREATE (m)-[:FOLLOW]->(follow)";
                session.writeTransaction(tx -> tx.run(
                        followAddQuery, followAddParam
                ).consume());
            }
        }
        System.out.println("팔로우 종료 ⭐⭐⭐⭐️");

        // 트랙 생성
        // batch 처리
        System.out.println("트랙 생성 시작 ⭐⭐⭐⭐️(4/7)");
        try (var session = driver.session()) {
            List<Map<String, Object>> trackAddList = new LinkedList<>();
            System.out.println("모든 트랙에 대해서 태그를 생성합니다.");
            for (int i = wholeTrackCount + 1; i <= wholeTrackCount + trackCount; i++) {
                Map<String, Object> map = new HashMap<>();
                map.put("id", i);
                trackAddList.add(map);
            }

            Map<String, Object> trackAddParam = new HashMap<>();
            trackAddParam.put("props", trackAddList);
            String trackAddQuery = "UNWIND $props AS map " +
                    "CREATE (t:Track) SET t = map ";
            System.out.println("트랙 쿼리 실행");
            session.writeTransaction(tx -> tx.run(
                    trackAddQuery, trackAddParam
            ).consume());
        }
        System.out.println("트랙 생성 종료 ⭐⭐⭐⭐️");

        // 태그 연결
        // batch 처리
        System.out.println(" 트랙 태그 연결 시작 ⭐⭐⭐⭐️ (5/7)");
         // 태그 연결
        try (var session = driver.session()) {
            List<Map<String, Object>> tagAddList = new LinkedList<>();
            System.out.println("모든 트랙에 대해서 태그를 생성합니다.");
            for (int i = wholeTrackCount + 1; i <= wholeTrackCount + trackCount; i++) {
                // 태그 개수 1~10 (범위 체크 추가)
                int tagCount = Math.max(1, Math.min(wholeTagCount, random.nextInt(11)));
                List<Integer> tagList = getRandomNumbers(1, wholeTagCount, tagCount);
                Map<String, Object> map = new HashMap<>();
                map.put("id", i);
                map.put("tagList", tagList);

                tagAddList.add(map);
            }
            System.out.println("트랙 태그 쿼리 실행");
            Map<String, Object> tagAddParam = new HashMap<>();
            tagAddParam.put("props", tagAddList);
            String tagAddQuery = "UNWIND $props AS map " +
                    "MATCH (t:Track {id: map.id}) " +
                    "WITH t, map.tagList AS tagList " +
                    "UNWIND tagList AS tagId " +
                    "MATCH (tag:Tag {id: tagId}) " +
                    "CREATE (t)-[:HAVE]->(tag)";
            session.writeTransaction(tx -> tx.run(
                    tagAddQuery, tagAddParam
            ).consume());
        }
        System.out.println("트랙 태그 연결 종료 ⭐⭐⭐⭐️");

        // similar 연결
        // batch 처리
        System.out.println("트랙 유사도 연결 시작 ⭐⭐⭐⭐️ (6/7)");
        try (var session = driver.session()) {
            List<Map<String,Object>> similarAddList = new LinkedList<>();
            int totalTracks = wholeTrackCount + trackCount;
            // 각 트랙마다 유사한 트랙 최대 10개까지 연결
            System.out.println("모든 트랙에 대해서 유사도를 생성합니다.");
            for(int i = wholeTrackCount + 1; i <= wholeTrackCount + trackCount; i++){
                // 전체 트랙 수가 10보다 많을 경우에만 진행
                if (totalTracks > 1) {
                    int similarCount = Math.min(10, totalTracks - 1); // 자기 자신 제외하고 최대 10개
                    List<Integer> similarList = getRandomNumbers(1, totalTracks, similarCount);
                    // 자기 자신 제외
                    for(int j = 0; j < similarList.size(); j++) {
                        if (similarList.get(j) == i) {
                            similarList.remove(j);
                        }
                    }

                    if (!similarList.isEmpty()) {
                        Map<String,Object> map = new HashMap<>();
                        map.put("id", i);
                        map.put("similarList", similarList);
                        map.put("similarity", random.nextDouble() * 0.97 + 0.02); // 0.02~0.99 범위의 랜덤값
                        similarAddList.add(map);
                    }
                }
            }
            // 유사도 연결 쿼리 실행
            System.out.println("트랙 유사도 쿼리 실행");
            if (!similarAddList.isEmpty()) {
                Map<String,Object> similarAddParam = new HashMap<>();
                similarAddParam.put("props", similarAddList);
                String similarAddQuery = "UNWIND $props AS map " +
                        "MATCH (t:Track {id: map.id}) " +
                        "WITH t, map.similarList AS similarList, map.similarity AS similarity " +
                        "UNWIND similarList AS similarId " +
                        "MATCH (similar:Track {id: similarId}) " +
                        "CREATE (t)-[:SIMILAR {similarity: similarity}]->(similar)";

                session.writeTransaction(tx -> tx.run(
                        similarAddQuery, similarAddParam
                ).consume());
            }
        }
        System.out.println("트랙 유사도 연결 종료 ⭐⭐⭐⭐️");

        //  랜덤 방문
        // batch 처리
        System.out.println("트랙 방문 시작 ⭐⭐⭐⭐️ (7/7)");
        try (var session = driver.session()) {
            List<Map<String, Object>> viewAddList = new LinkedList<>();
            System.out.println("모든 멤버에 대해서 트랙 방문을 생성합니다.");
            for (int i = wholeMemberCount + 1; i <= wholeMemberCount + memberCount; i++) {
                List<Map<String, Object>> viewList = new LinkedList<>();
                // 몇개의 방문을 할 지 3~30
                int visitCount = random.nextInt(28) + 3; // 0~27에 3을 더해 3~30 범위로
                List<Integer> trackIdList = getRandomNumbers(1, wholeTrackCount + trackCount, visitCount);
                // 방문할 트랙이 존재하는지 확인
                if (wholeTrackCount + trackCount >= wholeTrackCount + 1) {
                    for (int j = 0; j < visitCount; j++) {
                        Map<String, Object> visitMap = new HashMap<>();
                        int trackId = trackIdList.get(j);
                        visitMap.put("id", trackId);
                        int selection = random.nextInt(3) + 1; // 1~3
                        int weight = 0;
                        switch (selection) {
                            case 1:
                                weight = WeightType.LIKE.getValue();
                                break;
                            case 2:
                                weight = WeightType.DISLIKE.getValue();
                                break;
                            case 3:
                                weight = WeightType.VIEW.getValue();
                                break;
                        }
                        visitMap.put("weight", weight);
                        viewList.add(visitMap);

                    }
                    Map<String, Object> map = new HashMap<>();
                    map.put("id", i);
                    map.put("viewList", viewList);
                    viewAddList.add(map);
                }
            }

            // 방문 쿼리 실행
            System.out.println("트랙 방문 쿼리 실행");
            if (!viewAddList.isEmpty()) {
                Map<String, Object> viewAddParam = new HashMap<>();
                viewAddParam.put("props", viewAddList);
                String viewAddQuery = "UNWIND $props AS map " +
                        "MATCH (m:Member {id: map.id}) " +
                        "WITH m, map.viewList AS viewList " +
                        "UNWIND viewList AS trackData " +
                        "MATCH (t:Track {id: trackData.id}) " +
                        "CREATE (m)-[:LIKE {weight: trackData.weight}]->(t)";

                session.writeTransaction(tx -> tx.run(
                        viewAddQuery, viewAddParam
                ).consume());
            }
        }
        System.out.println("트랙 방문 종료 ⭐⭐⭐⭐️");
    }

    /**
     * a부터 b까지의 숫자 중에서 중복 없이 K개의 숫자를 무작위로 추출하여 배열로 반환합니다.
     * 안전한 범위 검사를 추가했습니다.
     */
    public List<Integer> getRandomNumbers(int a, int b, int K) {
        // 범위 유효성 검사
        if (a > b) {
            int temp = a;
            a = b;
            b = temp;
        }

        // 범위 내 가능한 숫자 수
        int N = b - a + 1;

        // K가 범위보다 크면 K를 범위로 제한
        K = Math.min(K, N);

        // 추출할 숫자가 없으면 빈 리스트 반환
        if (K <= 0) {
            return new ArrayList<>();
        }

        // a부터 b까지의 숫자를 배열에 초기화
        int[] numbers = new int[N];
        for (int i = 0; i < N; i++) {
            numbers[i] = a + i;
        }

        Random rand = new Random();

        // partial Fisher-Yates 셔플: K개만 필요하므로 K번만 섞음
        for (int i = 0; i < K; i++) {
            // i번째 원소와 i ~ (N-1) 사이의 임의의 원소를 교환
            int remainingItems = N - i;
            if (remainingItems <= 1) break; // 더 이상 섞을 원소가 없음

            int j = i + rand.nextInt(remainingItems);
            int temp = numbers[i];
            numbers[i] = numbers[j];
            numbers[j] = temp;
        }

        // 결과 배열에 앞의 K개 원소를 복사
        int[] result = new int[K];
        System.arraycopy(numbers, 0, result, 0, K);
        return Arrays.stream(result).boxed().collect(Collectors.toList());
    }
}