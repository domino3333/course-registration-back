package com.course.service;

import com.course.dto.cart.CartItemResponse;
import com.course.enums.CartResult;

import java.util.List;

public interface CartItemService {
    List<CartItemResponse> getCartItemList(String email) throws Exception;

    CartResult addToCart(String email, long lectureNo) throws Exception;

    void deleteCartItem(String email, long lectureNo) throws Exception;
}
