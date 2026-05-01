package com.course.service;

import com.course.domain.CartItem;
import java.util.List;

public interface CartItemService {
    List<CartItem> getCartItemList(String email) throws Exception;

    void addToCart(String email, long lectureNo) throws Exception;
}
