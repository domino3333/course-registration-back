package com.course.controller;


import com.course.domain.Member;
import com.course.dto.LoginRequest;
import com.course.dto.LoginResponse;
import com.course.security.JwtTokenProvider;
import com.course.service.MemberService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
@Slf4j
public class AuthController {

    private final JwtTokenProvider jwtTokenProvider;
    private final MemberService memberService;
    private final PasswordEncoder passwordEncoder;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request){


        Member member = null;
        try {
            member = memberService.findMemberByEmail(request.getEmail());
        } catch (Exception e) {
            log.info("email에 해당하는 member가 존재하지 않습니다.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("login에러");
        }

        if(!passwordEncoder.matches(request.getPassword(),member.getPassword())){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("비밀번호가 일치하지 않습니다.");
        }

        String token = jwtTokenProvider.createToken(request.getEmail());

        return ResponseEntity.ok(new LoginResponse(token));
    }
}
