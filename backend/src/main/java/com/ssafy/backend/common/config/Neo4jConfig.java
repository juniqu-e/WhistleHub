package com.ssafy.backend.common.config;

import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.Driver;
import org.neo4j.driver.GraphDatabase;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.neo4j.core.transaction.Neo4jTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * <pre>neo4j 연동 및 설정 파일</pre>
 *
 * @author 박병주
 * @version 1.0
 * @since 2025-03-12
 */

@Configuration
@EnableTransactionManagement
public class Neo4jConfig {

    @Value("${NEO4J_URI}")
    private String uri;

    @Value("${NEO4J_USERNAME}")
    private String username;

    @Value("${NEO4J_PASSWORD}")
    private String password;

    @Bean
    public Neo4jTransactionManager transactionManager(org.neo4j.driver.Driver driver) {
        return new Neo4jTransactionManager(driver);
    }

    @Bean
    public Driver neo4jDriver() {
        return GraphDatabase.driver(uri, AuthTokens.basic(username, password));
    }
}
