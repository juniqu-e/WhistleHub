package com.ssafy.whistlehub.graph.util;/*
package com.ssafy.demo.neo4j.service;

import com.ssafy.demo.neo4j.Item;
import com.ssafy.demo.neo4j.Tag;
import com.ssafy.demo.neo4j.User;
import com.ssafy.demo.neo4j.repo.ItemRepository;
import com.ssafy.demo.neo4j.repo.TagRepository;
import com.ssafy.demo.neo4j.repo.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Component
@RequiredArgsConstructor
public class DataGenerator implements CommandLineRunner {

    private final UserRepository userRepo;
    private final ItemRepository itemRepo;
    private final TagRepository tagRepo;

    private static final int USER_COUNT = 10000; // 사용자 1만명
    private static final int ITEM_COUNT = 20000; // 아이템 2만개
    private static final int TAG_COUNT = 500; // 태그 500개

    @Override
    public void run(String... args) throws Exception {
        generateData();
        System.out.println("✅ 데이터 생성 완료!");
    }

    @Transactional
    public void generateData() {

        Random random = new Random();

        // 1. 태그 생성 및 저장
        List<Tag> tags = new ArrayList<>();
        for (int i = 0; i < TAG_COUNT; i++) {
            Tag tag = new Tag("Tag-" + i);
            tags.add(tag);
        }
        tagRepo.saveAll(tags);

        // 2. 사용자 생성 및 저장 (각 사용자에 랜덤 태그 연결)
        List<User> users = new ArrayList<>();
        for (int i = 0; i < USER_COUNT; i++) {
            User user = new User("User-" + i);

            // 랜덤으로 태그 연결 (1~5개)
            Set<Tag> preferredTags = new HashSet<>();
            for (int j = 0; j < random.nextInt(5) + 1; j++) {
                preferredTags.add(tags.get(random.nextInt(TAG_COUNT)));
            }
            user.setPreferredTags(preferredTags);
            users.add(user);
        }
        userRepo.saveAll(users);

        // 3. 아이템 생성 및 저장 (각 아이템에 랜덤 태그 연결)
        List<Item> items = new ArrayList<>();
        for (int i = 0; i < ITEM_COUNT; i++) {
            Item item = new Item("Item-" + i);

            // 랜덤으로 태그 연결 (1~3개)
            Set<Tag> itemTags = new HashSet<>();
            for (int j = 0; j < random.nextInt(3) + 1; j++) {
                itemTags.add(tags.get(random.nextInt(TAG_COUNT)));
            }
            item.setTags(itemTags);
            items.add(item);
        }
        itemRepo.saveAll(items);

        System.out.println("Data loading completed!");
    }
}
*/
