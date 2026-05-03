package com.course.controller;


import com.course.service.QueueService;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.Response;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/queue")
public class QueueController {

    private final QueueService queueService;


    @PostMapping("/enter")
    public ResponseEntity<?> enterQueue(Authentication authentication){
        String email = authentication.getName();

        // 음
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
}










