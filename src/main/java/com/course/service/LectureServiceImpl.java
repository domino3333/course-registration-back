package com.course.service;


import com.course.domain.Lecture;
import com.course.mapper.LectureMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LectureServiceImpl implements LectureService{

    private final LectureMapper lectureMapper;

    @Override
    public List<Lecture> getLectureList() throws Exception{
        return lectureMapper.getLectureList();
    }
}
