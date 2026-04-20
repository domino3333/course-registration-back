package com.course.controller;


import com.course.domain.Lecture;
import com.course.mapper.LectureMapper;
import com.course.service.LectureService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/lecture")
@Slf4j
@RequiredArgsConstructor
public class LectureController {

    private final LectureService lectureService;

    @GetMapping
    public ResponseEntity<?> getLectureList(){

        List<Lecture> list = null;
        try {
            list = lectureService.getLectureList();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        log.info("list:"+ list);

        return ResponseEntity.ok(list);
    }
}
