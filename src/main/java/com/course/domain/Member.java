package com.course.domain;


import lombok.Data;

@Data
public class Member {
    
    private Long memberNo;
    private String name;
    private String id;
    private String gender;
    private String password;
    private String email;
    private String code; //학번

}
