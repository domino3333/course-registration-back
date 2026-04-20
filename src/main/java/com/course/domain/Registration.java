package com.course.domain;


import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Registration {

    private Long registrationNo;
    private Long studentNo;
    private Long lectureNo;
    private LocalDateTime createdAt;

}
