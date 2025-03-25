package com.ssafy.backend.common.config;

import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.googleai.GoogleAiGeminiChatModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ChatLanguageModelConfig {

    @Value("${GEMINI_AI_KEY}")
    private String AiKey;
    @Value("${GEMINI_AI_MODEL_NAME}")
    private String modelName;

    @Bean
    public ChatLanguageModel chatLanguageModel() {
        return GoogleAiGeminiChatModel.builder()
                .apiKey(AiKey) // API 키 설정
                .modelName(modelName) // 모델 이름 설정
                .build();
    }
}
