package com.course.service;

import com.course.dto.cart.CartItemResponse;

import java.util.List;

public interface CartItemService {
    List<CartItemResponse> getCartItemList(String email) throws Exception;

    void addToCart(String email, long lectureNo) throws Exception;

    void deleteCartItem(String email, long lectureNo) throws Exception;
}
