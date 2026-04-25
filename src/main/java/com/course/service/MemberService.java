package com.course.service;


import com.course.domain.Member;

public interface MemberService {

    Member findMemberByEmail(String email) throws Exception;


}
