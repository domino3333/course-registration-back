package com.course.service.impl;

import com.course.dto.queue.QueueStatusResponse;
import com.course.service.QueueService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;


@Service
@RequiredArgsConstructor
public class QueueServiceImpl implements QueueService {

    private final StringRedisTemplate stringRedisTemplate;
    private final String LOGIN_QUEUE_KEY = "queue:login";
    private static final long MAX_ACTIVE_USERS = 3;


    @Override
    public Long enterQueue(String email) {

        // enter한 시간으로 순번을 정함
        double score = System.currentTimeMillis();

        // ZADD queue:login score email와 같음
        stringRedisTemplate.opsForZSet().add(LOGIN_QUEUE_KEY, email, score);

        return getMyRank(email);
    }

    @Override
    public Long getMyRank(String email) {

        // ZRANK queue:login email와 같음
        Long rank = stringRedisTemplate.opsForZSet().rank(LOGIN_QUEUE_KEY, email);

        if (rank == null) return null;

        return rank + 1;
    }

    @Override
    public void leaveQueue(String email) {
        //ZREM queue:login email와 같음
        stringRedisTemplate.opsForZSet().remove(LOGIN_QUEUE_KEY, email);
    }

    @Override
    public QueueStatusResponse getQueueStatus(String email) {

        Long rank = getMyRank(email);

        if (rank == null) {
            return new QueueStatusResponse(null, null, false);
        }

        Long waitingAhead = rank - 1;
        boolean allowed = rank <= MAX_ACTIVE_USERS;

        return new QueueStatusResponse(rank, waitingAhead, allowed);
    }

    @Override
    public boolean admit(String email) {

        // 내 순위 확인
        Long rank = getMyRank(email);

        //입장 불가능하면 false 반환
        if(rank == null || rank > MAX_ACTIVE_USERS){
            return false;
        }

        // 입장 가능하다면 대기 큐에서 삭제
        stringRedisTemplate.opsForZSet().remove(LOGIN_QUEUE_KEY,email);

        //입장 가능할 경우 ticket 만들어주기
        // key는 ticket:123@naver.com 형태로 생성
        String ticketKey = "ticket:" +email;
        //set으로 스트링 자료구조 생성
        stringRedisTemplate.opsForValue().set(ticketKey,"allowed",5, TimeUnit.MINUTES);


        return true;
    }
}
