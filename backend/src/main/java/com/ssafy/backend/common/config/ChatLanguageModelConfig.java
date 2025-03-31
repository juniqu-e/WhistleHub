package com.ssafy.backend.common.config;

import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.googleai.GoogleAiGeminiChatModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ChatLanguageModelConfig {

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

//    @Bean
//    public ChatLanguageModel chatLanguageModel() {
//        return OpenAiChatModel.builder()
//                .apiKey(aiKey) // API 키 설정
//                .modelName(modelName) // 모델 이름 설정
//                .temperature(0.7)
//                .build();
//    }
}
