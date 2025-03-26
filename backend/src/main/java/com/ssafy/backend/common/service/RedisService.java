package com.ssafy.backend.common.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

/**
 * <pre>Redis 서비스</pre>
 * Redis의 CRUD를 담당하는 서비스, 기본적으로 String key, Object value로 저장.
 * 추후, List, Set, Hash, ZSet 등의 다양한 데이터 타입을 저장할 수 있도록 확장 가능.
 *
 * @author 허현준
 * @version 1.0
 * @since 2025-03-26
 */

@Service
@RequiredArgsConstructor
public class RedisService {
    private final RedisTemplate<String, Object> redisTemplate;

    /**
     * Set에 key, value 추가
     *
     * @param key   저장할 value의 key
     * @param value 저장할 object
     */
    public void set(String key, Object value) {
        redisTemplate.opsForValue().set(key, value);
    }

    /**
     * Set에 key, value 추가
     *
     * @param key     저장할 value의 key
     * @param value   저장할 object
     * @param timeout 만료시간
     */
    public void set(String key, Object value, long timeout) {
        redisTemplate.opsForValue().set(key, value);
        expire(key, timeout);
    }

    /**
     * Set에 key만 추가
     *
     * @param key 저장할 값
     */
    public void setKeyOnly(String key) {
        redisTemplate.opsForValue().set(key, key);
    }

    /**
     * Set에 key만 추가
     *
     * @param key     저장할 값
     * @param timeout 만료시간
     */
    public void setKeyOnly(String key, long timeout) {
        redisTemplate.opsForSet().add(key);
        expire(key, timeout);
    }

    /**
     * redis에 저장된 값을 가져옴
     *
     * @param key 가져올 값의 key
     */
    public Object get(String key) {
        try {
            return redisTemplate.opsForValue().get(key);
        } catch(Exception e) {
            return null;
        }
    }

    /**
     * key에 해당하는 값을 삭제
     *
     * @param key 삭제할 값의 key
     */
    public void delete(String key) {
        redisTemplate.delete(key);
    }

    /**
     * key가 존재하는지 확인
     *
     * @param key 확인할 값의 key
     */
    public boolean hasKey(String key) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(key)); // null이 아닌 경우에만 true 반환
    }

    /**
     * key의 만료시간 설정
     *
     * @param key     만료시간을 설정할 key
     * @param timeout 만료시간
     */
    public void expire(String key, long timeout) {
        redisTemplate.expire(key, timeout, java.util.concurrent.TimeUnit.SECONDS);
    }

}
