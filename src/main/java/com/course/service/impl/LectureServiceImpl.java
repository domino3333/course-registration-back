package com.course.service.impl;


import com.course.domain.Lecture;
import com.course.mapper.LectureMapper;
import com.course.service.LectureService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LectureServiceImpl implements LectureService {

    private final LectureMapper lectureMapper;

    @Override
    public List<Lecture> getLectureList() throws Exception{
        return lectureMapper.getLectureList();
    }
}
