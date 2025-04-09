package com.ssafy.backend.graph.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ssafy.backend.graph.model.entity.TrackNode;
import com.ssafy.backend.graph.repository.RelationshipRepository;
import com.ssafy.backend.graph.repository.TrackNodeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.neo4j.driver.Driver;
import org.neo4j.driver.Result;
import org.neo4j.driver.Session;
import org.neo4j.driver.TransactionWork;
import org.neo4j.driver.summary.ResultSummary;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;
import org.springframework.web.client.RestTemplate;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@RequiredArgsConstructor
@Component
public class DataReconstructor {

    private final Driver driver;
    private final TrackNodeRepository trackNodeRepository;
    private final RelationshipRepository relationshipRepository;

    @Value("${FASTAPI_HOST}")
    private String FASTAPI_HOST;

    private final int BATCH_SIZE = 1000;
    private final int TIMEOUT_MINUTES = 30;

    /**
     * 1. GET TRACK NODE LIST
     * 2. FAST API REQUEST
     * 3. DELETE ALL SIMILAR RELATIONSHIP
     * 4. REUNION RELATIONSHIP
     */
    public void reconstruct() {
        try {
            StopWatch stopWatch = new StopWatch();
            stopWatch.start("reconstruct");
            log.info("데이터 재구성 시작");

            // 1. GET TRACK NODE LIST
            List<TrackNode> nodes = trackNodeRepository.findAll();
            List<Integer> nodeIds = nodes.stream().map(TrackNode::getId).toList();
            log.info("처리할 트랙 노드 수: {}", nodeIds.size());

            Map<String, List<Integer>> request = new HashMap<>();
            request.put("trackIds", nodeIds);

            // 2. FAST API REQUEST
            Map<String, List<Map<String, Object>>> similarities = requestSimilarityData(request);
            if (similarities == null || similarities.isEmpty()) {
                log.warn("유사성 데이터를 받아오지 못했습니다.");
                return;
            }

            log.info("유사성 데이터 수신 완료: {} 트랙에 대한 데이터", similarities.size());

            // 3. DELETE ALL SIMILAR RELATIONSHIP
            int deletedCount = relationshipRepository.deleteAllSimilarRelationships();
            log.info("기존 유사성 관계 삭제 완료: {} 관계 삭제됨", deletedCount);

            // 4. REUNION RELATIONSHIP - 배치 처리 방식
            int createdCount = createSimilarRelationships(similarities);
            log.info("새로운 유사성 관계 생성 완료: {} 관계 생성됨", createdCount);

            stopWatch.stop();
            log.info("데이터 재구성 완료 - 소요시간 : {}", stopWatch.prettyPrint());
        } catch (Exception e) {
            log.error("데이터 재구성 중 오류 발생", e);
            throw new RuntimeException("데이터 재구성 실패", e);
        }
    }

    private Map<String, List<Map<String, Object>>> requestSimilarityData(Map<String, List<Integer>> nodeIds) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, List<Integer>>> requestEntity = new HttpEntity<>(nodeIds, headers);
            RestTemplate restTemplate = new RestTemplate();

            ResponseEntity<Map<String, List<Map<String, Object>>>> response = restTemplate.exchange(
                    FASTAPI_HOST + "/api/FastAPI/track/similarity",
                    HttpMethod.POST,
                    requestEntity,
                    new ParameterizedTypeReference<Map<String, List<Map<String, Object>>>>() {}
            );

            if (response.getStatusCode() != HttpStatus.OK) {
                log.error("FastAPI 요청 실패: {}", response.getStatusCode());
                return Collections.emptyMap();
            }

