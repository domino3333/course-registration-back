package com.course.mapper;

import com.course.domain.Lecture;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface LectureMapper {

    List<Lecture> getLectureList() throws Exception;

    void increaseCurrentEnrollment(long lectureNo) throws Exception;
}
