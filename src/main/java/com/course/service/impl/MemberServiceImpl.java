package com.course.service.impl;

import com.course.domain.Member;
import com.course.dto.auth.SignUpRequest;
import com.course.mapper.CartMapper;
import com.course.mapper.MemberMapper;
import com.course.service.MemberService;
import jakarta.transaction.Transactional;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MemberServiceImpl implements MemberService {

    private final MemberMapper memberMapper;
    private final PasswordEncoder passwordEncoder;
    private final CartMapper cartMapper;

    @Override
    public Member findMemberByEmail(String email) throws Exception {

        return memberMapper.findMemberByEmail(email);
    }

    @Override
    @Transactional
    public void signUp(SignUpRequest request) throws Exception {
        Member member = new Member();
        String encodedPassword = passwordEncoder.encode(request.getPassword());
        member.setId("tempId");
        member.setName(request.getName());
        member.setGender(request.getGender());
        member.setEmail(request.getEmail());
        member.setCode(request.getCode());
        member.setPassword(encodedPassword);

        memberMapper.signUp(member);
        cartMapper.createCart(request.getEmail());



    }
}
