package com.course.service;

import com.course.domain.Lecture;

import java.util.List;

public interface LectureService {

    List<Lecture> getLectureList() throws Exception;
}
