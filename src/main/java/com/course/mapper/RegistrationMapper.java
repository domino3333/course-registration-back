package com.course.mapper;

import com.course.domain.Registration;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface RegistrationMapper {

    List<Registration> getRegistrationList(long memberNo) throws Exception;

    void registerLecture(long memberNo, long lectureNo) throws Exception;

    void cancelRegistration(Long memberNo, long registrationNo) throws Exception;

    int checkIsAlreadyRegistered(Long memberNo, long lectureNo) throws Exception;
}
