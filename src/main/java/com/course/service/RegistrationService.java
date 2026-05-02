package com.course.service;

import com.course.dto.registration.RegistrationResponse;
import com.course.enums.RegistrationResult;

import java.util.List;

public interface RegistrationService {

    List<RegistrationResponse> getRegistrationList(String email) throws Exception;

    RegistrationResult registerLecture(String email, long lectureNo) throws Exception;

    void cancelRegistration(String email, long registrationNo) throws Exception;
}
