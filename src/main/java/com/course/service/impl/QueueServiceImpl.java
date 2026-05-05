package com.course.service.impl;

import com.course.dto.queue.QueueStatusResponse;
import com.course.service.QueueService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/*
* 1. 로그인 시 입장대기큐에 사용자를 집어넣는다.
* 2. admit, status를 호출할 때마다 공석이 발생하고
* 3. 입장한 사용자는 만료시간이 5분인 티켓을 발급받는다.
* */
@Service
@RequiredArgsConstructor
public class QueueServiceImpl implements QueueService {

    private final StringRedisTemplate stringRedisTemplate;

    private static final String LOGIN_QUEUE_KEY = "queue:login";
    private static final String ACTIVE_LOGIN_KEY = "active:login";

    private static final String TICKET_KEY_PREFIX = "ticket:";

    private static final long MAX_ACTIVE_USERS = 3;
    private static final long TICKET_TTL_MINUTES = 5;

    @Override
    public Long enterQueue(String email) {

        //이미 존재하는 대기순번이 있는지 확인(새로고침했을 때 대기열이 초기화되는 것을 방지)
        Double existingScore = stringRedisTemplate.opsForZSet().score(LOGIN_QUEUE_KEY, email);

        if (existingScore == null) {
            // enter한 시간으로 순번을 정함
            double score = System.currentTimeMillis();
            // ZADD queue:login score email와 같음
            stringRedisTemplate.opsForZSet().add(LOGIN_QUEUE_KEY, email, score);
        }
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
        stringRedisTemplate.opsForZSet().remove(ACTIVE_LOGIN_KEY, email);
        stringRedisTemplate.delete(TICKET_KEY_PREFIX + email);
    }

    @Override
    public QueueStatusResponse getQueueStatus(String email) {

        removeExpiredActiveUsers();

        Long rank = getMyRank(email);

        if (rank == null) {
            return new QueueStatusResponse(null, null, false);
        }

        long activeCount = getActiveUserCount();
        long availableSeats = MAX_ACTIVE_USERS - activeCount;

        Long waitingAhead = rank - 1;
        boolean allowed = availableSeats > 0 && rank <= availableSeats;

        return new QueueStatusResponse(rank, waitingAhead, allowed);
    }

    @Override
    public boolean admit(String email) {

        removeExpiredActiveUsers();

        // 내 순위 확인
        Long rank = getMyRank(email);

        //입장 불가능하면 false 반환
        if (rank == null) {
            return false;
        }

        long activeCount = getActiveUserCount();
        long availableSeats = MAX_ACTIVE_USERS - activeCount;

        if (availableSeats <= 0 || rank > availableSeats) {
            return false;
        }

        // 입장 가능하다면 로그인입장대기 큐에서 삭제
        stringRedisTemplate.opsForZSet().remove(LOGIN_QUEUE_KEY, email);

        //현재 시간으로부터 ~ 5분까지 사용가능하도록 만료시간 설정
        long expiresAt = System.currentTimeMillis()
                + TimeUnit.MINUTES.toMillis(TICKET_TTL_MINUTES);

        //active:login에 사용자 추가
        stringRedisTemplate.opsForZSet().add(ACTIVE_LOGIN_KEY, email, expiresAt);


        //ticket:123@naver.com - allowed 티켓 생성
        stringRedisTemplate.opsForValue().set(TICKET_KEY_PREFIX + email, "allowed", TICKET_TTL_MINUTES, TimeUnit.MINUTES);
        return true;
    }


    private long getActiveUserCount() {
        // active:login의 카디널리티
        Long count = stringRedisTemplate.opsForZSet().zCard(ACTIVE_LOGIN_KEY);
        return count == null ? 0 : count;
    }

    private void removeExpiredActiveUsers() {
        long now = System.currentTimeMillis();
        // 현재시간 전까지, 즉 만료된 사람들은 active유저에서 삭제
        // 스코어가 0~now 인 사용자를 삭제하는 것임, score는 시간이야
        stringRedisTemplate.opsForZSet().removeRangeByScore(ACTIVE_LOGIN_KEY, 0, now);
    }

    //10초마다 active:login 유저들 정리
    @Scheduled(fixedRate=10000)
    private void cleanupExpiredActiveUsers(){
        removeExpiredActiveUsers();
    }
}
