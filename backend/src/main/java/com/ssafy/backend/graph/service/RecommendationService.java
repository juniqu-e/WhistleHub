package com.ssafy.backend.graph.service;

import com.ssafy.backend.graph.model.entity.MemberNode;
import com.ssafy.backend.graph.repository.MemberNodeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <pre>추천 데이터 제공 서비스</pre>
 * neo4j 데이터를 활용해 추천 데이터를 가공하는 서비스
 * @author 박병주
 * @version 1.0
 * @since 2025-03- 12
 */

@Service
@RequiredArgsConstructor
public class RecommendationService {
    final private MemberNodeRepository memberNodeRepository;

//    public void get3DepthUserNodes(String username){
//        List<MemberNode> userNodes = memberNodeRepository.findUserNetworkByName(username);
//    }

}

