package com.course.dto.auth;

import lombok.Data;

@Data
public class SignUpRequest {

    private String email;
    private String password;
    private String name;
    private String code;
    private String gender;
}
