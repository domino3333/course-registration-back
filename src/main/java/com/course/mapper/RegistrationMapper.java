package com.course.mapper;

import com.course.domain.Registration;
import com.course.dto.registration.RegistrationResponse;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface RegistrationMapper {

    List<RegistrationResponse> getRegistrationList(long memberNo) throws Exception;

    void registerLecture(long memberNo, long lectureNo) throws Exception;

    void cancelRegistration(Long memberNo, long registrationNo) throws Exception;

    int checkIsAlreadyRegistered(Long memberNo, long lectureNo) throws Exception;
}
