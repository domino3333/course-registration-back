package com.course.mapper;

import com.course.domain.Registration;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface RegistrationMapper {

    List<Registration> getRegistrationList(long memberNo) throws Exception;
}
