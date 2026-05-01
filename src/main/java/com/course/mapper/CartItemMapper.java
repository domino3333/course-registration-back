package com.course.mapper;


import com.course.domain.CartItem;
import com.course.dto.cart.CartItemResponse;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;


@Mapper
public interface CartItemMapper {
    List<CartItemResponse> getCartItemList(String email) throws Exception;
    void addToCart(String email, long lectureNo) throws Exception;
    void deleteCartItem(String email, long lectureNo) throws Exception;
}
