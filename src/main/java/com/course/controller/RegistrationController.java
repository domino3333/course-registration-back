package com.course.controller;


import com.course.domain.Registration;
import com.course.service.MemberService;
import com.course.service.RegistrationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.coyote.Response;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/registration")
public class RegistrationController {


    private final RegistrationService registrationService;

    @PostMapping("/{lectureNo}")
    public ResponseEntity<?> registerLecture(Authentication authentication, @PathVariable long lectureNo){

        try {
            registrationService.registerLecture(authentication.getName(),lectureNo);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("등록 실패");

        }
        return ResponseEntity.ok("수강신청 성공");
    }

    @GetMapping
    public ResponseEntity<?> getRegistrationList(Authentication authentication){

        List<Registration> list;
        try {
            log.info("getRegistrationList진입, auth:" + authentication.getName());
            list = registrationService.getRegistrationList(authentication.getName());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("등록된 강의 목록을 가져오지 못함");
        }
        return ResponseEntity.ok(list);

    }
}
