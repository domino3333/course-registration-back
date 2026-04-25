package com.course.service;


import com.course.domain.Member;
import com.course.dto.auth.SignUpRequest;

public interface MemberService {

    Member findMemberByEmail(String email) throws Exception;
    void signUp(SignUpRequest request) throws Exception;

}
