package com.course.service.impl;

import com.course.domain.Member;
import com.course.mapper.MemberMapper;
import com.course.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MemberServiceImpl implements MemberService {

    private final MemberMapper memberMapper;

    @Override
    public Member findMemberByEmail(String email) throws Exception {

        return memberMapper.findMemberByEmail(email);
    }
}
