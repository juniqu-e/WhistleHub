package com.ssafy.backend.common.config;

import dev.langchain4j.model.chat.ChatLanguageModel;
import static dev.langchain4j.model.chat.Capability.RESPONSE_FORMAT_JSON_SCHEMA;
import dev.langchain4j.model.chat.request.ResponseFormat;
import dev.langchain4j.model.chat.request.ResponseFormatType;
import dev.langchain4j.model.chat.request.json.JsonArraySchema;
import dev.langchain4j.model.chat.request.json.JsonObjectSchema;
import dev.langchain4j.model.chat.request.json.JsonSchema;
import dev.langchain4j.model.chat.request.json.JsonStringSchema;
import dev.langchain4j.model.googleai.GoogleAiGeminiChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.util.Set;

@Configuration
public class ChatLanguageModelConfig {

//    @Value("${OPEN_AI_KEY}")
//    private String aiKey;
//    @Value("${OPEN_AI_MODEL_NAME}")
//    private String modelName;
    @Value("${GEMINI_AI_KEY}")
    private String aiKey;
    @Value("${GEMINI_AI_MODEL_NAME}")
    private String modelName;

    // Gemini 사용 시
    @Bean
    public ChatLanguageModel chatLanguageModel() {
        return GoogleAiGeminiChatModel.builder()
                .apiKey(aiKey) // API 키 설정
                .modelName(modelName) // 모델 이름 설정
                .temperature(1.0)
                .build();
    }
    // chat gpt
//    @Bean
//    public ChatLanguageModel chatLanguageModel() {
//        return OpenAiChatModel.builder()
//                .apiKey(aiKey)
//                .temperature(1.0)
//                .timeout(Duration.ofSeconds(10000)) // 추론형 테스트 용 타임아웃
////                .supportedCapabilities(Set.of(RESPONSE_FORMAT_JSON_SCHEMA))
//                .strictJsonSchema(true)
//                .modelName(modelName)
//                .build();
//    }
}