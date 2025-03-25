package com.ssafy.backend.ai.service;

import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.googleai.GoogleAiGeminiChatModel;
import dev.langchain4j.model.input.Prompt;
import dev.langchain4j.model.input.PromptTemplate;
import dev.langchain4j.rag.content.Content;
import dev.langchain4j.rag.content.retriever.neo4j.Neo4jContentRetriever;
import dev.langchain4j.rag.query.Query;
import dev.langchain4j.store.graph.neo4j.Neo4jGraph;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Service
@RequiredArgsConstructor
public class Neo4jContentRetrieverService {

    private final Neo4jGraph neo4jGraph; // 의존성 주입된 Neo4jGraph
    private final ChatLanguageModel model;
    private Neo4jContentRetriever retriever;

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
        for(Content content : retriever.retrieve(query)) {
            list.add(Integer.parseInt(content.textSegment().text()));
        }
        return list;
    }
}

