package com.course.domain;


import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Registration {

    private Long registrationNo;
    private Long memberNo;
    private Long lectureNo;
    private LocalDateTime createdAt;

}
