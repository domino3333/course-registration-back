package com.course.service;

import com.course.domain.Registration;
import com.course.dto.registration.RegistrationResponse;

import java.util.List;

public interface RegistrationService {

    List<RegistrationResponse> getRegistrationList(String email) throws Exception;

    int registerLecture(String email, long lectureNo) throws Exception;

    void cancelRegistration(String email, long registrationNo) throws Exception;
}
