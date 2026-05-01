package com.course.controller;


import com.course.domain.Member;
import com.course.dto.auth.LoginRequest;
import com.course.dto.auth.LoginResponse;
import com.course.dto.auth.SignUpRequest;
import com.course.security.JwtTokenProvider;
import com.course.service.MemberService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.coyote.Response;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
@Slf4j
public class AuthController {

    private final JwtTokenProvider jwtTokenProvider;
    private final MemberService memberService;
    private final PasswordEncoder passwordEncoder;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {


        Member member = null;
        try {
            member = memberService.findMemberByEmail(request.getEmail());
        } catch (Exception e) {
            log.info("email에 해당하는 member가 존재하지 않습니다.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("login에러");
        }

        if (!passwordEncoder.matches(request.getPassword(), member.getPassword())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("비밀번호가 일치하지 않습니다.");
        }

        String token = jwtTokenProvider.createToken(request.getEmail());
        log.info("로그인성공");
        return ResponseEntity.ok(new LoginResponse(token));
    }

    @PostMapping("/signUp")
    public ResponseEntity<?> signUp(@RequestBody SignUpRequest request) {

        log.info("signUp 진입");
        log.info("request:" + request.getGender());

        try {
            memberService.signUp(request);
        } catch (Exception e) {
            log.info("signUp 메서드 에러");
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("회원가입 실패");
        }

        return ResponseEntity.ok("회원가입 성공");
    }


    @GetMapping
    public ResponseEntity<?> fetchMe(Authentication authentication) {
        Member member;
        try {
            log.info("fetchMe 진입");
            member = memberService.findMemberByEmail(authentication.getName());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("fetchMe 오류 발생");
        }

        return ResponseEntity.ok(member);
    }
}
