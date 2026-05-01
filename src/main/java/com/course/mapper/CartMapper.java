package com.course.mapper;

import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface CartMapper {

    void createCart(String email) throws Exception;
}
