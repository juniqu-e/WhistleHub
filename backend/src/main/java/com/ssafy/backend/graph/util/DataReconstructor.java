package com.ssafy.backend.graph.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ssafy.backend.graph.model.entity.TrackNode;
import com.ssafy.backend.graph.repository.RelationshipRepository;
import com.ssafy.backend.graph.repository.TrackNodeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.neo4j.driver.Driver;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;
import org.springframework.web.client.RestTemplate;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import java.util.*;

@Slf4j
@RequiredArgsConstructor
@Component
public class DataReconstructor {

    private final Driver driver;
    private final TrackNodeRepository trackNodeRepository;
    private final RelationshipRepository relationshipRepository;

    @Value("${FASTAPI_HOST}")
    private String FASTAPI_HOST;

    private int BATCH_SIZE = 1000;

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
            relationshipRepository.deleteAllSimilarRelationships();

            // 4. REUNION RELATIONSHIP - 배치 처리 방식
            createSimilarRelationships(similarities);
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

    private void createSimilarRelationships(Map<String, List<Map<String, Object>>> similarities) {
        int numThreads = Runtime.getRuntime().availableProcessors(); // 사용 가능한 프로세서 수만큼 스레드 생성
        log.info("AvailableProcessors : {}", numThreads);
        ExecutorService executorService = Executors.newFixedThreadPool(numThreads);

        try {
            List<Map<String, Object>> batchParams = new ArrayList<>();

            // 모든 유사성 관계 데이터 수집
            similarities.forEach((key, value) -> {
                for (Map<String, Object> sim : value) {
                    Map<String, Object> params = new HashMap<>();
                    params.put("fromTrackId", key);
                    params.put("toTrackId", sim.get("trackId"));
                    params.put("similarity", sim.get("similarity"));
                    batchParams.add(params);
                }
            });

            log.info("생성할 유사성 관계 수: {}", batchParams.size());

            // 배치 단위로 처리
            for (int i = 0; i < batchParams.size(); i += BATCH_SIZE) {
                int endIndex = Math.min(i + BATCH_SIZE, batchParams.size());
                List<Map<String, Object>> batch = batchParams.subList(i, endIndex);

                executorService.submit(() -> processBatch(batch));
            }

            executorService.shutdown();
            executorService.awaitTermination(1, TimeUnit.HOURS);

        } catch (InterruptedException e) {
            log.error("병렬 처리 중 인터럽트 발생", e);
            Thread.currentThread().interrupt();
        } finally {
            if (!executorService.isTerminated()) {
                executorService.shutdownNow();
            }
        }
    }

    private void processBatch(List<Map<String, Object>> batch) {
        try (var session = driver.session()) {
            String batchQuery =
                    "UNWIND $batch AS item " +
                            "MATCH (t1:Track {id: item.fromTrackId}), (t2:Track {id: item.toTrackId}) " +
                            "MERGE (t1)-[:SIMILAR{similarity: item.similarity}]->(t2)";

            Map<String, Object> parameters = new HashMap<>();
            parameters.put("batch", batch);

            session.writeTransaction(tx -> tx.run(batchQuery, parameters).consume());
            log.info("배치 처리 완료: {} 관계", batch.size());
        } catch (Exception e) {
            log.error("배치 처리 중 오류 발생", e);
        }
    }


}
