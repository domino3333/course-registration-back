package com.course.controller;


import com.course.dto.queue.QueueStatusResponse;
import com.course.service.QueueService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.coyote.Response;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/queue")
@Slf4j
public class QueueController {

    private final QueueService queueService;


    @PostMapping("/enter")
    public ResponseEntity<?> enterQueue(Authentication authentication){
        String email = authentication.getName();
        // 엔터 큐가 되자마자 바로 랭크를 계산해서 내려주기 위해 리턴을 rank로 받기
        Long rank = queueService.enterQueue(email);
        return ResponseEntity.ok(rank);
    }

    @GetMapping("/rank")
    public ResponseEntity<?> getMyRank(Authentication authentication){
        String email = authentication.getName();
        Long rank = queueService.getMyRank(email);
        return ResponseEntity.ok(rank);
    }


    @DeleteMapping("/leave")
    public ResponseEntity<?> leaveQueue(Authentication authentication){
        String email = authentication.getName();
        queueService.leaveQueue(email);
        return ResponseEntity.ok("대기열에서 제거되었습니다.");

    }

    @GetMapping("/status")
    public ResponseEntity<?> getQueueStatus(Authentication authentication){
        String email = authentication.getName();
        QueueStatusResponse status = queueService.getQueueStatus(email);
        return ResponseEntity.ok(status);
    }

    @PostMapping("/admit")
    public ResponseEntity<?> admit(Authentication authentication){
        log.info("QueueController admit 진입:"+ authentication.getName());
        return ResponseEntity.ok(queueService.admit(authentication.getName()));
    }

    @GetMapping("/ticket")
    public ResponseEntity<?> hasTicket(Authentication authentication){
        String email = authentication.getName();
        log.info("hasticket로그인포:"+email);
        boolean hasTicket = queueService.hasTicket(email);
        log.info("hasTicket?:"+hasTicket);
        return ResponseEntity.ok(hasTicket);

    }





}










