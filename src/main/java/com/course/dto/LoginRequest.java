package com.course.dto;

import lombok.Data;

@Data
public class LoginRequest {

    private String email;
    private String password;
}
