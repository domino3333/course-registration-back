package com.course.service;

import com.course.domain.Registration;

import java.util.List;

public interface RegistrationService {

    List<Registration> getRegistrationList(String email) throws Exception;

    int registerLecture(String email, long lectureNo) throws Exception;

    void cancelRegistration(String email, long registrationNo) throws Exception;
}
