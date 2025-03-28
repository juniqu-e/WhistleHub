package com.ssafy.backend.common.util;

import com.ssafy.backend.graph.model.entity.TagNode;
import com.ssafy.backend.graph.repository.TagNodeRepository;
import com.ssafy.backend.graph.service.DataCollectingService;
import com.ssafy.backend.mysql.entity.Tag;
import com.ssafy.backend.mysql.repository.TagRepository;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class TagNodeGenerator {

    private final TagNodeRepository tagNodeRepository;
    private final TagRepository tagRepository;

    public TagNodeGenerator(TagNodeRepository tagNodeRepository, TagRepository tagRepository) {
        this.tagNodeRepository = tagNodeRepository;
        this.tagRepository = tagRepository;

        List<Tag> tags = tagRepository.findAll();
        for (Tag tag : tags) {
            if(!tagNodeRepository.existsById(tag.getId())) {
                tagNodeRepository.upsertTag(tag.getId(), tag.getName());
            }
        }
    }
}
