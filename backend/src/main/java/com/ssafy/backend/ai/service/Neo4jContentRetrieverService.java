package com.ssafy.backend.ai.service;

import com.ssafy.backend.graph.repository.MemberNodeRepository;
import com.ssafy.backend.graph.repository.TrackNodeRepository;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.rag.content.Content;
import dev.langchain4j.rag.content.retriever.neo4j.Neo4jContentRetriever;
import dev.langchain4j.rag.query.Query;
import dev.langchain4j.store.graph.neo4j.Neo4jGraph;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.neo4j.driver.internal.value.PathValue;
import org.neo4j.driver.types.Node;
import org.neo4j.driver.types.Path;
import org.neo4j.driver.types.Relationship;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class Neo4jContentRetrieverService {

    private final Neo4jGraph neo4jGraph; // 의존성 주입된 Neo4jGraph
    private final ChatLanguageModel model;
    private Neo4jContentRetriever retriever;
    private final TrackNodeRepository trackNodeRepository;

    @PostConstruct
    public void init() {
        this.retriever = Neo4jContentRetriever.builder()
                .graph(neo4jGraph)
                .chatLanguageModel(model)
                .build();
    }

    public List<Integer> retrieveContent(int trackCount) {
        // 쿼리 실행 및 결과 조회
        Query query = new Query("3번 트랙에 추천할 다른 트랙 " + trackCount + "개");
        List<Integer> list = new ArrayList<>();
        for (Content content : retriever.retrieve(query)) {
            list.add(Integer.parseInt(content.textSegment().text()));
        }
        return list;
    }
}

