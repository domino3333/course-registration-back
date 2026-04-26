package com.course.service;

import com.course.domain.Registration;

import java.util.List;

public interface RegistrationService {

    List<Registration> getRegistrationList(String email) throws Exception;
}
