package com.ssafy.backend.common.config;

import dev.langchain4j.store.graph.neo4j.Neo4jGraph;
import org.neo4j.driver.Driver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class Neo4jGraphConfig {

    private final Driver driver; // Spring에서 관리하는 Neo4j 드라이버

    public Neo4jGraphConfig(Driver driver) {
        this.driver = driver;
    }

    @Bean
    public Neo4jGraph neo4jGraph() {
        return Neo4jGraph.builder()
                .driver(driver)
                .build();
    }
}
