package com.ssafy.backend.workstation.controller;

import com.ssafy.backend.ai.service.Neo4jContentRetrieverService;
import com.ssafy.backend.common.ApiResponse;
import com.ssafy.backend.openl3.dto.AiRecommendRequestDto;
import com.ssafy.backend.track.dto.request.TrackUploadRequestDto;
import com.ssafy.backend.track.service.TrackService;
import com.ssafy.backend.workstation.service.WorkstationService;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.googleai.GoogleAiGeminiChatModel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * <pre>Track 컨트롤러</pre>
 *
 * @author 박병주
 * @version 1.0
 * @since 2025-03-18
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/workstation")
@Slf4j
public class WorkstationController {
    private final TrackService trackService;
    private final WorkstationService workstationService;
    private final Neo4jContentRetrieverService neo4jContentRetrieverService;

    @PostMapping()
    public ApiResponse<?> createTrack(@ModelAttribute TrackUploadRequestDto trackUploadRequestDto) {
        log.info("⭐ Create new track ⭐ {}", trackUploadRequestDto.toString());
        return new ApiResponse.builder<Object>()
                .payload(workstationService.createTrack(trackUploadRequestDto))
                .build();
    }

    @GetMapping("/import")
    public ApiResponse<?> importTrack(int trackId) {
        return new ApiResponse.builder<Object>()
                .payload(workstationService.importTrack(trackId))
                .build();
    }

    @PostMapping("/ai/recommend")
    public ApiResponse<?> recommendImportTrack(@RequestBody AiRecommendRequestDto requset) {

        log.info("⭐ Recommend import track ⭐ {}", requset.getLayerIds());
        return new ApiResponse.builder<Object>()
                .payload(workstationService.recommendImportTrack(requset.getLayerIds()))
                .build();
    }

    //    @GetMapping("/ai")
//    public ApiResponse<?> getAi() {
//        ChatLanguageModel gemini = GoogleAiGeminiChatModel.builder()
//                .apiKey("AIzaSyDdQa4WGgijoYy01w1ZV4i56vBYNEn_JYc")
//                .modelName("gemini-2.0-flash")
//                .build();
//        ChatResponse chatResponse = gemini.chat(ChatRequest.builder()
//                .messages(UserMessage.from(
//                        "노래를 작곡해줘"))
//                .build());
//        return new ApiResponse.builder<Object>()
//                .payload(chatResponse.aiMessage().text())
//                .build();
//    }
    @GetMapping("/ai2")
    public ApiResponse<?> getAi2() {
        return new ApiResponse.builder<Object>()
                .payload(neo4jContentRetrieverService.retrieveContent(2))
                .build();
    }
}
