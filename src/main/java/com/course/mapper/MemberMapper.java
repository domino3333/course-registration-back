package com.course.mapper;


import com.course.domain.Member;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface MemberMapper {

    Member findMemberByEmail(String email) throws Exception;
}
