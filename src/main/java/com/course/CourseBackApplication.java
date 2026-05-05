package com.course;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@MapperScan(basePackages = "com.course.mapper")
public class CourseBackApplication {

    public static void main(String[] args) {
        SpringApplication.run(CourseBackApplication.class, args);
    }

}
