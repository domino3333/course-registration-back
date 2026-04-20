package com.course.domain;


import lombok.Data;

@Data
public class Lecture {

    private Long lectureNo;
    private String title;
    private String professor;
    private Integer capacity;
    private Integer credit; //학점

}
