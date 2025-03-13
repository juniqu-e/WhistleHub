package com.ssafy.whistlehub.guide;

import com.ssafy.whistlehub.common.ApiResponse;
import com.ssafy.whistlehub.common.error.exception.NotFoundPageException;
//import com.ssafy.demo.graph.service.DataGeneratorService;
import com.ssafy.whistlehub.graph.model.entity.type.WeightType;
import com.ssafy.whistlehub.graph.service.DataCollectingService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;

@RequestMapping("/guide")
@RestController
@RequiredArgsConstructor
public class GuideController {

//    private final DataGeneratorService dataGeneratorService;
    private final DataCollectingService dataCollectingService;

    @GetMapping("/success")
    public ApiResponse<?> test() {
        return new ApiResponse.builder<Object>()
                .object("데이터")
                .build();
    }

    @GetMapping("/error")
    public void except(){
        throw new NotFoundPageException();
    }

    @GetMapping("/neo4j")
    public void neo4j() throws Exception {
//        dataGeneratorService.run();
    }

    @GetMapping("/create/member/{id}")
    public String createMember(@PathVariable int id) throws Exception {
        dataCollectingService.createMember(id);
        return "";
    }
    @GetMapping("/create/track/{id}")
    public String createTrack(@PathVariable int id) throws Exception {
        dataCollectingService.createTrack(id, Arrays.asList(1,2, 3, 4));
        return "";
    }

    @GetMapping("/view/{memberId}/{trackId}")
    public String view(@PathVariable int memberId, @PathVariable int trackId) throws Exception {
        dataCollectingService.viewTrack(memberId, trackId, WeightType.VIEW, Arrays.asList(2,3));
        return "";
    }

    @GetMapping("/tagging/{trackId}")
    public String tagging(@PathVariable int trackId) throws Exception {
        dataCollectingService.createTrack(trackId, Arrays.asList(1,2));
        return "";
    }

}
