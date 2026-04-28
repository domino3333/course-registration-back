package com.course.dto.registration;


import lombok.Data;

import java.time.LocalDateTime;

@Data
public class RegistrationResponse {

    private Long registrationNo;
    private LocalDateTime createdAt;
    private String title;
    private String professor;
    private Integer capacity;
    private Integer credit;

}
