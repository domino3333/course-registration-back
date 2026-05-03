package com.course.service.impl;

import com.course.service.QueueService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class QueueServiceImpl implements QueueService {

    private final StringRedisTemplate stringRedisTemplate;
    private final String LOGIN_QUEUE_KEY = "queue:login";
    private final RedisTemplate<Object, Object> redisTemplate;

    @Override
    public Long enterQueue(String email) {

        // enter한 시간으로 순번을 정함
        double score = System.currentTimeMillis();

        // ZADD queue:login score email와 같음
        redisTemplate.opsForZSet().add(LOGIN_QUEUE_KEY,email,score);

        return getMyRank(email);
    }

    @Override
    public Long getMyRank(String email) {

        // ZRANK queue:login email와 같음
        Long rank = redisTemplate.opsForZSet().rank(LOGIN_QUEUE_KEY,email);

        if(rank==null) return null;

        return rank + 1;
    }

    @Override
    public void leaveQueue(String email) {
        //ZREM queue:login email와 같음
        redisTemplate.opsForZSet().remove(LOGIN_QUEUE_KEY,email);
    }
}