            return response.getBody();
        } catch (Exception e) {
            log.error("FastAPI 요청 중 오류 발생", e);
            return Collections.emptyMap();
        }
    }

    private int createSimilarRelationships(Map<String, List<Map<String, Object>>> similarities) {
        int numThreads = Math.min(Runtime.getRuntime().availableProcessors(), 8); // 최대 8개 스레드로 제한
        log.info("사용할 스레드 수: {}", numThreads);

        ExecutorService executorService = Executors.newFixedThreadPool(numThreads);
        AtomicInteger totalCreatedRelationships = new AtomicInteger(0);

        try {
            List<Map<String, Object>> batchParams = new ArrayList<>();

            // 모든 유사성 관계 데이터 수집
            similarities.forEach((key, value) -> {
                for (Map<String, Object> sim : value) {
                    Map<String, Object> params = new HashMap<>();
                    params.put("fromTrackId", Integer.parseInt(key));
                    params.put("toTrackId", Integer.parseInt(sim.get("trackId").toString()));
                    params.put("similarity", Double.parseDouble(sim.get("similarity").toString()));
//                    log.info("from : {}, to : {}, sim : {}",key, sim.get("trackId"), sim.get("similarity"));
                    batchParams.add(params);
                }
            });

            log.info("생성할 유사성 관계 수: {}", batchParams.size());

            // 배치 단위로 처리
            List<Future<Integer>> futures = new ArrayList<>();
            for (int i = 0; i < batchParams.size(); i += BATCH_SIZE) {
                int endIndex = Math.min(i + BATCH_SIZE, batchParams.size());
                List<Map<String, Object>> batch = batchParams.subList(i, endIndex);

                futures.add(executorService.submit(() -> processBatch(batch)));
            }

            // 모든 배치 처리 결과 수집
            for (Future<Integer> future : futures) {
                try {
                    int createdCount = future.get(TIMEOUT_MINUTES, TimeUnit.MINUTES);
                    totalCreatedRelationships.addAndGet(createdCount);
                } catch (Exception e) {
                    log.error("배치 처리 결과 수집 중 오류 발생", e);
                }
            }

            executorService.shutdown();
            if (!executorService.awaitTermination(TIMEOUT_MINUTES, TimeUnit.MINUTES)) {
                log.warn("일부 작업이 제한 시간 내에 완료되지 않았습니다.");
                executorService.shutdownNow();
            }

            return totalCreatedRelationships.get();
        } catch (InterruptedException e) {
            log.error("병렬 처리 중 인터럽트 발생", e);
            Thread.currentThread().interrupt();
            return totalCreatedRelationships.get();
        } finally {
            if (!executorService.isTerminated()) {
                executorService.shutdownNow();
            }
        }
    }

    private int processBatch(List<Map<String, Object>> batch) {
        try (Session session = driver.session()) {
            return session.executeWrite(tx -> {
                String batchQuery =
                        "UNWIND $batch AS item " +
                                "MATCH (t1:Track {id: item.fromTrackId}) " +
                                "MATCH (t2:Track {id: item.toTrackId}) " +
                                "MERGE (t1)-[r:SIMILAR {similarity: item.similarity}]->(t2) " +
                                "RETURN count(r) as createdCount";

                Map<String, Object> parameters = new HashMap<>();
                parameters.put("batch", batch);

                Result result = tx.run(batchQuery, parameters);
                int createdCount = 0;

                if (result.hasNext()) {
                    createdCount = result.next().get("createdCount").asInt();
                }

                ResultSummary summary = result.consume();
                log.debug("배치 처리 결과: 생성된 관계 수 = {}, 쿼리 요약 = {}",
                        createdCount, summary.counters().relationshipsCreated());

                return createdCount;
            });
        } catch (Exception e) {
            log.error("배치 처리 중 오류 발생: {}", e.getMessage(), e);
            return 0;
        }
    }


    // 테스트용 단일 트랜잭션 메소드 추가
    public void testSingleRelationship() {
        try (Session session = driver.session()) {
            session.executeWrite(tx -> {
                String query = "MATCH (t1:Track {id: $fromId}) " +
                        "MATCH (t2:Track {id: $toId}) " +
                        "MERGE (t1)-[r:SIMILAR {similarity: $sim}]->(t2) " +
                        "RETURN r";
                Map<String, Object> params = new HashMap<>();
                params.put("fromId", 95);
                params.put("toId", 28);
                params.put("sim", 0.0104);
                Result result = tx.run(query, params);
                return result.hasNext();
            });
        }
    }


}
