package com.ssafy.backend.common.config;

import com.ssafy.backend.common.util.TagNodeGenerator;
import com.ssafy.backend.graph.repository.TagNodeRepository;
import com.ssafy.backend.graph.service.DataCollectingService;
import com.ssafy.backend.mysql.repository.TagRepository;
import dev.langchain4j.store.graph.neo4j.Neo4jGraph;
import lombok.RequiredArgsConstructor;
import org.neo4j.driver.Driver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class Neo4jGraphConfig {

    private final Driver driver; // Spring에서 관리하는 Neo4j 드라이버
    private final TagNodeRepository tagNodeRepository;
    private final TagRepository tagRepository;

    @Bean
    public Neo4jGraph neo4jGraph() {
        return Neo4jGraph.builder()
                .driver(driver)
                .build();
    }

    @Bean
    public TagNodeGenerator tagNodeGenerator() {
        return new TagNodeGenerator(tagNodeRepository, tagRepository);
    }
}
