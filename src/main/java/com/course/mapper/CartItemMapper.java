package com.course.mapper;


import com.course.domain.CartItem;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;


@Mapper
public interface CartItemMapper {
    List<CartItem> getCartItemList(String email) throws Exception;


}
